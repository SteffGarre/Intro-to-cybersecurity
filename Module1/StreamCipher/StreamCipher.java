
/*
* Code written by Stefan Garrido, 2022.03.26
*  Guide used to implement a fast reading of binary files:
* https://www.codejava.net/java-se/file-io/how-to-read-and-write-binary-files-in-java
* */


import java.io.*;
import java.math.BigInteger;

public class StreamCipher {

    private static final int bufferSize = 4096;

    public static void main(String[] args) {

        //check for correct number of arguments
        if(args.length != 3){
            System.out.println("Incorrect number of arguments, input should be:" +
                    "\n<key (integer)>, <name of infile>, <name of outfile>");
            System.exit(1);
        }

        //tries to read "infile" and outputs the requested "outfile"
        try{

            //try to parse key as an integer, zero and negative numbers are not allowed.
            // If it fails -> system exit!
            try{
                /* // Used for task 2
                Long.parseLong(args[0]);
                if(Long.parseLong(args[0]) < 1){
                    System.out.println("Integer \"" + Long.parseLong(args[0]) + "\" is not allowed as key!\n");
                    System.exit(1);
                }*/

                //Used for task 3
                BigInteger key = new BigInteger(args[0]);
                if( (key.compareTo(BigInteger.ZERO)) < 1){
                    System.out.println("Integer \"" + key + "\" is not allowed as key!\n");
                    System.exit(1);
                }
            } catch (NumberFormatException e){
                System.out.println("Couldn't parse argument to an integer, only integers > 0 are allowed.");
                System.out.println("Exception catched: " + e +"\n");
                System.exit(1);
            }

            //Uses args[0] as seed for rand, args[1] for reading from binary file,
            // and args[2] to write to a binary file.

            // Random rand = new Random(Long.parseLong(args[0]));       //used on task 1
            //StreamCipher.StreamCipher.MyRandom rand = new StreamCipher.StreamCipher.MyRandom(Long.parseLong(args[0]));    //used on task 2

            MyRandom rand = new MyRandom(args[0]);                      //used on task 3
            InputStream inputStream = new BufferedInputStream(new FileInputStream(args[1]));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(args[2]));

            // buffer used to buffer a determined "buffer size" to increase speed in I/O operations.
            byte [] buffer = new byte[bufferSize];
            int nrOfBytesRead;

            //while there is data in the buffer, take that data and XOR every element with a random byte,
            //take the updated buffer and write to file.
            while( (nrOfBytesRead = inputStream.read(buffer)) != -1){

                for (int i = 0; i < buffer.length; i++){
                    //buffer[i] = (byte) (buffer[i] ^ rand.nextInt(256));    //task 2
                    buffer[i] = (byte) (buffer[i] ^ rand.next(8));      //task 3
                }
                outputStream.write(buffer, 0, nrOfBytesRead);
            }

            //close input/output-stream
            inputStream.close();
            outputStream.close();

            System.out.println("\n* * * Successfully executed the program. Good day! * * *\n");
            System.exit(0);

        } catch(Exception e){
            System.out.println("\nOops! Problem with reading from/writing to file.");
            System.out.println("Exception catched: " + e);
            System.exit(1);
        }
    }
}
