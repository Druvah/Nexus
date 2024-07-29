package dev.vorzya.nexuscrypto.commands;

import dev.vorzya.nexuscrypto.manager.EconomyManager;
import dev.vorzya.nexuscrypto.manager.InvestmentManager;
import dev.vorzya.nexuscrypto.settings.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

import java.text.DecimalFormat;
import java.util.UUID;

@AutoRegister
public final class NexCoinCmd extends SimpleCommand {

    private static EconomyManager economyManager;
    private static InvestmentManager investmentManager;

    public NexCoinCmd() {
        super("nexcoin");
        setUsage(ChatColor.GOLD + "<balance|pay|add|sell|worth|invest|adjustvalue> [nexcoin|nexcrypto] [player] [amount] [duration|percentage]");
    }

    public static void setEconomyManager(EconomyManager manager) {
        economyManager = manager;
        Bukkit.getLogger().info("EconomyManager has been set in NexCoinCmd");
    }

    public static void setManagers(EconomyManager econManager, InvestmentManager investManager) {
        economyManager = econManager;
        investmentManager = investManager;
        Bukkit.getLogger().info("Managers have been set in NexCoinCmd");
    }

    @Override
    protected void onCommand() {
        checkConsole();
        Player player = getPlayer();

        if (economyManager == null || investmentManager == null) {
            tellError("Managers are not init");
            return;
        }

        if (args.length == 0) {
            tellError("Usage: " + getUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "balance":
                handleBalanceCommand(player);
                break;
            case "pay":
                handlePayCommand(player);
                break;
            case "add":
                handleAddCommand(player);
                break;
            case "sell":
                handleSellCommand(player);
                break;
            case "worth":
                handleWorthCommand(player);
                break;
            case "invest":
                handleInvestCommand(player);
                break;
            case "adjustvalue":
                handleAdjustValueCommand(player);
                break;
            default:
                tellError("Unknown command. Usage: " + getUsage());
                break;
        }
    }

    private void handleBalanceCommand(Player player) {
        if (args.length < 2) {
            tellError("Usage: " + ChatColor.GOLD + "/nexcoin balance <nexcoin|nexcrypto>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "nexcoin":
                double nexCoinBalance = economyManager.getNexCoinBalance(player);
                tell("Your NexCoin balance is: " + formatCurrency(nexCoinBalance, "NexCoin"));
                break;
            case "nexcrypto":
                double nexCryptoBalance = economyManager.getNexCryptoBalance(player);
                tell("Your NexCrypto balance is: " + formatCurrency(nexCryptoBalance, "NexCrypto"));
                break;
            default:
                tellError("Invalid currency type. Usage: " + ChatColor.GOLD + "/nexcoin balance <nexcoin|nexcrypto>");
                break;
        }
    }

    private void handlePayCommand(Player player) {
        if (args.length < 4) {
            tellError("Usage: " + ChatColor.GOLD + "/nexcoin pay <nexcoin|nexcrypto> <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            tellError("Player not found.");
            return;
        }
        double amountToPay;
        try {
            amountToPay = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            tellError("Invalid amount.");
            return;
        }
        if (amountToPay <= 0) {
            tellError("Amount must be greater than zero.");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "nexcoin":
                if (economyManager.payNexCoin(player, target, amountToPay)) {
                    tell("Paid " + formatCurrency(amountToPay, "NexCoin") + " to " + target.getName());
                    target.sendMessage("Received " + formatCurrency(amountToPay, "NexCoin") + " from " + player.getName());
                } else {
                    tellError("Payment failed.");
                }
                break;
            case "nexcrypto":
                if (economyManager.payNexCrypto(player, target, amountToPay)) {
                    tell("Paid " + formatCurrency(amountToPay, "NexCrypto") + " to " + target.getName());
                    target.sendMessage("Received " + formatCurrency(amountToPay, "NexCrypto") + " from " + player.getName());
                } else {
                    tellError("Payment failed. Consider checking your balance.");
                }
                break;
            default:
                tellError("Invalid currency type. Usage: " + ChatColor.GOLD + "/nexcoin pay <nexcoin|nexcrypto> <player> <amount>");
                break;
        }
    }

    private void handleAddCommand(Player player) {
        if (args.length < 4) {
            tellError("Usage: " + ChatColor.GOLD + "/nexcoin add <nexcoin|nexcrypto> <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            tellError("Player not found.");
            return;
        }
        double amountToAdd;
        try {
            amountToAdd = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            tellError("Invalid amount.");
            return;
        }
        if (amountToAdd <= 0) {
            tellError("Amount must be greater than zero.");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "nexcoin":
                if (economyManager.addNexCoin(target, amountToAdd)) {
                    tell("Added " + formatCurrency(amountToAdd, "NexCoin") + " to " + target.getName());
                    target.sendMessage("You received " + formatCurrency(amountToAdd, "NexCoin"));
                } else {
                    tellError("Failed to add NexCoins. Please check the command syntax.");
                }
                break;
            case "nexcrypto":
                if (economyManager.addNexCrypto(target, amountToAdd)) {
                    tell("Added " + formatCurrency(amountToAdd, "NexCrypto") + " to " + target.getName());
                    target.sendMessage("You received " + formatCurrency(amountToAdd, "NexCrypto"));
                } else {
                    tellError("Failed to add NexCrypto.");
                }
                break;
            default:
                tellError("Invalid currency type.");
                break;
        }
    }

    private void handleSellCommand(Player player) {
        if (args.length < 3) {
            tellError("Usage: " + ChatColor.GOLD + "/nexcoin sell <nexcrypto> <amount>");
            return;
        }
        if (!"nexcrypto".equalsIgnoreCase(args[1])) {
            tellError("You can only sell NexCrypto. Usage: " + ChatColor.GOLD + "/nexcoin sell nexcrypto <amount>");
            return;
        }
        double amountToSell;
        try {
            amountToSell = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            tellError("Invalid amount.");
            return;
        }
        if (amountToSell <= 0) {
            tellError("Amount must be greater than zero.");
            return;
        }
        if (economyManager.sellNexCrypto(player, amountToSell)) {
            tell("Sold " + formatCurrency(amountToSell, "NexCrypto") + ". You received " + formatCurrency(amountToSell * economyManager.getNEXCRYPTO_TO_USD_RATE(), "USD"));
        } else {
            tellError("Failed to sell NexCrypto. Please check your balance and try again.");
        }
    }

    private void handleWorthCommand(Player player) {

        tell("The current worth of NexCrypto is: " + Configuration.getInstance().getNexCryptoUSDRate());
    }

    private void handleInvestCommand(Player player) {
        if (args.length < 5) {
            tellError("Usage: " + ChatColor.GOLD + "/nexcoin invest <amount> <duration> <nexcoin|nexcrypto> <message>");
            return;
        }
        double amountToInvest;
        try {
            amountToInvest = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            tellError("Invalid amount. Please enter a valid number.");
            return;
        }

        int duration;
        try {
            duration = parseDuration(args[2]);
        } catch (IllegalArgumentException e) {
            tellError("Invalid duration format. Use 'm' for minutes, 'h' for hours, 'd' for days, and 'mo' for months.");
            return;
        }

        String currencyType = args[3].toLowerCase();
        if (!"nexcoin".equals(currencyType) && !"nexcrypto".equals(currencyType)) {
            tellError("Invalid currency type. Use 'nexcoin' or 'nexcrypto'.");
            return;
        }

        String message = buildMessage(args, 4);

        if (investmentManager.invest(player, amountToInvest, duration, currencyType, message)) {
            tell("&aInvested " + amountToInvest + " " + currencyType + " for " + formatDuration(duration) + ".");
        } else {
            tell("&cFailed to invest. Please check your balance and try again.");
        }
    }

    private void handleInvestmentMaturity(Player player) {
        UUID playerUUID = player.getUniqueId();

        Double result = investmentManager.getInvestmentResult(playerUUID);
        if (result != null) {
            tell("Your investment has matured! You earned: " + formatCurrency(result, "USD"));
            if (result > 0) {
                economyManager.addMoney(player, result);
            } else {
                tell("Unfortunately, you lost: " + formatCurrency(result, "USD"));
            }
        } else {
            tellError("Investment not found or not matured yet.");
        }
    }



    private String buildMessage(String[] args, int start) {
        StringBuilder message = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        return message.toString().trim();
    }

    private int parseDuration(String durationString) {
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
                throw new IllegalArgumentException("Invaalid unit.");
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

    private void handleAdjustValueCommand(Player player) {
        if (args.length < 2) {
            tellError("Usage: " + ChatColor.GOLD + "/nexcoin adjustvalue <percentage>");
            return;
        }
        double percentage;
        try {
            percentage = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            tellError("Invalid percentage.");
            return;
        }
        if (economyManager.adjustNexCryptoValue(percentage)) {
            tell("NexCrypto value adjusted by " + percentage + "%.");
        } else {
            tellError("Failed. Please check the command syntax.");
        }
    }

    private String formatCurrency(double amount, String currency) {
        DecimalFormat df = new DecimalFormat("#.##");
        return ChatColor.GREEN + df.format(amount) + " " + currency + ChatColor.RESET;
    }
}