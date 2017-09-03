package cc.redberry.libdivide4j;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class Examples {
    @Test
    public void example1() throws Exception {
        long[] someData = {213974, 12334, 3123, 12434};
        long denominator = 45;
        FastDivision.Magic magic = FastDivision.magicSigned(denominator);

        long[] reduced = new long[someData.length];
        for (int i = 0; i < someData.length; ++i) {
            // this is the same as someData[i] / denominator but 3 times faster
            reduced[i] = FastDivision.divideSignedFast(someData[i], magic);
        }
    }

    @Test
    public void example2() throws Exception {
        long largeModulus = Long.MAX_VALUE + 1;
        long someNum = Long.MAX_VALUE + 99999;

        FastDivision.Magic magic = FastDivision.magicUnsigned(largeModulus);
        System.out.println(FastDivision.modUnsignedFast(someNum, magic));
        System.out.println(someNum % largeModulus);
    }

    @Test
    public void example3() throws Exception {
        // multiply mod
        long someModulus = 0x1632faf679feffaeL;
        long a = Integer.MAX_VALUE + 14869869L;
        long b = Integer.MAX_VALUE + 98762346L;

        FastDivision.Magic magic = FastDivision.magic32ForMultiplyMod(someModulus);
        System.out.println(FastDivision.multiplyMod128Unsigned(a, b, someModulus, magic));
    }

    void reduceArrayFast(long[] data, long denominator) {
        FastDivision.Magic magic = FastDivision.magicSigned(denominator);
        for (int i = 0; i < data.length; ++i)
            // computes data[i] / denominator
            data[i] = FastDivision.divideSignedFast(data[i], magic);
    }

    void reduceArraySlow(long[] data, long denominator) {
        for (int i = 0; i < data.length; ++i)
            // computes data[i] / denominator
            data[i] = data[i] / denominator;
    }

    @Test
    public void benchmark() throws Exception {
        Random rnd = new Random();
        int nIterations = 10000;
        //let the JIT to optimize something
        for (int att = 0; att < nIterations; att++) {
            long[] data = new long[1000];
            for (int i = 0; i < data.length; i++)
                data[i] = rnd.nextLong();

            long denominator = rnd.nextLong();

            long[] slow = data.clone();
            long start = System.nanoTime();
            reduceArraySlow(slow, denominator);
            long slowTime = System.nanoTime() - start;


            long[] fast = data.clone();
            start = System.nanoTime();
            reduceArrayFast(fast, denominator);
            long fastTime = System.nanoTime() - start;

            // last 100 timings (JVM warmed up)
            if (att > nIterations - 100) {
                Assert.assertArrayEquals(slow, fast);
                System.out.println("\"/\" operation: " + slowTime);
                System.out.println("Fast division: " + fastTime);
                System.out.println("");
            }
        }
    }
}