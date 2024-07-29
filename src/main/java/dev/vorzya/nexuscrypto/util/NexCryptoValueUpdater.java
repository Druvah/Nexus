package dev.vorzya.nexuscrypto.util;

import dev.vorzya.nexuscrypto.NexusCrypto;
import dev.vorzya.nexuscrypto.manager.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class NexCryptoValueUpdater {

    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000; // 1 day in milliseconds
    private static EconomyManager economyManager;

    public static void startNexCryptoValueUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateNexCryptoValues();
            }
        }.runTaskTimerAsynchronously(NexusCrypto.getInstance(), DAY_IN_MILLIS, DAY_IN_MILLIS);
    }

    private static void updateNexCryptoValues() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            double currentNexCryptoValue = economyManager.getNexCryptoValue(player);

            //calculate new value with daily fluctuation
            double newNexCryptoValue = calculateNewValue(currentNexCryptoValue);

            //update value for player
            economyManager.setNexCryptoValue(player, newNexCryptoValue);
        }
    }

    private static double calculateNewValue(double currentValue) {
        //daily fluctuation range: -10% to +10%
        double dailyFluctuation = RandomUtil.getRandomDouble(-0.10, 0.10);

        //quarterly change: +/- 35% every 90 days
        long daysSinceEpoch = System.currentTimeMillis() / DAY_IN_MILLIS;
        if (daysSinceEpoch % 90 == 0) {
            double quarterlyChange = RandomUtil.getRandomBoolean() ? 0.35 : -0.35;
            return currentValue * (1 + quarterlyChange);
        }

        return currentValue * (1 + dailyFluctuation);
    }

    public static void setEconomyManager(EconomyManager manager) {
        economyManager = manager;
    }
}
