/*
* Code written by Stefan Garrido for assignment Hidden encryption.
* The general idea to locate and decrypt is according to info from lab description:
*
*   Compute H(k), the MD5 hash of the secret key k.
*   Scan the file, by decrypting with k and searching for H(k) in the decrypted data.
*   When H(k) is found, it indicates the start of the blob.
*   Decrypt the succeeding blocks until the next H(k) is found.
*   Take the plaintext between the two occurrences of H(k) as the secret information, Data.
*   Decrypt the block after the second H(k). Call this value H’.
*   Compute the MD5 hash of the data, H(Data).
*   Verify that H(Data) equals H’, in which case the operation has been successful and Data is the hidden information.
* */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;

public class Hiddec {

    public static void main(String[] args){

        //if nr of args != 3 or 4 -> exit!
        if (args.length < 3 || args.length > 4) {
            System.out.println("Invalid number of arguments!" +
                    "\nArgs: <--key=KEY>, [optional: <--ctr=CTR>], <--input=INPUT>, <--output=OUTPUT>");
            System.exit(-1);
        }

        //init variables used to store arguments
        String[] splitted;
        String key = null;
        String ctr = null;
        byte[] inputFile = null;
        String outputFile = null;
        int pos1, pos2;

        // try/catch used for verification and assignment of arguments
        try {

            //check that the first arg is actually a key
            splitted = args[0].split("=");
            if (!(("--key").equals(splitted[0]))) {
                System.out.println("Wrong input for argument: <--key>");
                System.exit(-1);
            }
            key = splitted[1];

            //check if an optinal argument exist (eg. --ctr)
            if (args.length == 4) {

                splitted = args[1].split("=");
                if (!(("--ctr").equals(splitted[0]))) {
                    System.out.println("Wrong input for argument: <--ctr>");
                    System.exit(-1);
                }
                ctr = splitted[1];
                pos1 = 2;
                pos2 = 3;
            } else {
                pos1 = 1;
                pos2 = 2;
            }

            //check args for input & output file, assign if OK.
            splitted = args[pos1].split("=");
            if (!(("--input").equals(splitted[0]))) {
                System.out.println("Wrong input for argument: <--input>");
                System.exit(-1);
            }
            inputFile = Files.readAllBytes(Paths.get(splitted[1]));

            splitted = args[pos2].split("=");
            if (!(("--output").equals(splitted[0]))) {
                System.out.println("Wrong input for argument: <--output>");
                System.exit(-1);
            }
            outputFile = splitted[1];

        } catch (Exception e) {
            System.out.println("A problem with reading from arguments!" +
                    "\nException catched: " + e);
            System.exit(-1);
        }

        //convert key and ctr to byte arrays
        byte[] ctrInBytes = null;
        byte[] keyInBytes = hexToByteArray(key);
        if(ctr != null){
            ctrInBytes = hexToByteArray(ctr);
        }

        //hash keyInBytes using MD5
        byte[] keyHashed = md5Hash(keyInBytes);
        // store decrypted input file
        byte [] inputFileDecrypted = null;

        if(ctr == null){
            //Create cipher object for ECB
            Cipher cipher;
            SecretKeySpec cipherKey = new SecretKeySpec(keyInBytes, "AES");
            try{
                cipher = Cipher.getInstance("AES/ECB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE,cipherKey);
                inputFileDecrypted = cipher.update(inputFile);
            } catch(Exception e){
                System.out.println("A problem occurred with creating a Cipher object for ECB" +
                        "\nException catched: " + e);
                System.exit(-1);
            }
        }

        //go through file and try to find the hidden data
        byte[] dataFound = null;
        try{
            if(ctrInBytes == null)
                dataFound = ecbDecrypt(keyHashed, inputFileDecrypted);
            else
                dataFound = ctrDecrypt(keyHashed, keyInBytes, ctrInBytes, inputFile);
        } catch(Exception e){
            System.out.println("An error occurred while decrypting file"+
                    "\nException catched: " +e);
            System.exit(-1);
        }

        //write to output file
        try{
            Files.write(Paths.get(outputFile), dataFound);
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

    //returns hidden data found in input file, handles AES-ECB-128
    private static byte[] ecbDecrypt (byte[] keyHashed,byte[] inputFileDecrypted) throws IOException {

        byte [] dataResult = null;
        InputStream inputStream = new ByteArrayInputStream(inputFileDecrypted);
        byte [] buffer = new byte[16];
        boolean switcher = false;
        int pos = 0;
        int dataPosStart = 0;
        int dataPosEnd = 0;

        // read 16 bytes into buffer and try to find keyHashed in file.
        // if switcher = true then read buffer until the keyHashed is found again.
        // if found, save data in dataResult and check the following 16 block that hash/data matches.
        while(inputStream.read(buffer) != -1){

            //is only active after first pos of data is found
            if(switcher){
                if(Arrays.equals(keyHashed, buffer)){
                    dataPosEnd = pos;
                    dataResult = Arrays.copyOfRange(inputFileDecrypted, dataPosStart, dataPosEnd);

                    if(inputStream.read(buffer) != -1){

                        byte[] dataHash = md5Hash(dataResult);
                        if(Arrays.equals(dataHash, buffer)){
                            break;
                        }else{
                            System.out.println("Hash of data doesn't match data");
                            System.exit(-1);
                        }
                    }else{
                        System.out.println("No hash of data could be found");
                        System.exit(-1);
                    }
                }
            } else{
                if(Arrays.equals(keyHashed, buffer)){
                    dataPosStart = pos + 16;
                    switcher = true;
                }
            }
            pos += 16;
        }

        //exit if there isn't any hash of key in file
        if(dataPosStart == 0 || dataPosEnd == 0){
            System.out.println("The hash of key could not be found in file");
            System.exit(-1);
        }

        return dataResult;
    }

    //returns hidden data found in input file, handles AES-CTR-128
    private static byte[] ctrDecrypt (byte[] keyHashed, byte[] keyInBytes, byte[] ctrInBytes, byte[] inputFile) throws IOException {

        Cipher cipher = null;
        byte [] dataResult = null;
        InputStream inputStream = new ByteArrayInputStream(inputFile);
        byte [] buffer = new byte[16];
        byte[] blobStart;
        boolean found = false;
        int pos = 0;
        int dataPosStart = 0;
        int dataPosEnd = 0;

        // read 16 bytes into buffer and try to find the first keyHashed in file.
        while(inputStream.read(buffer) != -1){

            //makes sure that counter with cipher object doesn't change until we find the first hash in file
            cipher = initCipher(cipher,keyInBytes, ctrInBytes);

            //decrypt the 16 bytes from buffer
            buffer = cipher.update(buffer);

            if(Arrays.equals(keyHashed, buffer)){

                //restart counter and create a new array that starts from the beginning of the blob.
                cipher = initCipher(cipher,keyInBytes, ctrInBytes);
                blobStart = Arrays.copyOfRange(inputFile, pos, inputFile.length);
                blobStart = cipher.update(blobStart);
                inputStream = new ByteArrayInputStream(blobStart);
                inputStream.read(buffer); //read past first block which contains the first hash
                dataPosStart = 16;
                pos = 16;

                //read 16 bytes until we find the second keyHashed in the new array
                //if found, save data in dataResult and check the following 16 block that hash/data matches.
                while(inputStream.read(buffer) != -1){

                    if(Arrays.equals(keyHashed, buffer)){
                        dataPosEnd = pos;
                        dataResult = Arrays.copyOfRange(blobStart, dataPosStart, dataPosEnd);

                        if(inputStream.read(buffer) != -1){

                            byte[] dataHash = md5Hash(dataResult);
                            if(Arrays.equals(dataHash, buffer)){
                                found = true;
                                break;
                            }else{
                                System.out.println("Hash of data doesn't match data");
                                System.exit(-1);
                            }
                        }else{
                            System.out.println("No hash of data could be found");
                            System.exit(-1);
                        }
                    }
                    pos += 16;
                }
                if(found){break;}
                System.out.println("The second hash of key could not be found in file");
                System.exit(-1);
            }
            pos += 16;
        }

        //exit if there isn't any hash of key in file
        if(dataPosStart == 0){
            System.out.println("The first hash of key could not be found in file");
            System.exit(-1);
        }

        return dataResult;
    }

    //function initializes and returns a cipher object, used for CTR decrypt.
    private static Cipher initCipher(Cipher cipher, byte[] keyInBytes, byte[] ctrInBytes ){

        try{
            SecretKeySpec cipherKey = new SecretKeySpec(keyInBytes, "AES");
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParam = new IvParameterSpec(ctrInBytes);
            cipher.init(Cipher.DECRYPT_MODE, cipherKey, ivParam);
        }catch (Exception e){
            System.out.println("An error occured while creating a cipher object for CTR"+
                    "\nException catched: " + e);
            System.exit(-1);
        }

        return cipher;
    }

}

