package com.libdivide;

import org.junit.Test;

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
}