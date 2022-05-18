import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class checkString {

    public static void main(String[] args) {

        if(args.length != 2){
            System.out.println("Wrong input, expected: <passwd file> <string>");
            System.exit(-1);
        }

        try{
            BufferedReader passwordFile = new BufferedReader(new FileReader(args[0]));
            Scanner scanner = new Scanner(args[1]);
            String currentString;
            String string = scanner.nextLine();

            while((currentString = passwordFile.readLine()) != null){
                String [] stringArray = currentString.split(":", 14);

                if(jcrypt.crypt(stringArray[1].substring(0,2), string).equals(stringArray[1])){
                    System.out.println("Matched user: " + stringArray[0]);
                    System.out.println(string +", " + stringArray[1].substring(0,2) + ", " + stringArray[1] );
                }

            }

            System.out.println("Finished");
            System.exit(0);

        } catch (Exception e){
            System.out.println("Exception: " + e);
        }

    }
}
