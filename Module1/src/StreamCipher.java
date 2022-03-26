public class StreamCipher {

    public static void main(String[] args) {

        try{

            long key = Long.parseLong(args[0]);

        } catch(Exception e){
            System.out.println("Exception catched: " + e);
            System.out.println("System exit! Good bye :(");
            System.exit(-1);
        }
    }
}
