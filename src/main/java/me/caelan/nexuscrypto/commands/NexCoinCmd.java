package me.caelan.nexuscrypto.commands;

import me.caelan.nexuscrypto.NexusCrypto;
import me.caelan.nexuscrypto.manager.EconomyManager;
import me.caelan.nexuscrypto.manager.InvestmentManager;
import me.caelan.nexuscrypto.settings.Configuration;
import me.caelan.nexuscrypto.util.NexCoinUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public final class NexCoinCmd {

    private EconomyManager economyManager = NexusCrypto.getInstance().getEconomyManager();
    private InvestmentManager investmentManager = NexusCrypto.getInstance().getInvestmentManager();

    @Command("nexcoin")
    public void onCommand(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /nexcoin <balance|pay|add|sell|worth|invest|adjustvalue>");
    }

    @Command("nexcoin balance")
    public void handleBalanceCommand(Player player) {
        player.sendMessage("Your NexCoin balance is: " + NexCoinUtils.formatCurrency(economyManager.getNexCoinBalance(player), "NexCoin"));
    }

    @Command("nexcoin pay")
    public void handlePayCommand(Player player, String targetName, double amount) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (economyManager.payNexCoin(player, target, amount)) {
            player.sendMessage("Paid " + NexCoinUtils.formatCurrency(amount, "NexCoin") + " to " + target.getName());
            target.sendMessage("Received " + NexCoinUtils.formatCurrency(amount, "NexCoin") + " from " + player.getName());
        } else {
            player.sendMessage(ChatColor.RED + "Payment failed.");
        }
    }

    @Command("nexcoin add")
    public void handleAddCommand(Player player, String targetName, double amount) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (economyManager.addNexCoin(target, amount)) {
            player.sendMessage("Added " + NexCoinUtils.formatCurrency(amount, "NexCoin") + " to " + target.getName());
            target.sendMessage("You received " + NexCoinUtils.formatCurrency(amount, "NexCoin"));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to add NexCoins.");
        }
    }

    @Command("nexcoin sell")
    public void handleSellCommand(Player player, double amount) {
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be greater than zero.");
            return;
        }

        if (economyManager.sellNexCrypto(player, amount)) {
            player.sendMessage("Sold " + NexCoinUtils.formatCurrency(amount, "NexCrypto") + ". You received " + NexCoinUtils.formatCurrency(amount * economyManager.getNEXCRYPTO_TO_USD_RATE(), "USD"));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to sell NexCrypto.");
        }
    }

    @Command("nexcoin worth")
    public void handleWorthCommand(Player player) {
        player.sendMessage("The current worth of NexCrypto is: " + Configuration.getInstance().getNexCryptoUSDRate());
    }

    @Command("nexcoin invest")
    public void handleInvestCommand(Player player, double amount, String durationString, String currency, String message) {
        int duration = NexCoinUtils.parseDuration(durationString);
        if (investmentManager.invest(player, amount, duration, currency, message)) {
            player.sendMessage("Invested " + amount + " " + currency + " for " + NexCoinUtils.formatDuration(duration) + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to invest. Please check your balance and try again.");
        }
    }

    @Command("nexcoin adjustvalue")
    public void handleAdjustValueCommand(Player player, double percentage) {
        if (economyManager.adjustNexCryptoValue(percentage)) {
            player.sendMessage("NexCrypto value adjusted by " + percentage + "%.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed. Please check the command syntax.");
        }
    }
}
