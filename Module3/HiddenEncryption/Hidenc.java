/*
* Code written by Stefan Garrido for assignment Hidden encryption.
* */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Random;

public class Hidenc {

    public static void main(String[] args) {

        //If nr of args != 4,5,6 -> exit!
        if (args.length < 4 || args.length > 6) {
            System.out.println("Invalid number of arguments!" +
                    "\nArgs: <--key=KEY>, [optional: <--ctr=CTR>], [optional: <--offset=OFFSET>]," +
                    " <--input=INPUT>, <--output=OUTPUT>, and either <--template=TEMPLATE> OR <--size=SIZE>");
            System.exit(-1);
        }

        //Variable initialization
        String[] splitted;
        String key = null;
        String ctr = null;
        int offset = -1;
        byte[] inputFile = null;
        String outputFile = null;
        byte[] templateInBytes = null;
        int size = -1;

        //Try to assign arguments to variables
        try{
            for (String argument : args) {

                splitted = argument.split("=");
                if(splitted.length != 2){
                    System.out.println("One of the arguments has an invalid format.");
                    System.exit(-1);
                }
                switch (splitted[0]) {
                    case "--key" -> key = splitted[1];
                    case "--ctr" -> ctr = splitted[1];
                    case "--offset" -> {
                        offset = Integer.parseInt(splitted[1]);
                        //check that offset value isn't negative and a multiple of 16.
                        if (offset < 0 || offset % 16 != 0) {
                            System.out.println("Error with offset: " +
                                    "\nCheck that offset isn't negative and that it is a multiple of 16.");
                            System.exit(-1);
                        }
                    }
                    case "--input" -> inputFile = Files.readAllBytes(Paths.get(splitted[1]));
                    case "--output" -> outputFile = splitted[1];
                    case "--template" -> templateInBytes = Files.readAllBytes(Paths.get(splitted[1]));
                    case "--size" -> size = Integer.parseInt(splitted[1]);
                    default -> {
                        System.out.println("One of the arguments couldn't be matched. Please check format: ");
                        System.out.println("Args: <--key=KEY>, [optional: <--ctr=CTR>], [optional: <--offset=OFFSET>]," +
                                " <--input=INPUT>, <--output=OUTPUT>, and either <--template=TEMPLATE> OR <--size=Size>");
                        System.exit(-1);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("A problem with reading from arguments!" +
                    "\nException catched: " + e);
            System.exit(-1);
        }

        //Check that only one is given (--template or --size)
        if(templateInBytes != null && size != -1){
            System.out.println("Error: either argument for --template OR --size can be given.");
            System.exit(-1);
        }

        //Check that the necessary arguments are given
        if(key == null || inputFile == null || outputFile == null || (templateInBytes == null && size < 0)){
            System.out.println("Error with arguments, the following arguments must be given: ");
            System.out.println("<--key=KEY>, <--input=INPUT>, <--output=OUTPUT>, and either <--template=TEMPLATE> OR <--size=Size>");
            System.exit(-1);
        }

        //Check that input file contains data to encrypt
        if(inputFile.length == 0 || inputFile.length % 16 != 0){
            System.out.println("Error with input file: " +
                    "\nCheck that file isn't empty or that it is a multiple of 16.");
            System.exit(-1);
        }

        //Variable initialization
        byte[] ctrInBytes = null;
        Random random = new Random();
        Cipher cipher = null;

        //convert key and ctr to byte arrays
        byte[] keyInBytes = hexToByteArray(key);
        if(ctr != null){
            ctrInBytes = hexToByteArray(ctr);
        }

        //Create Cipher object for either ECB or CTR
        SecretKeySpec cipherKey = new SecretKeySpec(keyInBytes, "AES");
        try{
            if(ctr == null){
                cipher = Cipher.getInstance("AES/ECB/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE,cipherKey);
            }else{
                cipher = Cipher.getInstance("AES/CTR/NoPadding");
                IvParameterSpec ivParam = new IvParameterSpec(ctrInBytes);
                cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivParam);
            }
        } catch(Exception e){
            System.out.println("A problem occurred when creating a Cipher object." +
                    "\nException catched: " + e);
            System.exit(-1);
        }

        //Hash key and data found in inputFile using MD5
        byte[] keyHashed = md5Hash(keyInBytes);
        byte[] dataHashed = md5Hash(inputFile);

        //Generate a blob using keyHashed, dataHashed and inputFile according to
        // assignment and encrypt it using cipher object
        byte [] blob = generateBlob(keyHashed, dataHashed, inputFile);
        byte [] blobEncrypted = null;
        try{
            blobEncrypted = cipher.update(blob);
        }catch (Exception e){
            System.out.println("A problem occurred when encrypting blob" +
                    "\nException catched: " + e);
            System.exit(-1);
        }

        // If no template is given then generate random bytes and store them in
        // templateInBytes (according to given size)
        if(templateInBytes == null){
            templateInBytes = new byte[size];
            random.nextBytes(templateInBytes);
        }

        //Check if the blob fits inside the template (container)
        if(blobEncrypted.length > templateInBytes.length){
            System.out.println("The encrypted blob is larger than the container." +
                    "\nDubbelcheck the requested SIZE or the given TEMPLATE");
            System.exit(-1);
        }

        // Check if an offset is given, if not generate a random offset.
        if(offset == -1){

            //if the blob is the same size as the template, set offset to zero.
            if(blobEncrypted.length == templateInBytes.length){
                offset = 0;
            }else{

                //the upperLimit is when the blob is placed at the end of the container
                int upperLimit = templateInBytes.length - blobEncrypted.length;
                int randomInt;
                boolean found = false;

                //Generate a random number R €[0, upperLimit], add +1 to R until R % 16 = 0.
                // if R > upperLimit then start over. This way we don't spend time generating
                // a lot of random numbers until an R is found.
                do {
                    randomInt = (int) (Math.random() * (upperLimit + 1));
                    while (randomInt <= upperLimit) {

                        if (randomInt % 16 == 0) {
                            offset = randomInt;
                            found = true;
                            break;
                        }
                        randomInt++;
                    }
                } while (!found);
            }
        }

        //check that a given offset is valid so that the blob fits in the container
        if(offset + blob.length > templateInBytes.length){
            System.out.println("The given offset is invalid, blob will end up outside the container.");
            System.exit(-1);
        }

        //write the encrypted blob into the template
        for(int i = 0, j = offset; i < blobEncrypted.length; i++,j++){
            templateInBytes[j] = blobEncrypted[i];
        }

        //Try to write to output file
        try{
            Files.write(Paths.get(outputFile), templateInBytes);
        }catch(Exception e){
            System.out.println("A problem occured with writing to output file"+
                    "\nException catched: " + e);
            System.exit(-1);
        }

    }

    // according to Character.digit documentation in java:
    // https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#digit-char-int-
    // Inspiration from the following guide: https://www.geeksforgeeks.org/java-program-to-convert-hex-string-to-byte-array/
    private static byte[] hexToByteArray(String hexString){

        //for every hex string, we only need half the size since every char-pair in hex
        //is converted to a byte representation.
        byte[] result = new byte[hexString.length()/2];
        int first, second;
        try{
            for(int i=0, j=0; i < hexString.length(); i +=2, j++){
                //take the numeric values of the pair, add them, and store as one byte in result
                first = Character.digit(hexString.charAt(i), 16);
                second = Character.digit(hexString.charAt(i+1), 16);
                result[j] = (byte) ((first <<4) + second);
            }
        }catch(Exception e){
            System.out.println("Hex representation for Key and/or CTR is incorrect");
            System.exit(-1);
        }
        return result;
    }

    //returns a MD5 hash of a byte array
    private static byte[] md5Hash(byte [] data){
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(data);
        } catch (Exception e) {
            System.out.println("A problem occured with creating an instance of MD5" +
                    "\nException catched: " + e);
            System.exit(-1);
        }
        return hash;
    }

    //returns a byte array representing a blob according to assignment
    private static byte[] generateBlob(byte[] keyHashed, byte[] dataHashed, byte[] data){
        byte[] blob = new byte[keyHashed.length*2 + data.length + dataHashed.length];

        //write data from keyHashed to blob
        for(int i = 0, j = (keyHashed.length + data.length ); i < keyHashed.length; i++, j++){
            blob[i] = keyHashed[i];
            blob[j] = keyHashed[i];
        }

        //write data from data array to blob
        for(int i=0, j = keyHashed.length; i < data.length; i++, j++){
            blob[j] = data[i];
        }

        //write data from dataHashed to blob
        for(int i=0, j = ((keyHashed.length*2) +data.length) ; i < dataHashed.length; i++, j++){
            blob[j] = dataHashed[i];
        }

        return blob;
    }

}
