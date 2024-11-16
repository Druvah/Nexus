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
    Configuration configuration = new Configuration();
    private final Map<UUID, Investment> investments = new HashMap<>();

    public InvestmentManager(Economy economy, SQLiteHelper sqliteHelper, EconomyManager economyManager) {
        this.economy = economy;
        this.sqliteHelper = sqliteHelper;
        this.economyManager = economyManager;
        loadInvestments();
        startInvestmentCheckTask();
    }

    public boolean invest(Player player, double amount, int duration, String currencyType, String message) {
        UUID playerUUID = player.getUniqueId();
        if (!isValidCurrencyType(currencyType)) {
            player.sendMessage("Unsupported currency type for investment.");
            return false;
        }

        if (!validateInvestment(player, amount, currencyType)) {
            player.sendMessage("Failed to validate investment. Please check your balance and try again.");
            return false;
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + duration;

        if ("nexcrypto".equalsIgnoreCase(currencyType)) {
            economyManager.subtractNexCrypto(player, amount);
            sqliteHelper.updateTotalNexCryptoInvested(playerUUID.toString(), amount);
        }

        Investment investment = new Investment(playerUUID, amount, startTime, endTime, currencyType, message);
        investments.put(playerUUID, investment);
        sqliteHelper.addInvestment(playerUUID.toString(), amount, startTime, endTime, currencyType, message);

        // player.sendMessage("Successfully invested " + amount + " " + currencyType + " for " + formatDuration(duration) + ".");
        return true;
    }

    public double getTotalNexCryptoInvested(Player player) {
        return sqliteHelper.getTotalNexCryptoInvested(player.getUniqueId().toString());
    }

    private boolean isValidCurrencyType(String currencyType) {
        return "nexcoin".equalsIgnoreCase(currencyType) || "nexcrypto".equalsIgnoreCase(currencyType);
    }

    private boolean validateInvestment(Player player, double amount, String currencyType) {
        if ("nexcoin".equalsIgnoreCase(currencyType)) {
            if (economyManager.getNexCoinBalance(player) < amount) {
                return false;
            }
        } else if ("nexcrypto".equalsIgnoreCase(currencyType)) {
            if (economyManager.getNexCryptoBalance(player) < amount) {
                return false;
            }
        }
        return true;
    }

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

    private void handleInvestmentCompletion(Player player, Investment investment) {
        double result = calculateReturn(investment.getAmount(), investment.getStartTime(), investment.getEndTime());
        double totalAmount = investment.getAmount() + result;
        boolean isProfit = result > 0;

        if (isProfit) {
            if ("nexcoin".equalsIgnoreCase(investment.getCurrencyType())) {
                economyManager.addNexCoin(player, totalAmount);
                player.sendMessage(ChatColor.GOLD + "Your investment of " + ChatColor.GREEN + investment.getAmount() + " " + ChatColor.YELLOW + "NexCoin" + ChatColor.GOLD + " is complete. You earned " + ChatColor.GREEN + result + " NexCoin, total returned: " + ChatColor.GREEN + totalAmount + " NexCoin.");
            } else if ("nexcrypto".equalsIgnoreCase(investment.getCurrencyType())) {
                economyManager.addNexCrypto(player, totalAmount);
                player.sendMessage(ChatColor.GOLD + "Your investment of " + ChatColor.GREEN + investment.getAmount() + " " + ChatColor.YELLOW + "NexCrypto" + ChatColor.GOLD + " is complete. You earned " + ChatColor.GREEN + result + " NexCrypto, total returned: " + ChatColor.GREEN + totalAmount + " NexCrypto.");
                Remain.sendActionBar(player, "&6Profits: " + totalAmount);
            }
        } else {
            double lossAmount = Math.abs(result);
            double remainingAmount = investment.getAmount() - lossAmount;

            if ("nexcoin".equalsIgnoreCase(investment.getCurrencyType())) {
                player.sendMessage(ChatColor.GOLD + "Your investment of " + ChatColor.GREEN + investment.getAmount() + " " + ChatColor.YELLOW + "NexCoin" + ChatColor.GOLD + " is complete. Unfortunately, you lost " + ChatColor.RED + lossAmount + " NexCoin, total returned: " + ChatColor.GREEN + remainingAmount + " NexCoin.");
            } else if ("nexcrypto".equalsIgnoreCase(investment.getCurrencyType())) {
                player.sendMessage(ChatColor.GOLD + "Your investment of " + ChatColor.GREEN + investment.getAmount() + " " + ChatColor.YELLOW + "NexCrypto" + ChatColor.GOLD + " is complete. Unfortunately, you lost " + ChatColor.RED + lossAmount + " NexCrypto, total returned: " + ChatColor.GREEN + remainingAmount + " NexCrypto.");
                Remain.sendActionBar(player, "&6Loss: " + lossAmount);
            }
        }

        sqliteHelper.updateInvestmentResult(investment.getPlayerUUID(), result, isProfit);
    }

    private double calculateReturn(double amount, long startTime, long endTime) {
        long duration = endTime - startTime;
        double baseRate = configuration.getBaseRate();
        double volatility = configuration.getVolatility();

        double profitMultiplier = baseRate + (volatility * (duration / (60 * 60 * 1000)));
        if (Math.random() > configuration.getProfitChance()) {
            return amount * profitMultiplier - amount;
        } else {
            return amount * (1 - (volatility * configuration.getLossFactor())) - amount;
        }
    }

    public Double getInvestmentResult(UUID playerUUID) {
        Investment investment = investments.get(playerUUID);
        if (investment != null && investment.isComplete()) {
            return calculateReturn(investment.getAmount(), investment.getStartTime(), investment.getEndTime());
        }
        return null;
    }

    public boolean isProfit(UUID playerUUID) {
        Double result = getInvestmentResult(playerUUID);
        return result != null && result > 0;
    }

    public void removeInvestment(UUID playerUUID) {
        investments.remove(playerUUID);
        sqliteHelper.removeInvestment(playerUUID);
    }

    private void startInvestmentCheckTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(NexusCrypto.getInstance(), this::checkInvestments, 20L, 20L);
    }

    private void loadInvestments() {
        List<Investment> investmentList = sqliteHelper.getInvestments();
        for (Investment investment : investmentList) {
            investments.put(investment.getPlayerUUID(), investment);
        }
    }

    private String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return seconds / 60 + " minutes";
        } else if (seconds < 86400) {
            return seconds / 3600 + " hours";
        } else if (seconds < 2592000) {
            return seconds / 86400 + " days";
        } else {
            return seconds / (30 * 86400) + " months";
        }
    }
}
