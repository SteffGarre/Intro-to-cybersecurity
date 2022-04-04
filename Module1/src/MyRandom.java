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


    final long m = 99991;    //a prime number
    final int a = 6;
    //final int [] primRootArray = {6, 153, 277, 417, 486, 922, 1292, 1754, 7906, 13116,
    //                22473, 29291, 48911, 58376, 65856, 72577, 37121, 9477, 83744, 96962 };   // primitive roots to m
    final int b = 0;
    long seed;
    //Random random = new Random();

    //Constructors
    public MyRandom(){
       this.seed = System.nanoTime();
    }

    public MyRandom(long seed){
        setSeed(seed);
    }

     @Override
     public int next(int bits){

        //int randPos = random.nextInt(20);         // chooses a random pos in array a[]
        //long nextSeed =((a[randPos] * seed) + b ) % m;  // implements the PRNG according to the formula above
        long nextSeed =((a * seed) + b ) % m;
        double r = (double) seed / m;                   // r gives us a double between (0,1)
        setSeed(nextSeed);                              // set this.seed to nextSeed

        return ((1 << bits) -1) & (int) Math.floor(((1 << bits) * r) + 1);
    }

     @Override
     public void setSeed(long seed){
        this.seed = seed;
    }


}
