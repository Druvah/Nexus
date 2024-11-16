package me.caelan.nexuscrypto.manager;

import me.caelan.nexuscrypto.NexusCrypto;
import me.caelan.nexuscrypto.settings.Configuration;
import me.caelan.nexuscrypto.util.RandomUtil;
import me.caelan.nexuscrypto.util.SQLiteHelper;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public final class EconomyManager {

    private final Economy economy;
    private final double NEXCRYPTO_TO_USD_RATE;
    private final SQLiteHelper sqliteHelper;
    private double nexCryptoValue;

    public EconomyManager(Economy economy) {
        this.economy = economy;
        this.sqliteHelper = new SQLiteHelper();
        this.NEXCRYPTO_TO_USD_RATE = Configuration.getInstance().getNexCryptoUSDRate();
        this.nexCryptoValue = 1.0; // Default value
    }

    // NexCoin Methods
    public double getNexCoinBalance(Player player) {
        return (player == null) ? 0 : economy.getBalance(player);
    }

    public boolean payNexCoin(Player from, Player to, double amount) {
        if (from == null || to == null || amount <= 0) return false;
        return economy.withdrawPlayer(from, amount).transactionSuccess() &&
                economy.depositPlayer(to, amount).transactionSuccess();
    }

    public boolean addNexCoin(Player player, double amount) {
        return (player != null && amount > 0) &&
                economy.depositPlayer(player, amount).transactionSuccess();
    }

    // NexCrypto Methods
    public double getNexCryptoBalance(Player player) {
        return (player == null) ? 0 : sqliteHelper.getNexCryptoBalance(player.getUniqueId().toString());
    }

    public boolean payNexCrypto(Player from, Player to, double amount) {
        if (from == null || amount <= 0) return false;

        double fromBalance = getNexCryptoBalance(from);
        if (fromBalance < amount) return false;

        if (sqliteHelper.updateNexCryptoBalance(from.getUniqueId().toString(), fromBalance - amount)) {
            if (to != null) {
                double toBalance = getNexCryptoBalance(to);
                sqliteHelper.updateNexCryptoBalance(to.getUniqueId().toString(), toBalance + amount);
            }
            return true;
        }
        return false;
    }

    public boolean addNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) return false;

        double currentBalance = getNexCryptoBalance(player);
        return sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance + amount);
    }

    public boolean subtractNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) return false;

        double currentBalance = getNexCryptoBalance(player);
        if (currentBalance < amount) return false;

        return sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance - amount);
    }

    public boolean sellNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) return false;

        double currentBalance = getNexCryptoBalance(player);
        if (currentBalance < amount) return false;

        double usdAmount = amount * NEXCRYPTO_TO_USD_RATE;
        sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance - amount);
        return economy.depositPlayer(player, usdAmount).transactionSuccess();
    }

    // NexCrypto Value Fluctuation
    public void updateNexCryptoValues() {
        sqliteHelper.getAllPlayerUUIDs().forEach(uuid -> {
            double currentValue = sqliteHelper.getNexCryptoValue(uuid.toString());
            double newValue = calculateNewValue(currentValue);
            sqliteHelper.updateNexCryptoValue(uuid.toString(), newValue);
        });
    }

    private double calculateNewValue(double currentValue) {
        double changePercentage = RandomUtil.getRandomBoolean()
                ? RandomUtil.getRandomDouble(-0.05, 0.05) // -5% to +5%
                : RandomUtil.getRandomDouble(-0.10, 0.10); // -10% to +10%

        // Quarterly change (every 90 days)
        if (System.currentTimeMillis() / (1000L * 60 * 60 * 24) % 90 == 0) {
            changePercentage += (RandomUtil.getRandomBoolean() ? 0.35 : -0.35); // ±35%
        }

        return Math.max(currentValue * (1.0 + changePercentage), 0.01); // Prevent dropping below 0.01
    }

    public double getNexCryptoValue(Player player) {
        return (player == null) ? 0 : sqliteHelper.getNexCryptoValue(player.getUniqueId().toString());
    }

    public void setNexCryptoValue(Player player, double value) {
        if (player != null) {
            sqliteHelper.updateNexCryptoValue(player.getUniqueId().toString(), value);
        }
    }

    public boolean adjustNexCryptoValue(double percentage) {
        double newValue = nexCryptoValue * (1.0 + (percentage / 100.0));
        nexCryptoValue = Math.max(newValue, 0.01);
        return true;
    }
}
