package cc.redberry.libdivide4j;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class FastDivisionTest {

    /**
     * Converts unsigned long to BigInteger
     *
     * @param bits unsigned bits
     * @return BigInteger value of unsigned long
     */
    public static BigInteger valueOfUnsigned(long bits) {
        if (bits >= 0)
            return valueOfSigned(bits);
        int upper = (int) (bits >>> 32);
        int lower = (int) bits;

        // return (upper << 32) + lower
        return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
    }

    /**
     * Converts signed long to BigInteger
     *
     * @param bits signed bits
     * @return BigInteger value of signed long
     */
    public static BigInteger valueOfSigned(long bits) {
        return BigInteger.valueOf(bits);
    }

    static long lowBits(BigInteger num) {
        return num.and(BigInteger.valueOf(0xFFFFFFFFFFFFFFFFL)).longValue();
    }

    static long highBits(BigInteger num) {
        return num.shiftRight(64).longValue();
    }

    protected final RandomGenerator getRandom() {
        return new Well44497a();
    }

    @Test
    public void testMulHighRandom1_unsigned() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < 10000; i++) {

            long x = rnd.nextLong();
            long y = rnd.nextLong();

            long high = FastDivision.multiplyHighUnsigned(x, y);
            long low = FastDivision.multiplyLow(x, y);

            BigInteger num = valueOfUnsigned(y).multiply(valueOfUnsigned(x));
            long highExpected = highBits(num);
            long lowExpected = lowBits(num);

            String errMsg = Long.toHexString(x) + "  " + Long.toHexString(y);
            assertEquals(errMsg, highExpected, high);
            assertEquals(errMsg, lowExpected, low);
        }
    }

    @Test
    public void testMulHighRandom1_signed() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < 10000; i++) {

            long x = rnd.nextLong();
            long y = rnd.nextLong();

            long high = FastDivision.multiplyHighSigned(x, y);
            long low = FastDivision.multiplyLow(x, y);

            BigInteger num = valueOfSigned(y).multiply(valueOfSigned(x));
            long highExpected = highBits(num);
            long lowExpected = lowBits(num);

            String errMsg = Long.toHexString(x) + "  " + Long.toHexString(y);
            assertEquals(errMsg, highExpected, high);
            assertEquals(errMsg, lowExpected, low);
        }
    }

    @Test
    public void testMulHighUnsigned1() throws Exception {
        for (long x : new long[]{-1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE})
            for (long y : new long[]{-1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE}) {
                long high = FastDivision.multiplyHighUnsigned(x, y);
                long low = FastDivision.multiplyLow(x, y);

                BigInteger num = valueOfUnsigned(x).multiply(valueOfUnsigned(y));
                long highExpected = highBits(num);
                long lowExpected = lowBits(num);
                assertEquals(highExpected, high);
                assertEquals(lowExpected, low);
            }
    }

    @Test
    public void testMulHighSigned1() throws Exception {
        for (long x : new long[]{-1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE})
            for (long y : new long[]{-1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE}) {
                long high = FastDivision.multiplyHighSigned(x, y);
                long low = FastDivision.multiplyLow(x, y);

                BigInteger num = valueOfSigned(x).multiply(valueOfSigned(y));
                long highExpected = highBits(num);
                long lowExpected = lowBits(num);
                assertEquals(highExpected, high);
                assertEquals(lowExpected, low);
            }
    }

    @Test
    public void testDivideFastRandom1() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < its(10_000, 1_000_000); i++) {
            long dividend = rnd.nextLong();
            long divider = rnd.nextLong();

            FastDivision.Magic magic;
            magic = FastDivision.magicUnsigned(divider, true);
            Assert.assertEquals(dividend + "/" + divider, valueOfUnsigned(dividend).divide(valueOfUnsigned(divider)).longValue(), FastDivision.divideUnsignedFast(dividend, magic));
            magic = FastDivision.magicUnsigned(divider, false);
            Assert.assertEquals(dividend + "/" + divider, valueOfUnsigned(dividend).divide(valueOfUnsigned(divider)).longValue(), FastDivision.divideUnsignedFast(dividend, magic));

            magic = FastDivision.magicSigned(divider, true);
            Assert.assertEquals(dividend + "/" + divider, dividend / divider, FastDivision.divideSignedFast(dividend, magic));
            magic = FastDivision.magicSigned(divider, false);
            Assert.assertEquals(dividend + "/" + divider, dividend / divider, FastDivision.divideSignedFast(dividend, magic));
        }
    }

    @Test
    public void testDivideFast1() throws Exception {
        long dividend = -1;
        long divider = 324234234L;
        FastDivision.Magic magic = FastDivision.magicUnsigned(divider);

        BigInteger bDividend = valueOfUnsigned(dividend);
        BigInteger bDivider = valueOfUnsigned(divider);

        Assert.assertEquals(bDividend.divide(bDivider).longValue(), FastDivision.divideUnsignedFast(dividend, magic));
    }

    @Test
    public void testDivideFast2() throws Exception {
        for (long dividend : new long[]{-1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE})
            for (long divider : new long[]{Long.MAX_VALUE}) {
                FastDivision.Magic magic = FastDivision.magicSigned(divider, true);
                Assert.assertEquals(dividend + "/" + divider, dividend / divider, FastDivision.divideSignedFast(dividend, magic));
                magic = FastDivision.magicSigned(divider, false);
                Assert.assertEquals(dividend + "/" + divider, dividend / divider, FastDivision.divideSignedFast(dividend, magic));

            }
    }

    @Test
    public void testDivideFast3() throws Exception {
        for (long dividend : new long[]{-1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE})
            for (long divider : new long[]{-1L, Long.MAX_VALUE, Long.MIN_VALUE}) {
                FastDivision.Magic magic = FastDivision.magicUnsigned(divider, true);
                Assert.assertEquals(dividend + "/" + divider, valueOfUnsigned(dividend).divide(valueOfUnsigned(divider)).longValue(), FastDivision.divideUnsignedFast(dividend, magic));
                magic = FastDivision.magicUnsigned(divider, false);
                Assert.assertEquals(dividend + "/" + divider, valueOfUnsigned(dividend).divide(valueOfUnsigned(divider)).longValue(), FastDivision.divideUnsignedFast(dividend, magic));
            }
    }

    @Test
    public void testDivideFast4() throws Exception {
        Assert.assertEquals(1432344, FastDivision.divideSignedFast(1432344, FastDivision.magicSigned(1)));
        Assert.assertEquals(-1432344, FastDivision.divideSignedFast(1432344, FastDivision.magicSigned(-1)));
    }

    @Test
    public void testDivideFast5() throws Exception {
        long dividend = -941192;
        long divider = -8;
        Assert.assertEquals(dividend / divider, FastDivision.divideSignedFast(dividend, FastDivision.magicSigned(divider, true)));
        Assert.assertEquals(dividend / divider, FastDivision.divideSignedFast(dividend, FastDivision.magicSigned(divider, false)));
    }

    @Test
    public void testDivideFast6() throws Exception {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < its(100, 10_000); i++) {
            long dividend = rnd.nextLong();
            for (long sign : new long[]{-1, 1})
                for (int p = 1; p < 55; p++) {
                    long divider = sign * (1L << p);

                    FastDivision.Magic magic;
                    magic = FastDivision.magicUnsigned(divider, true);
                    Assert.assertEquals(dividend + "/" + divider, valueOfUnsigned(dividend).divide(valueOfUnsigned(divider)).longValue(), FastDivision.divideUnsignedFast(dividend, magic));
                    magic = FastDivision.magicUnsigned(divider, false);
                    Assert.assertEquals(dividend + "/" + divider, valueOfUnsigned(dividend).divide(valueOfUnsigned(divider)).longValue(), FastDivision.divideUnsignedFast(dividend, magic));

                    magic = FastDivision.magicSigned(divider, true);
                    Assert.assertEquals(dividend + "/" + divider, dividend / divider, FastDivision.divideSignedFast(dividend, magic));
                    magic = FastDivision.magicSigned(divider, false);
                    Assert.assertEquals(dividend + "/" + divider, dividend / divider, FastDivision.divideSignedFast(dividend, magic));
                }
        }
    }

    @Test
    public void testDivideFast7() throws Exception {
        long dividend = -3188646;
        long divider = -6;
        Assert.assertEquals(dividend / divider, FastDivision.divideSignedFast(dividend, FastDivision.magicSigned(divider, false)));
        Assert.assertEquals(dividend / divider, FastDivision.divideSignedFast(dividend, FastDivision.magicSigned(divider, true)));
    }

    @Test
    public void testDivideFast8() throws Exception {
        long dividend = -108969;
        long divider = -3;
        Assert.assertEquals(dividend / divider, FastDivision.divideSignedFast(dividend, FastDivision.magicSigned(divider, false)));
        Assert.assertEquals(dividend / divider, FastDivision.divideSignedFast(dividend, FastDivision.magicSigned(divider, true)));
    }

    @Test
    public void testMulMod128() {
        RandomGenerator rnd = getRandom();
        for (int i = 0; i < its(10_000, 1_000_000); i++) {
            long a = rnd.nextLong(), b = rnd.nextLong(), modulus;
            do {
                modulus = rnd.nextLong();
            } while (Long.compareUnsigned(modulus, (1L << 62)) >= 0);

            BigInteger expected = valueOfUnsigned(a).multiply(valueOfUnsigned(b)).mod(valueOfUnsigned(modulus));
            FastDivision.Magic magic = FastDivision.magicUnsigned(modulus);
            a = FastDivision.modUnsignedFast(a, magic);
            b = FastDivision.modUnsignedFast(b, magic);
            FastDivision.Magic magic32 = FastDivision.magic32ForMultiplyMod(modulus);
            Assert.assertEquals(a + "*" + b + " mod " + modulus, expected.longValue(), FastDivision.multiplyMod128Unsigned(a, b, modulus, magic32));
        }
    }

    static long[] modulusBenchmarkFast(int n, long[] arr, FastDivision.Magic magic) {
        long r = 0;
        long timing = 0;
        for (int i = 0; i < n; i++) {
            long[] tmp = arr.clone();
            long start = System.nanoTime();
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] = FastDivision.modSignedFast(tmp[j], magic);
                r += tmp[j];
            }
            timing += System.nanoTime() - start;
        }
        return new long[]{timing, r};
    }

    static long[] modulusBenchmarkFast(int n, long[] arr, long divider) {
        long r = 0;
        long timing = 0;
        FastDivision.Magic magic = FastDivision.magicSigned(divider);
        for (int i = 0; i < n; i++) {
            long[] tmp = arr.clone();
            long start = System.nanoTime();
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] = FastDivision.modSignedFast(tmp[j], magic);
                r += tmp[j];
            }
            timing += System.nanoTime() - start;
        }
        return new long[]{timing, r};
    }

    static long[] modulusBenchmarkFast2(int n, long[] arr, long divider) {
        long r = 0;
        long timing = 0;
        Int64 magic = NewInt64(divider);
        for (int i = 0; i < n; i++) {
            long[] tmp = arr.clone();
            long start = System.nanoTime();
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] = mod(magic, tmp[j]);
                r += tmp[j];
            }
            timing += System.nanoTime() - start;
        }
        return new long[]{timing, r};
    }

    static long[] modulusBenchmarkPlain(int n, long[] arr, long modulus) {
        long r = 0;
        long timing = 0;
        for (int i = 0; i < n; i++) {
            long[] tmp = arr.clone();
            long start = System.nanoTime();
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] = Math.floorMod(tmp[j], modulus);
                r += tmp[j];
            }
            timing += System.nanoTime() - start;
        }
        return new long[]{timing, r};
    }


    static final long DIVISOR_111287658L = 111287658L;

    static long[] modulusBenchmarkPlainDivBy111287658(int n, long[] arr) {
        long r = 0;
        long timing = 0;
        for (int i = 0; i < n; i++) {
            long[] tmp = arr.clone();
            long start = System.nanoTime();
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] = Math.floorMod(tmp[j], DIVISOR_111287658L);
                r += tmp[j];
            }
            timing += System.nanoTime() - start;
        }
        return new long[]{timing, r};
    }

    @Test
    public void fastDivisionBenchmark1() throws Exception {
        for (boolean small : new boolean[]{true, false}) {

            RandomGenerator rnd = getRandom();
            DescriptiveStatistics plain = new DescriptiveStatistics(), fast = new DescriptiveStatistics(), fast2 = new DescriptiveStatistics();
            long nIterations = its(20009, 20000);
            for (int i = 0; i < nIterations; i++) {
                if (i == nIterations / 2) {
                    fast.clear();
                    plain.clear();
                }
                long[] arr = new long[1000];
                for (int j = 0; j < arr.length; j++)
                    arr[j] = rnd.nextLong();

                long modulus;
                if (small)
                    do {
                        modulus = rnd.nextLong();
                        modulus = modulus % 100;
                        if (modulus < 0)
                            modulus = -modulus;
                    } while (modulus == 0);
                else {
                    modulus = rnd.nextLong();
                    if (modulus < 0)
                        modulus = -modulus;
                }
                if (modulus == 0 || modulus == 1)
                    modulus = 123;

                assert modulus > 0;

                long[] f = modulusBenchmarkFast(10, arr, modulus);
                long[] f2 = modulusBenchmarkFast2(10, arr, modulus);
                long[] p = modulusBenchmarkPlain(10, arr, modulus);

                assertEquals(f[1], p[1]);
//                assertEquals(f2[1], p[1]);

                fast.addValue(f[0]);
                fast2.addValue(f2[0]);
                plain.addValue(p[0]);
            }

            System.out.println("==== Fast long modulus ====");
            System.out.println("Mean timing: " + fast.getPercentile(50));
            System.out.println("==== Fast2 long modulus ====");
            System.out.println("Mean timing: " + fast2.getPercentile(50));
            System.out.println("==== Plain long modulus ====");
            System.out.println("Mean timing: " + plain.getPercentile(50));
        }
    }

    @Test
    public void fastDivisionBenchmark2() throws Exception {

        RandomGenerator rnd = getRandom();
        DescriptiveStatistics plain = new DescriptiveStatistics(), fast = new DescriptiveStatistics();
        long nIterations = its(2000, 20000);
        for (int i = 0; i < nIterations; i++) {
            if (i == nIterations / 2) {
                fast.clear();
                plain.clear();
            }
            long[] arr = new long[1000];
            for (int j = 0; j < arr.length; j++)
                arr[j] = Math.abs(rnd.nextLong());

            long modulus = DIVISOR_111287658L;
            long[] f = modulusBenchmarkFast(10, arr, FastDivision.magicSigned(modulus));
            long[] p = modulusBenchmarkPlainDivBy111287658(10, arr);

            assertEquals(f[1], p[1]);

            fast.addValue(f[0]);
            plain.addValue(p[0]);
        }

        System.out.println("==== Fast long modulus ====");
        System.out.println("Mean timing: " + fast.getPercentile(50));
        System.out.println("==== JVM inlined compilation-time constant modulus ====");
        System.out.println("Mean timing: " + plain.getPercentile(50));
    }

    /**
     * Whether to run time consuming tests
     *
     * @return whether to run time consuming tests
     */
    private static boolean runHard() {
        String runHard = System.getProperty("runLongTests");
        return Objects.equals(runHard, "") || Objects.equals(runHard, "true");
    }


    /**
     * Returns {@code nSmall} if time-consuming are disabled and {@code nLarge} otherwise
     */
    private static long its(long nSmall, long nLarge) {
        return runHard() ? nLarge : nSmall;
    }


    static final class Int64 {
        final long absd;
        final long hi, lo;
        final boolean neg;

        public Int64(long absd, long hi, long lo, boolean neg) {
            this.absd = absd;
            this.hi = hi;
            this.lo = lo;
            this.neg = neg;
        }
    }

    static long[] Div64(long hi, long lo, long y) {
        long two32 = 1L << 32;
        long mask32 = two32 - 1;
        if (y == 0) {
            throw new RuntimeException();
        }
        if (y <= hi) {
            throw new RuntimeException();
        }

        long s = Long.numberOfLeadingZeros(y);
        y <<= s;

        long yn1 = y >> 32;
        long yn0 = y & mask32;
        long un32 = hi << s | lo >> (64 - s);
        long un10 = lo << s;
        long un1 = un10 >> 32;
        long un0 = un10 & mask32;
        long q1 = Long.divideUnsigned(un32, yn1);
        long rhat = un32 - q1 * yn1;

        for (; q1 >= two32 || q1 * yn0 > two32 * rhat + un1; ) {
            q1--;
            rhat += yn1;
            if (rhat >= two32) {
                break;
            }
        }

        long un21 = un32 * two32 + un1 - q1 * y;
        long q0 = Long.divideUnsigned(un21, yn1);
        rhat = un21 - q0 * yn1;

        for (; q0 >= two32 || q0 * yn0 > two32 * rhat + un0; ) {
            q0--;
            rhat += yn1;
            if (rhat >= two32) {
                break;
            }
        }

        return new long[]{q1 * two32 + q0, (un21 * two32 + un0 - q0 * y) >> s};

    }

    // Add64 returns the sum with carry of x, y and carry: sum = x + y + carry.
// The carry input must be 0 or 1; otherwise the behavior is undefined.
// The carryOut output is guaranteed to be 0 or 1.
    static long[] Add64(long x, long y, long carry) {
        long yc = y + carry;
        long sum = x + yc;
        long carryOut = 0;
        if (sum < x || yc < y) {
            carryOut = 1;
        }
        return new long[]{sum, carryOut};
    }

    static Int64 NewInt64(long d) {
        boolean neg = false;
        if (d < 0) {
            neg = true;
            d = -d;
        }

        long absd = d;
        long hi = Long.divideUnsigned(~0, absd);
        long r = ~0 - absd * Long.divideUnsigned(~0, absd);
        long lo = Div64(r, ~0, absd)[0];


        long c = 1;
        if ((absd & (absd - 1)) == 0) {
            c++;
        }

        long[] loc = Add64(lo, c, 0);
        lo = loc[0];
        c = loc[1];

        hi = Add64(hi, 0, c)[0];
        return new Int64(absd, hi, lo, neg);
    }

    // Mul64 returns the 128-bit product of x and y: (hi, lo) = x * y
// with the product bits' upper half returned in hi and the lower
// half returned in lo.
    static long[] Mul64(long x, long y) {
        long mask32 = 1L << 32 - 1;
        long x0 = x & mask32;
        long x1 = x >> 32;
        long y0 = y & mask32;
        long y1 = y >> 32;
        long w0 = x0 * y0;
        long t = x1 * y0 + w0 >> 32;
        long w1 = t & mask32;
        long w2 = t >> 32;
        w1 += x0 * y1;
        long hi = x1 * y1 + w2 + w1 >> 32;
        long lo = x * y;
        return new long[]{hi, lo};
    }

    static long mod(Int64 d, long n) {
        boolean neg = false;
        if (n < 0) {
            n = -n;
            neg = true;
        }
        long[] hilo = Mul64(d.lo, n);
        long hi = hilo[0], lo = hilo[1];

        hi += d.hi * n;
        long modlo1 = Mul64(lo, d.absd)[0];

        long[] modmodlo2 = Mul64(hi, d.absd);
        long mod = modmodlo2[0];
        long modlo2 = modmodlo2[1];

        long c = Add64(modlo1, modlo2, 0)[1];
        mod = Add64(mod, 0, c)[0];
        if (neg) {
            return -mod;
        }
        return mod;
    }
}