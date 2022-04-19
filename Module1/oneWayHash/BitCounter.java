/*Code written by Stefan Garrido 2022-04-19*/

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class BitCounter {
    public static void main(String[] args) {

        //Initial checks for invalid arguments.
        if(args.length != 2){
            System.out.println("Wrong number of arguments! The 2 arguments should be: " +
                    "\n<hash value in hex> <hash value in hex>");
            System.exit(-1);
        }

        if ( args[0].length() != args[1].length()){
            System.out.println("Error! The two hash values have to have the same length");
            System.exit(-1);
        }

        //only used for the for loop below
        byte [] value1 = args[0].toLowerCase().getBytes(StandardCharsets.UTF_8);
        byte [] value2= args[1].toLowerCase().getBytes(StandardCharsets.UTF_8);

        //checks for invalid characters in the given hash values
        for(int i = 0; i < value1.length; i++){

            if( !( (47 < value1[i] && value1[i] < 58) || (96 < value1[i] && value1[i] < 103)) ){
                System.out.println("Invalid character in the first hash value."
                        + "\nSee position " + (i+1) + " in the first string."
                        + "\nNote: Valid chars in hex are: abcdef0123456789");
                System.exit(-1);
            }
            if( !( (47 < value2[i] && value2[i] < 58) || (96 < value2[i] && value2[i] < 103)) ){
                System.out.println("Invalid character in the second hash value."
                                + "\nSee position " + (i+1) + " in the second string."
                                + "\nNote: Valid chars in hex are: abcdef0123456789");
                System.exit(-1);
            }
        }

        //convert to BigInteger, use radix 16 because hex is used
        BigInteger hash1 = new BigInteger(args[0], 16);
        BigInteger hash2 = new BigInteger(args[1], 16);

        /* XOR between the two hash values, bitCount() returns nr of bit = 1.
        We're only interested in the bits that are equal to 0, these are
        the bits that matched when we used XOR */
        int result = hash1.xor(hash2).bitCount();

        // Each char in string represents a nibble (4 bits), we therefore
        // multiply with 4 to get total number of bits in the string.
        int length = args[0].length() * 4;


        System.out.println("Program finished without errors."
                +"\nNumber of shared bits between the two hash values are: " + (length - result));
        System.exit(0);
    }
}
