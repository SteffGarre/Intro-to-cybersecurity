/*
* A simple program to check a given string, hash it using jcrypt
* and compare to entries in password file. Print if match is found.
* */

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
            boolean found = false;

            while((currentString = passwordFile.readLine()) != null){
                String [] stringArray = currentString.split(":", 14);

                if(jcrypt.crypt(stringArray[1].substring(0,2), string).equals(stringArray[1])){
                    System.out.println("Matched user: " + stringArray[0]);
                    System.out.println(string + ": Salt = " + stringArray[1].substring(0,2) + ", Hash = " + stringArray[1] );
                    found = true;
                }

            }
            if(!found){
                System.out.println("No match found!");
            }

            System.out.println("Program finished");
            System.exit(0);

        } catch (Exception e){
            System.out.println("Exception: " + e);
        }

    }
}
