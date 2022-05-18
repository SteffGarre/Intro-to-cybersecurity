/*
* The program is written by Stefan Garrido, 04. 2022
* The program goes through a given dictionary and password file and tries
* to crack the password hash in the password file.
* */


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordCrack {
    final static char [] charactersAll = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890123456789".toCharArray();
    final static char [] charactersLow = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    final static char [] charactersUpp = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    final static char [] charactersNumb = "0123456789".toCharArray();
    private static List<String> listWithMangles = new ArrayList<>();


    public static void main(String[] args) {

        //check for correct number of arguments
        if(args.length != 2){
            System.out.println("Incorrect number of arguments, input should be:" +
                    "\n<dictionary file>, <password file>");
            System.exit(-1);
        }

        try{

            //variable declarations
            File dictFile = new File(args[0]);
            BufferedReader dictionaryReader;
            BufferedReader passwordFile = new BufferedReader(new FileReader(args[1]));
            List<String> hashedPasswords = new ArrayList<>();
            List<String> salts = new ArrayList<>();
            String currentString;
            String firstname;
            String lastname;
            String hash;
            String salt;

            //While loop goes through users and their corresponding hash & salt in password file
            //loop saves users hash & salt and checks variations of name against hashed password.
            while((currentString = passwordFile.readLine()) != null){
                String [] stringArray = currentString.split(":", 14);
                hashedPasswords.add(stringArray[1]);
                salts.add(stringArray[1].substring(0,2));
                hash = stringArray[1];
                salt = stringArray[1].substring(0,2);
                stringArray = stringArray[4].split(" ");

                // checks if name contains the format:
                // <firstname> <initial.> <lastname>  || <initial.> <firstname> <lastname>
                if (stringArray.length == 2){
                    firstname = stringArray[0];
                    lastname = stringArray[1];
                }else{
                    lastname = stringArray[2];
                    if(stringArray[0].charAt(1) == '.'){firstname = stringArray[1];
                    } else {firstname = stringArray[0];}
                }

                //call mangleWord() via checkName() to check if password is related to a users name.
                if(checkName(firstname, salt, hash)){
                    hashedPasswords.remove(hash);
                    salts.remove(salt);
                    continue;
                }
                if(checkName(lastname, salt, hash)){
                    hashedPasswords.remove(hash);
                    salts.remove(salt);
                    continue;
                }
                if(checkName(firstname + lastname, salt, hash)){
                    hashedPasswords.remove(hash);
                    salts.remove(salt);
                }
            }

            //variable declatation
            List<String> currentList = new ArrayList<>();
            String prevWord = null;
            String temp;
            char L = 'L';
            char U = 'U';
            boolean controlBoolean;
            int round = 1;

            /* 7 rounds:
            * 1: go through dict and mangle every word without append/prepend
            * 2: go through dict again and only append/prepend numbers
            * 3: go through dict again with only append char (lowecase)
            * 4: go through dict again with only append char (uppercase)
            * 5: go through dict again with only prepend char (lowecase)
            * 6: go through dict again with only prepend char (uppercase)
            * */
            while(round != 7) {

                if (hashedPasswords.isEmpty()) {
                    break;
                }

                dictionaryReader = new BufferedReader(new FileReader(dictFile));
                while ((currentString = dictionaryReader.readLine()) != null) {

                    if (round == 1) {

                        //check for word in dictionary as it is
                        for (int j = 0; j < hashedPasswords.size(); j++) {
                            if (jcrypt.crypt(salts.get(j), currentString).equals(hashedPasswords.get(j))) {
                                System.out.println(currentString);
                                hashedPasswords.remove(hashedPasswords.get(j));
                                salts.remove(salts.get(j));
                            }
                        }

                        //if word is >8, only do the if() in mangleWord()
                        controlBoolean = false;
                        if (currentString.length() > 8) {
                            temp = currentString.substring(0, 8);
                            if (temp.equals(prevWord)) {
                                controlBoolean = true;
                            } else {
                                prevWord = temp;
                            }
                        }
                        currentList = mangleWord(currentString, controlBoolean);
                    }
                    if (round == 2) {
                        currentList = appendPrependNumb(currentString);
                    }
                    if (round == 3) {
                        currentList = appendChar(currentString, L);
                    }
                    if (round == 4) {
                        currentList = appendChar(currentString, U);
                    }
                    if (round == 5) {
                        currentList = prependChar(currentString, L);
                    }
                    if (round == 6) {
                        currentList = prependChar(currentString, U);
                    }

                    for (String string : currentList) {
                        for (int j = 0; j < hashedPasswords.size(); j++) {

                            if(round == 1){
                                listWithMangles.add(string);
                            }
                            if (jcrypt.crypt(salts.get(j), string).equals(hashedPasswords.get(j))) {
                                System.out.println(string);
                                hashedPasswords.remove(hashedPasswords.get(j));
                                salts.remove(salts.get(j));
                            }
                        }
                        if (hashedPasswords.isEmpty()) {break;}
                    }
                    if (hashedPasswords.isEmpty()) {break;}
                }
                round++;
            }

            System.exit(0);

        } catch (Exception e){
            System.out.println("\nAn error with reading from file(s) has occurred.");
            System.out.println("Exception catched: " + e);
            System.exit(-1);
        }
    }

    // Help function that checks the names in the first while loop in main.
    private static Boolean checkName(String stringToCheck, String salt, String hash){

        Boolean alwaysFalse = false;
        List<String> listOfVariations = mangleWord(stringToCheck, alwaysFalse);

        for (String string: listOfVariations) {
            if(jcrypt.crypt(salt, string).equals(hash)){
                System.out.println(string);
                return true;
            }
        }

        //is used when every word in listOfVariations is mangled again
        if(dubbelcheckNames(listOfVariations, salt, hash)){
            return true;
        }

        listOfVariations = mangleWordAdvance(stringToCheck);
        for (String string: listOfVariations) {
            if(jcrypt.crypt(salt, string).equals(hash)){
                System.out.println(string);
                return true;
            }
        }

        //is used when every word in listOfVariations is mangled again
        if(dubbelcheckNames(listOfVariations, salt, hash)){
            return true;
        }
        return false;
    }
    

    //help function is used when a double mangle of a name is required
    private static Boolean dubbelcheckNames (List<String> list, String salt, String hash){

        Boolean alwaysFalse = false;
        for (String s: list) {
            List<String> listOfVariations = mangleWord(s, alwaysFalse);
            for (String string: listOfVariations) {
                if(jcrypt.crypt(salt, string).equals(hash)){
                    System.out.println(string);
                    return true;
                }
            }

            listOfVariations = mangleWordAdvance(s);
            for (String string: listOfVariations) {
                if(jcrypt.crypt(salt, string).equals(hash)){
                    System.out.println(string);
                    return true;
                }
            }
        }
        return false;
    }


    //mangles a word according to the suggested list in assignment description
    private static List<String> mangleWord(String string, Boolean control){

        //List is used to store all variations
        List<String> variationsOfString = new ArrayList<>();

        String revString = new StringBuilder(string).reverse().toString();

        if(control){
            //reverse and reflect string
            variationsOfString.add(revString);

            //delete first char of string
            variationsOfString.add(string.substring(1));

            return variationsOfString;
        }else {

            //used in the following statement and in the last (toggle function)
            String lowerString = string.toLowerCase();

            //Lowercase string
            variationsOfString.add(lowerString);

            //Uppercase string
            variationsOfString.add(string.toUpperCase());

            //used on the next three variations
            String subString = string.substring(1);

            //Capitalize string
            variationsOfString.add(string.substring(0, 1).toUpperCase() + subString);

            //nCapitalize string
            variationsOfString.add(string.charAt(0) + subString.toUpperCase());

            //delete first char of string
            variationsOfString.add(subString);

            //remove last char of string
            variationsOfString.add(string.substring(0, string.length() - 1));

            //reverse and reflect string
            variationsOfString.add(revString);
            variationsOfString.add(string + revString);
            variationsOfString.add(revString + string);

            //duplicate string
            variationsOfString.add(string + string);

            //toggle case of the string, e.g., StRiNg or sTrInG.
            StringBuilder toggle1 = new StringBuilder();
            StringBuilder toggle2 = new StringBuilder();
            for (int i = 0; i < lowerString.length(); i++) {

                if (lowerString.charAt(i) >= 'a' && lowerString.charAt(i) <= 'z') {

                    if ((i % 2) == 0) {
                        toggle1.append((char) (lowerString.charAt(i) + 'A' - 'a'));
                        toggle2.append(lowerString.charAt(i));
                    } else {
                        toggle1.append(lowerString.charAt(i));
                        toggle2.append((char) (lowerString.charAt(i) + 'A' - 'a'));
                    }
                } else {
                    toggle1.append(lowerString.charAt(i));
                    toggle2.append(lowerString.charAt(i));
                }

            }
            //add the toggled variations to list
            variationsOfString.add(toggle1.toString());
            variationsOfString.add(toggle2.toString());

            return variationsOfString;
        }
    }

    //only used when checking names (not with dictionary)
    private static List<String> mangleWordAdvance(String string){

        //List is used to store all variations
        List<String> variationsOfString = new ArrayList<>();

        //prepend and append a character to the string, e.g., 0string, string9
        for (char c : charactersAll) {
            variationsOfString.add(c + string);
            variationsOfString.add(string + c);
        }
        return variationsOfString;
    }

    private static List<String> appendChar(String string, char control){
        //List is used to store all variations
        List<String> variationsOfString = new ArrayList<>();

        if(control == 'L'){
            //append a character (a-z) to the string
            for (char c : charactersLow) {
                variationsOfString.add(string + c);
            }
        }
        if(control == 'U'){
            for (char c : charactersUpp) {
                variationsOfString.add(string + c);
            }
        }
        return variationsOfString;
    }

    private static List<String> prependChar(String string, char control){
        //List is used to store all variations
        List<String> variationsOfString = new ArrayList<>();

        if(control == 'L'){
            //prepend a character (a-z) to the string
            for (char c : charactersLow) {
                variationsOfString.add(c + string);
            }
        }
        if(control == 'U'){
            //prepend a character (A-Z) to the string
            for (char c : charactersUpp) {
                variationsOfString.add(c + string);
            }
        }
        return variationsOfString;
    }

    private static List<String> appendPrependNumb(String string){
        //List is used to store all variations
        List<String> variationsOfString = new ArrayList<>();

        //prepend and append a number (0-9) to the string
        for (char c : charactersNumb) {
            variationsOfString.add(c + string);
            variationsOfString.add(string + c);
        }
        return variationsOfString;
    }

}
