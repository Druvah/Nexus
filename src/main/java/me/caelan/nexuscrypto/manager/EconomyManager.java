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
    private final SQLiteHelper sqliteHelper; // Helper for database interactions
    private double nexCryptoValue; // The current value of NexCrypto

    // Constructor for the EconomyManager
    public EconomyManager(Economy economy) {
        this.economy = economy;
        this.sqliteHelper = NexusCrypto.getInstance().getSqliteHelper();
        this.NEXCRYPTO_TO_USD_RATE = Configuration.getInstance().getNexCryptoUSDRate();
        this.nexCryptoValue = 0.0;
    }

    // NexCoin Methods
    /**
     * Gets the NexCoin balance for a player.
     * @param player the player whose balance is to be retrieved
     * @return the NexCoin balance of the player
     */
    public double getNexCoinBalance(Player player) {
        return (player == null) ? 0 : economy.getBalance(player); // Returns 0 if player is null
    }

    /**
     * Transfers NexCoin from one player to another.
     * @param from the player sending the NexCoin
     * @param to the player receiving the NexCoin
     * @param amount the amount to be transferred
     * @return true if the transaction is successful, false otherwise
     */
    public boolean payNexCoin(Player from, Player to, double amount) {
        if (from == null || to == null || amount <= 0) return false;
        return economy.withdrawPlayer(from, amount).transactionSuccess() &&
                economy.depositPlayer(to, amount).transactionSuccess();
    }

    /**
     * Adds NexCoin to a player's balance.
     * @param player the player to whom NexCoin will be added
     * @param amount the amount of NexCoin to add
     * @return true if the operation is successful, false otherwise
     */
    public boolean addNexCoin(Player player, double amount) {
        return (player != null && amount > 0) &&
                economy.depositPlayer(player, amount).transactionSuccess();
    }

    // NexCrypto Methods
    /**
     * Gets the NexCrypto balance for a player from the SQLite database.
     * @param player the player whose NexCrypto balance is to be retrieved
     * @return the NexCrypto balance of the player
     */
    public double getNexCryptoBalance(Player player) {
        return (player == null) ? 0 : sqliteHelper.getNexCryptoBalance(player.getUniqueId().toString());
    }

    /**
     * Transfers NexCrypto from one player to another.
     * @param from the player sending the NexCrypto
     * @param to the player receiving the NexCrypto
     * @param amount the amount to be transferred
     * @return true if the transaction is successful, false otherwise
     */
    public boolean payNexCrypto(Player from, Player to, double amount) {
        if (from == null || amount <= 0) return false;

        double fromBalance = getNexCryptoBalance(from);
        if (fromBalance < amount) return false;

        if (sqliteHelper.updateNexCryptoBalance(from.getUniqueId().toString(), fromBalance - amount)) {
            if (to != null) {
                double toBalance = getNexCryptoBalance(to);
                sqliteHelper.updateNexCryptoBalance(to.getUniqueId().toString(), toBalance + amount); // Update the receiver's balance
            }
            return true;
        }
        return false;
    }

    /**
     * Adds NexCrypto to a player's balance in the SQLite database.
     * @param player the player to whom NexCrypto will be added
     * @param amount the amount of NexCrypto to add
     * @return true if the operation is successful, false otherwise
     */
    public boolean addNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) return false;

        double currentBalance = getNexCryptoBalance(player);
        return sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance + amount);
    }

    /**
     * Subtracts NexCrypto from a player's balance in the SQLite database.
     * @param player the player from whom NexCrypto will be subtracted
     * @param amount the amount of NexCrypto to subtract
     * @return true if the operation is successful, false otherwise
     */
    public boolean subtractNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) return false;

        double currentBalance = getNexCryptoBalance(player);
        if (currentBalance < amount) return false;

        return sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance - amount);
    }

    /**
     * Allows a player to sell their NexCrypto for USD.
     * @param player the player selling NexCrypto
     * @param amount the amount of NexCrypto to sell
     * @return true if the transaction is successful, false otherwise
     */
    public boolean sellNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) return false;

        double currentBalance = getNexCryptoBalance(player);
        if (currentBalance < amount) return false;

        double usdAmount = amount * NEXCRYPTO_TO_USD_RATE; // Convert NexCrypto to USD
        sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance - amount);
        return economy.depositPlayer(player, usdAmount).transactionSuccess();
    }

    // NexCrypto Value Fluctuation
    /**
     * Updates the NexCrypto values for all players based on some random fluctuation logic.
     */
    public void updateNexCryptoValues() {
        sqliteHelper.getAllPlayerUUIDs().forEach(uuid -> {
            double currentValue = sqliteHelper.getNexCryptoValue(uuid.toString());
            double newValue = calculateNewValue(currentValue);
            sqliteHelper.updateNexCryptoValue(uuid.toString(), newValue);
        });
    }

    /**
     * Calculates a new value for NexCrypto based on a random fluctuation.
     * @param currentValue the current value of NexCrypto for a player
     * @return the new value after fluctuation
     */
    private double calculateNewValue(double currentValue) {
        double changePercentage = RandomUtil.getRandomBoolean()
                ? RandomUtil.getRandomDouble(-0.05, 0.05) // -5% to +5%
                : RandomUtil.getRandomDouble(-0.10, 0.10); // -10% to +10%

        // Apply quarterly change (every 90 days)
        if (System.currentTimeMillis() / (1000L * 60 * 60 * 24) % 90 == 0) {
            changePercentage += (RandomUtil.getRandomBoolean() ? 0.35 : -0.35); // +-35% quarterly fluctuation
        }

        return Math.max(currentValue * (1.0 + changePercentage), 0.01);
    }

    /**
     * Gets the NexCrypto value for a player.
     * @param player the player whose NexCrypto value is to be retrieved
     * @return the NexCrypto value of the player
     */
    public double getNexCryptoValue(Player player) {
        return (player == null) ? 0 : sqliteHelper.getNexCryptoValue(player.getUniqueId().toString());
    }

    /**
     * Sets the NexCrypto value for a player.
     * @param player the player whose NexCrypto value is to be set
     * @param value the new value to set
     */
    public void setNexCryptoValue(Player player, double value) {
        if (player != null) {
            sqliteHelper.updateNexCryptoValue(player.getUniqueId().toString(), value);
        }
    }

    /**
     * Adjusts the global NexCrypto value by a percentage.
     * @param percentage the percentage by which to adjust the NexCrypto value
     * @return true if the adjustment is successful
     */
    public boolean adjustNexCryptoValue(double percentage) {
        double newValue = nexCryptoValue * (1.0 + (percentage / 100.0));
        nexCryptoValue = Math.max(newValue, 0.01);
        return true;
    }
}
