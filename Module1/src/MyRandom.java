/*
 * The class is a subclass to javas Random. It tries to implement PRNG using the
 * formula for linear congruential generator:
 *
 *       Xi+1 = (a * Xi  + b) mod m
 *
 * m (a prime number) is pre-chosen, where a is a primitive root to m.
 * This maximizes the period and the generated sequence is uniformly distributed.
 * We have to use very large m and change a, therefore a will consist of an array of primitive roots to m.
 * */

import java.util.Random;

public class MyRandom extends Random {
    //task 2
    final long m = 33029;    // a prime number
    final int a = 899;       // a primitive root to m
    final int b = 0;        // an additive constant
    long mySeed;

    //task 3
    private final int[] S = new int[256];
    private int i = 0;
    private int j = 0;


    //Constructors
    public MyRandom(){}

    public MyRandom(long seed){setSeed(seed);}

    public MyRandom(String seed){initRC4(seed);}


    //The functions that are override from the java Random class

    /*
    // Used for task2
     @Override
     public int next(int bits){

        long nextSeed =((a * mySeed) + b ) % m;
        setSeed(nextSeed);                              // set this.seed to nextSeed
        double r = (double) mySeed / m;                 // r gives us a double between (0,1)
        int mask = ((1 << bits) -1);                    // mask bits
        double y = Math.floor((mask * r) + 1);          // according to the lecture slide
        return (int) y & mask;
    }*/

     // Used for task 2
     @Override
     public void setSeed(long seed){
         mySeed= seed;
    }


    //Used for task 3
    @Override
    public int next(int bits){
        i = (i + 1) % 256;
        j = (j + S[i]) % 256;
        swapValues(S[i], S[j], i, j);
        return S[( (S[i] + S[j]) % 256)];
    }

    //initialization of RC4 used on task 3
    public void initRC4(String seed){

        byte [] key = seed.getBytes();
        for(int x = 0; x < S.length; x++){
            S[x] = x;
        }

        int y = 0;
        for (int x = 0; x < S.length; x++){
            y = (y + S[x] + key[(x % key.length)]) % 256;
            swapValues(S[x], S[y], x, y);
        }

    }

    public void swapValues(int value1, int value2, int pos1, int pos2 ){
        S[pos1] = value2;
        S[pos2] = value1;
    }


}
