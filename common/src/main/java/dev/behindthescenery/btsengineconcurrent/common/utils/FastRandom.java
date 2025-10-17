package dev.behindthescenery.btsengineconcurrent.common.utils;

public class FastRandom {

    private int s0, s1, s2, s3;

    public FastRandom(long seed) {
        setSeed(seed);
    }

    public void setSeed(long seed) {
        long z = seed + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= (z >>> 31);

        s0 = (int) z;
        s1 = (int) (z >>> 32);
        s2 = s0 ^ 0x9E3779B9;
        s3 = s1 ^ 0x243F6A88;
    }

    private int nextInt32() {
        int t = s1 << 9;
        int result = Integer.rotateLeft(s0 + s3, 7) + s0;
        s2 ^= s0;
        s3 ^= s1;
        s1 ^= s2;
        s0 ^= s3;
        s2 ^= t;
        s3 = Integer.rotateLeft(s3, 11);
        return result;
    }

    public int nextInt() {
        return nextInt32() & 0x7FFFFFFF;
    }

    public int nextInt(int bound) {
        int r = nextInt32() >>> 1;
        return r % bound;
    }

    public float nextFloat() {
        return (nextInt32() >>> 8) * 0x1.0p-24f;
    }

    public double nextDouble() {
        long hi = (long)(nextInt32() >>> 6);
        long lo = (long)(nextInt32() >>> 5);
        return (hi << 27 | lo) * 0x1.0p-53;
    }

    public long nextLong() {
        long hi = nextInt32() & 0xFFFFFFFFL;
        long lo = nextInt32() & 0xFFFFFFFFL;
        return (hi << 32) ^ lo;
    }

    private static final ThreadLocal<FastRandom> LOCAL =
            ThreadLocal.withInitial(() -> new FastRandom(System.nanoTime() ^ Thread.currentThread().getId()));

    public static FastRandom current() {
        return LOCAL.get();
    }

    public static FastRandom currentWithSeed(long seed) {
        FastRandom random = LOCAL.get();
        random.setSeed(seed);
        return random;
    }
}
