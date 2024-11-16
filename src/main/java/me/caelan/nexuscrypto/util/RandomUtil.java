package me.caelan.nexuscrypto.util;

import java.util.Random;

public class RandomUtil {

    private RandomUtil() {
    }

    private static final Random random = new Random();

    public static double getRandomDouble(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    public static boolean getRandomBoolean() {
        return random.nextBoolean();
    }
}
