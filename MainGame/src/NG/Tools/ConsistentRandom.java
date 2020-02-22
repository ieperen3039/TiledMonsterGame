package NG.Tools;

import java.util.Random;

/**
 * a random number generator that never changes implementation.
 * @author Geert van Ieperen created on 22-2-2020.
 */
public final class ConsistentRandom {
    // currently just use the library
    private final Random random;

    public ConsistentRandom(long seed) {
        random = new Random(seed);
    }

    public void seed(long seed) {
        random.setSeed(seed);
    }

    public float signed() {
        return next() * 2 - 1;
    }

    public float next() {
        return random.nextFloat();
    }

    public float sqSigned() {
        float s = signed();
        return s * s;
    }
}
