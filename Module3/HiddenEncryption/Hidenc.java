/*
* Code written by Stefan Garrido for assignment Hidden encryption.
* */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

public class Hidenc {

    public static void main(String[] args) {

        //if nr of args != 4,5,6 -> exit!
        if (args.length < 4 || args.length > 6) {
            System.out.println("Invalid number of arguments!" +
                    "\nArgs: <--key=KEY>, [optional: <--ctr=CTR>], [optional: <--offset=OFFSET>]," +
                    " <--input=INPUT>, <--output=OUTPUT>, and either <--template=TEMPLATE> OR <--size=Size>");
            System.exit(-1);
        }

        //variable initialization
        String[] splitted;
        String key = null;
        String ctr = null;
        int offset = -1;
        byte[] inputFile = null;
        String outputFile = null;
        byte[] templateInBytes = null;
        int size = -1;

        //try to assign arguments to variables
        try{
            for (String argument : args) {

                splitted = argument.split("=");
                if(splitted.length != 2){
                    System.out.println("One of the arguments has an invalid format.");
                    System.exit(-1);
                }
                switch (splitted[0]) {
                    case "--key":
                        key = splitted[1];
                        break;
                    case "--ctr":
                        ctr = splitted[1];
                        break;
                    case "--offset":
                        offset = Integer.parseInt(splitted[1]);
                        //check that offset value isn't negative and a multiple of 16.
                        if(offset < 0 || offset % 16 != 0){
                            System.out.println("Error with offset: " +
                                    "\nCheck that offset isn't negative and that it is a multiple of 16.");
                            System.exit(-1);
                        }
                        break;
                    case "--input":
                        inputFile = Files.readAllBytes(Paths.get(splitted[1]));
                        break;
                    case "--output":
                        outputFile = splitted[1];
                        break;
                    case "--template":
                        templateInBytes = Files.readAllBytes(Paths.get(splitted[1]));
                        break;
                    case "--size":
                        size = Integer.parseInt(splitted[1]);
                        break;
                    default: {
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

        //check that only one is given (--template or --size)
        if(templateInBytes != null && size != -1){
            System.out.println("Error: either argument for --template OR --size can be given.");
            System.exit(-1);
        }

        //check that the necessary arguments are given
        if(key == null || inputFile == null || outputFile == null || (templateInBytes == null && size < 0)){
            System.out.println("Error with arguments, the following arguments must be given: ");
            System.out.println("<--key=KEY>, <--input=INPUT>, <--output=OUTPUT>, and either <--template=TEMPLATE> OR <--size=Size>");
            System.exit(-1);
        }

        //check that input file contains data to encrypt
        if(inputFile.length == 0 || inputFile.length % 16 != 0){
            System.out.println("Error with input file: " +
                    "\nCheck that file isn't empty or that it is a multiple of 16.");
            System.exit(-1);
        }

        //variable initialization
        byte[] ctrInBytes = null;
        Random random = new Random();
        Cipher cipher = null;

        //generate random bytes and store them in templateInBytes (according to given size)
        if(templateInBytes == null){
            templateInBytes = new byte[size];
            random.nextBytes(templateInBytes);
        }

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
                cipher.init(Cipher.DECRYPT_MODE,cipherKey);
            }else{
                cipher = Cipher.getInstance("AES/CTR/NoPadding");
                IvParameterSpec ivParam = new IvParameterSpec(ctrInBytes);
                cipher.init(Cipher.DECRYPT_MODE, cipherKey, ivParam);
            }
        } catch(Exception e){
            System.out.println("A problem occurred when creating a Cipher object." +
                    "\nException catched: " + e);
            System.exit(-1);
        }

        //hash key and data found in inputFile using MD5
        byte[] keyHashed = md5Hash(keyInBytes);
        byte[] dataHashed = md5Hash(inputFile);

        //Generate a blob using keyHashed, dataHashed and inputFile according to
        // assignment and encrypt it using cipher object
        byte [] blob = generateBlob(keyHashed, dataHashed, inputFile);
        byte [] blobEncrypted = null;
        try{
            blobEncrypted = cipher.update(blob);
        }catch (Exception e){
            System.out.println("A problem occurred when encryptning blob" +
                    "\nException catched: " + e);
            System.exit(-1);
        }




        /*
        //Testing of blob and data related to that
        System.out.println("Blob:");
        System.out.println(Arrays.toString(blob));
        System.out.println();

        System.out.println("key hashed:");
        System.out.println(Arrays.toString(keyHashed));
        System.out.println();

        System.out.println("data:");
        System.out.println(Arrays.toString(inputFile));
        System.out.println();

        System.out.println("dataHashed:");
        System.out.println(Arrays.toString(dataHashed));

        System.out.println("size of Blob: " + blob.length);
        System.out.println("size of keyHashed: " + keyHashed.length*2);
        System.out.println("size of data: " + inputFile.length);
        System.out.println("size of dataHashed: " + dataHashed.length);
        System.out.println("Total: " + (keyHashed.length*2 + inputFile.length + dataHashed.length));

         */




        /*

        //write to output file
        try{
            Files.write(Paths.get(outputFile), templateInBytes);
        }catch(Exception e){
            System.out.println("A problem occured with writing to output file"+
                    "\nException catched: " + e);
            System.exit(-1);
        }

        */


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
