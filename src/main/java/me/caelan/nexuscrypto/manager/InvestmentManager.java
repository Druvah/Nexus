package me.caelan.nexuscrypto.manager;

import me.caelan.nexuscrypto.Investment;
import me.caelan.nexuscrypto.NexusCrypto;
import me.caelan.nexuscrypto.settings.Configuration;
import me.caelan.nexuscrypto.util.Remain;
import me.caelan.nexuscrypto.util.SQLiteHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class InvestmentManager {
    private final Economy economy;
    private final SQLiteHelper sqliteHelper;
    private final EconomyManager economyManager;
    private final Configuration configuration = new Configuration();
    private final Map<UUID, Investment> investments = new HashMap<>();

    public InvestmentManager(Economy economy, SQLiteHelper sqliteHelper, EconomyManager economyManager) {
        this.economy = economy;
        this.sqliteHelper = sqliteHelper;
        this.economyManager = economyManager;
        loadInvestments();
        startInvestmentCheckTask();
    }

    /**
     * Handles investment logic for a player.
     */
    public boolean invest(Player player, double amount, int duration, String currencyType, String message) {
        UUID playerUUID = player.getUniqueId();

        // Validate currency type
        if (!isValidCurrencyType(currencyType)) {
            player.sendMessage(ChatColor.RED + "Unsupported currency type for investment.");
            return false;
        }

        // Check if the player has sufficient balance
        if (!hasSufficientBalance(player, amount, currencyType)) {
            player.sendMessage(ChatColor.RED + "Insufficient balance for investment.");
            return false;
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + duration;

        // Deduct currency based on type
        if ("nexcrypto".equalsIgnoreCase(currencyType)) {
            economyManager.subtractNexCrypto(player, amount);
            sqliteHelper.updateTotalNexCryptoInvested(playerUUID.toString(), amount);
        } else {
            economyManager.subtractNexCrypto(player, amount);
        }

        // Create and store the investment
        Investment investment = new Investment(playerUUID, amount, startTime, endTime, currencyType, message);
        investments.put(playerUUID, investment);
        sqliteHelper.addInvestment(playerUUID.toString(), amount, startTime, endTime, currencyType, message);

        player.sendMessage(ChatColor.AQUA + "Successfully invested " + ChatColor.WHITE + amount + " " + ChatColor.WHITE + currencyType + ChatColor.AQUA + " for " + ChatColor.WHITE + formatDuration(duration) + ".");
        return true;
    }

    /**
     * Returns the total NexCrypto invested by a player.
     */
    public double getTotalNexCryptoInvested(Player player) {
        return sqliteHelper.getTotalNexCryptoInvested(player.getUniqueId().toString());
    }

    /**
     * Validates if the provided currency type is supported.
     */
    private boolean isValidCurrencyType(String currencyType) {
        return "nexcoin".equalsIgnoreCase(currencyType) || "nexcrypto".equalsIgnoreCase(currencyType);
    }

    /**
     * Checks if a player has sufficient balance for the investment.
     */
    private boolean hasSufficientBalance(Player player, double amount, String currencyType) {
        if ("nexcoin".equalsIgnoreCase(currencyType)) {
            return economyManager.getNexCoinBalance(player) >= amount;
        } else {
            return economyManager.getNexCryptoBalance(player) >= amount;
        }
    }

    /**
     * Periodically checks and processes investments.
     */
    public void checkInvestments() {
        Iterator<Map.Entry<UUID, Investment>> iterator = investments.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Investment> entry = iterator.next();
            Investment investment = entry.getValue();

            if (investment.isComplete()) {
                Player player = Bukkit.getPlayer(investment.getPlayerUUID());
                if (player != null) {
                    handleInvestmentCompletion(player, investment);
                }
                sqliteHelper.removeInvestment(investment.getPlayerUUID());
                iterator.remove();
            }
        }
    }

    /**
     * Handles investment completion logic, including profit or loss calculations.
     */
    private void handleInvestmentCompletion(Player player, Investment investment) {
        double result = calculateReturn(investment.getAmount(), investment.getStartTime(), investment.getEndTime());
        double totalAmount = investment.getAmount() + result;
        boolean isProfit = result > 0;

        String currencyType = investment.getCurrencyType();
        String currencyName = ChatColor.YELLOW + currencyType + ChatColor.GOLD;

        if (isProfit) {
            rewardPlayer(player, totalAmount, currencyType);
            player.sendMessage(ChatColor.AQUA + "Your investment of " + ChatColor.WHITE + investment.getAmount() + " " + currencyName + ChatColor.AQUA +
                    " is complete. You earned " + ChatColor.WHITE + result + " " + currencyName + ChatColor.AQUA + ", total returned: " + ChatColor.WHITE + totalAmount + ".");
        } else {
            double lossAmount = Math.abs(result);
            player.sendMessage(ChatColor.AQUA + "Your investment of " + ChatColor.WHITE + investment.getAmount() + " " + currencyName + ChatColor.AQUA +
                    " is complete. Unfortunately, you lost " + ChatColor.RED + lossAmount + " " + currencyName + ChatColor.AQUA + ".");
        }

        sqliteHelper.updateInvestmentResult(investment.getPlayerUUID(), result, isProfit);
    }

    /**
     * Rewards the player with the specified amount of currency.
     */
    private void rewardPlayer(Player player, double amount, String currencyType) {
        if ("nexcoin".equalsIgnoreCase(currencyType)) {
            economyManager.addNexCoin(player, amount);
        } else {
            economyManager.addNexCrypto(player, amount);
        }
        Remain.sendActionBar(player, "&6Profit: " + amount);
    }

    /**
     * Calculates the investment return based on configuration and duration.
     */
    private double calculateReturn(double amount, long startTime, long endTime) {
        long duration = endTime - startTime;
        double baseRate = configuration.getBaseRate();
        double volatility = configuration.getVolatility();

        double profitMultiplier = baseRate + (volatility * (duration / (60.0 * 60.0 * 1000.0)));
        if (Math.random() > configuration.getProfitChance()) {
            return amount * profitMultiplier - amount;
        } else {
            return amount * (1 - (volatility * configuration.getLossFactor())) - amount;
        }
    }

    /**
     * Formats duration into a readable string.
     */
    private String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        if (seconds < 60) return seconds + " seconds";
        if (seconds < 3600) return (seconds / 60) + " minutes";
        if (seconds < 86400) return (seconds / 3600) + " hours";
        return (seconds / 86400) + " days";
    }

    /**
     * Loads investments from the database into memory.
     */
    private void loadInvestments() {
        List<Investment> investmentList = sqliteHelper.getInvestments();
        for (Investment investment : investmentList) {
            investments.put(investment.getPlayerUUID(), investment);
        }
    }

    /**
     * Starts the asynchronous task for periodically checking investments.
     */
    private void startInvestmentCheckTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(NexusCrypto.getInstance(), this::checkInvestments, 20L, 20L);
    }
}
