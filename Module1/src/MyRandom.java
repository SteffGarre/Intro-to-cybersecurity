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

    final long m = 33029;    // a prime number
    final int a = 899;       // a primitive root to m
    final int b = 11;        // an additive constant
    long mySeed;

    //Constructors
    public MyRandom(){

    }

    public MyRandom(long seed){
        setSeed(seed);
    }

    //The two functions that are override from the java Random class
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


}
