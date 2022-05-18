import java.io.*;
import java.security.*;

public class CollisionRes {

    private static final int bufferSize = 4096;

    public static void main(String[] args) {

        //check for correct number of arguments
        if(args.length != 1){
            System.out.println("Incorrect number of arguments, expected input: " +
                    "\n<file containing a message>");
            System.exit(1);
        }

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            InputStream inputStream = new BufferedInputStream(new FileInputStream(args[0]));

            // buffer used to buffer a determined "buffer size" to increase speed in I/O operations.
            byte [] buffer = new byte[bufferSize];
            int nrOfBytesRead;

            while( (nrOfBytesRead = inputStream.read(buffer)) != -1){

                for (int i = 0; i < buffer.length; i++){

                }

            }
            //close InputStream
            inputStream.close();

        } catch (Exception e){
            System.out.println("Error! Problem reading from given file"
                    + "\nException catched: " + e);
            System.exit(-1);
        }
    }
}
