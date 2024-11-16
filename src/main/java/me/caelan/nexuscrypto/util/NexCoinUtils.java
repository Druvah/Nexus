package me.caelan.nexuscrypto.util;

import java.text.DecimalFormat;

public class NexCoinUtils {

    public static String formatCurrency(double amount, String currency) {
        DecimalFormat df = new DecimalFormat("#.##");
        return "§a" + df.format(amount) + " " + currency + "§r";
    }

    public static int parseDuration(String durationString) {
        String unit = durationString.substring(durationString.length() - 1);
        int value = Integer.parseInt(durationString.substring(0, durationString.length() - 1));

        switch (unit) {
            case "m":
                return value * 60 * 1000;
            case "h":
                return value * 60 * 60 * 1000;
            case "d":
                return value * 24 * 60 * 60 * 1000;
            case "o":
                return value * 30 * 24 * 60 * 60 * 1000;
            default:
                throw new IllegalArgumentException("Invalid unit.");
        }
    }

    public static String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        if (seconds < 60) {
            return seconds + " seconds";
        } else {
            long minutes = seconds / 60;
            seconds %= 60;
            return minutes + " minutes " + seconds + " seconds";
        }
    }
}
