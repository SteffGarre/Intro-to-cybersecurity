/*
* Code written by Stefan Garrido, 2022.03.26
*  Guide used to implement a fast reading of binary files:
* https://www.codejava.net/java-se/file-io/how-to-read-and-write-binary-files-in-java
*
* */


import java.io.*;
import java.util.*;

public class StreamCipher {

    private static final int bufferSize = 4096;

    public static void main(String[] args) {

        //check for correct number of arguments
        if(args.length != 3){
            System.out.println("Incorrect number of arguments, input should be:" +
                    "\n <key (integer)>, <name of infile>, <name of outfile>");
            System.exit(1);
        }

        //tries to read "infile" and outputs the requested "outfile"
        try{

            //try to parse key as an integer, zero and negative numbers are not allowed.
            // If it fails -> system exit!
            try{
                Long.parseLong(args[0]);

                if(Long.parseLong(args[0]) < 1){
                    System.out.println("Integer " + Long.parseLong(args[0]) + " is not allowed as key!");
                    System.exit(1);
                }
            } catch (NumberFormatException e){
                System.out.println("Exception catched: " + e);
                System.exit(1);
            }

            Random rand = new Random(Long.parseLong(args[0]));

            InputStream inputStream = new BufferedInputStream(new FileInputStream(args[1]));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(args[2]));

            byte [] buffer = new byte[bufferSize];
            int byteRead;

            while( (byteRead = inputStream.read(buffer)) != -1){


                outputStream.write(buffer, 0, byteRead);
            }


        } catch(Exception e){
            System.out.println("Exception catched: " + e);
            System.out.println("System exit! Good bye :(");
            System.exit(1);
        }
    }
}
