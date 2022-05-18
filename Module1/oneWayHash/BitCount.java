

import java.math.BigInteger;

public class BitCount {
    public static void main(String [] args){

        try{
            //String in1 = Files.readString(Paths.get(args[0]));
            //String in2 = Files.readString(Paths.get(args[1]));

            //String in1 = args[0];
            //String in2 = args[1];

            // 614f4def91527419f4d2b69f09aa1be0 - AUTHOR md5
            // a9629c4b3259fedc39d2aed64940495a - AUTHOR2 md5

            // fd04dc9c7a2e391893f551e23321dd055eb10d02e756665707df7848cdad09a6 - AUTHOR sha256
            // 92690c9e117abf9d53c844d7a7b44784db02c014b28f2a20e66d4cc7f809dde9 - AITHOR2 sha256

            String string1 = "0e1ff9de0138a94f20ffd6d074feb07cba51938213d7c182895127126a03b441";
            String string2 = "7f604d5ccc93cee4693c7b540ef9bda59a54443566496210ac4f2b6e2cb5c851";

            // Eftersom hash-värdet är i basen 16 omvandlar vi till basen 10
            BigInteger bi1 = new BigInteger(string1, 16);
            BigInteger bi2 = new BigInteger(string2, 16);

            byte[] inFile1 = getBigByteArray(bi1);
            byte[] inFile2 = getBigByteArray(bi2);

            int len1 = (inFile1[inFile1.length-1] == 0 ? inFile1.length-1 : inFile1.length);
            int len2 = (inFile2[inFile1.length-1] == 0 ? inFile2.length-1 : inFile2.length);

            // -----------------------------------------------------------------------

            if(len1 != len2){
                System.out.println("Different size of the first and second file!");
                System.exit(1);
            }

            // -----------------------------------------------------------------------

            int total_count = 0;

            for(int i = 0; i < len1; i++){
                byte b = (byte) ~(inFile1[i] ^ inFile2[i]);
                total_count += countBits(b);
            }

            System.out.println("Total number of shared bits is " + total_count);
            System.out.println("Total number of bits in file1 " + (len1 * 8));
            System.out.println("Total number of bits in file2 " + (len2 * 8));

        }catch(Exception e){
            System.out.println("Something went wrong :/\n" + e);
            System.exit(1);
        }
    }

    public static int countBits(byte b){

        int number = (b & 0xff);
        if(number < 0){
            System.out.println("Byte got negative sadge");
            System.exit(1);
        }
        if(number == 0){
            return number;
        }

        int count = 0;
        while (number != 0) {
            number &= (number - 1);
            count++;
        }
        return count;
    }

    public static byte[] getBigByteArray(BigInteger b){

        byte[] inFile = b.toByteArray();
        if(inFile[0] == 0){
            for(int i = 0; i < inFile.length-1; i++){
                inFile[i] = inFile[i+1];
            }
            inFile[inFile.length-1] = 0;
        }
        return inFile;
    }
}