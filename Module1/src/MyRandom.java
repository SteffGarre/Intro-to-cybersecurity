/*
 * The class is a subclass to javas Random. It tries to implement PRNG using the
 * formula for linear congruential generator:
 *
 *       Xi+1 = (a * Xi  + b) mod m
 *
 * m (a primitive number) is pre-chosen, where a is a primitive root.
 * This maximizes the period and the generated sequence is uniformly distributed.
 *
 * */

import java.util.Random;

public class MyRandom extends Random {


    final long m = 99991;    //a prime number
    final int a = 6;         //a primitive root to m
    final int b = 0;
    long seed;

    //Constructors
    public MyRandom(){
       this.seed = System.nanoTime();
    }

    public MyRandom(long seed){
        System.out.println("MyRandom in use!");
        setSeed(seed);
    }

     @Override
     public int next(int bits){
        System.out.println("MyRandom in use!");
        long nextSeed =((a * seed) + b ) % m;
        double r = (double) seed / m;
        setSeed(nextSeed);
        return ((1 << bits) -1) & (int) Math.floor(((1 << bits) * r) + 1);
    }

     @Override
     public void setSeed(long seed){
        this.seed = seed;
    }


}
