/*
 * Code written by Stefan Garrido 2022-04
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Documentation used to implement the class:                                                 *
 *  Slides "SymmetricKeyEncryption", from the course IV1013 Introduction to Computer Security  *
 *  Pseudocode for the RC4 implementation: Slides from course mentioned above and the          *
 *  wikipedia article for RC4 (https://en.wikipedia.org/wiki/RC4)                              *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Background for task 2:
 * The class is a subclass to javas Random. It tries to implement PRNG using the
 * formula for linear congruential generator:
 *
 *       Xi+1 = (a * Xi  + b) mod m
 *
 * m (a prime number) is pre-chosen, where a is a primitive root to m. This maximizes the period
 * and the generated sequence is uniformly distributed representing all numbers between [1, m-1].
 * We have to use very large m to have a good randomization.
 * */

import java.math.BigInteger;
import java.util.Random;

public class MyRandom extends Random {

    //task 2
    final long m = 10000019;    // a prime number
    final int a = 2;            // a primitive root to m
    final int b = 0;            // an additive constant
    long mySeed;

    //task 3
    private final int[] S = new int[256];   //array used for the RC4 implementation
    private int i = 0;
    private int j = 0;


    //Constructors
    public MyRandom(){}

    public MyRandom(long seed){setSeed(seed);}

    public MyRandom(String seed){initRC4(seed);}


    /* **** Functions below used for task 2 **** */

    /*
     @Override
     public int next(int bits){

        long nextSeed =((a * mySeed) + b ) % m;
        setSeed(nextSeed);                              // set this.seed to nextSeed
        double r = (double) mySeed / m;                 // r gives us a double between (0,1)
        int mask = ((1 << bits) -1);                    // mask bits
        double y = Math.floor((mask * r) + 1);          // according to the lecture slide
        return (int) y & mask;
    }

     @Override
     public void setSeed(long seed){
         mySeed= seed;
    }
    */


    /* **** Functions below used for task 3 **** */

    //The RC4 Pseudo Random Generator algorithm
    @Override
    public int next(int bits){
        i = ((i + 1) % 256) & 0xFF;
        j = ((j + S[i]) % 256) & 0xFF;
        int mask = ((1 << bits) -1);
        swapValues(S[i], S[j], i, j);
        return S[((S[i] + S[j]) % 256)] & mask;
    }

    //initialization of RC4, using the Key-scheduling algorithm (KSA)
    public void initRC4(String seed){

        //convert the key from  String -> Biginteger -> byte array
        BigInteger seedBigInt = new BigInteger(seed);
        byte [] key = seedBigInt.toByteArray();

        for(int x = 0; x < S.length; x++){
            S[x] = x;
        }
        int y = 0;
        for (int x = 0; x < S.length; x++){
            y = ((y + S[x] + key[(x % key.length)]) % 256) & 0xFF;
            swapValues(S[x], S[y], x, y);
        }
    }

    //function used to swap two values in the S array.
    public void swapValues(int value1, int value2, int pos1, int pos2 ){
        S[pos1] = value2;
        S[pos2] = value1;
    }

}
