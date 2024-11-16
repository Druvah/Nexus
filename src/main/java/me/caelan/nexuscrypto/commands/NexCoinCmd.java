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
import revxrsal.commands.annotation.Default;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class NexCoinCmd {

    private EconomyManager economyManager = NexusCrypto.getInstance().getEconomyManager();
    private InvestmentManager investmentManager = NexusCrypto.getInstance().getInvestmentManager();

    @Command("nexcoin help")
    @CommandPermission("nexcoin.help")
    public void onCommand(Player player) {
        player.sendMessage(ChatColor.AQUA + "---- " + ChatColor.WHITE + "NexCoin Help Menu" + ChatColor.AQUA + " ----");
        player.sendMessage(ChatColor.AQUA + "/nexcoin balance <nexcoin|nexcrypto> " + ChatColor.WHITE + "- View your balance for the specified currency.");
        player.sendMessage(ChatColor.AQUA + "/nexcoin pay " + ChatColor.WHITE + "[player] [amount] - Pay NexCoin to another player.");
        player.sendMessage(ChatColor.AQUA + "/nexcoin add " + ChatColor.WHITE + "[player] [amount] <nexcoin|nexcrypto> - Add currency to a player.");
        player.sendMessage(ChatColor.AQUA + "/nexcoin sell " + ChatColor.WHITE + "[amount] - Sell NexCrypto for USD.");
        player.sendMessage(ChatColor.AQUA + "/nexcoin worth " + ChatColor.WHITE + "- Check the current NexCrypto value.");
        player.sendMessage(ChatColor.AQUA + "/nexcoin invest " + ChatColor.WHITE + "[amount] [duration] - Invest NexCrypto.");
        player.sendMessage(ChatColor.AQUA + "/nexcoin adjustvalue " + ChatColor.WHITE + "[percentage] - Adjust the NexCrypto value.");
        player.sendMessage(ChatColor.AQUA + "-----------------------------------");
    }

    @Command("nexcoin balance")
    @CommandPermission("nexcoin.balance")
    public void handleBalanceCommand(Player player, String currencyType) {
        if (!currencyType.equalsIgnoreCase("nexcoin") && !currencyType.equalsIgnoreCase("nexcrypto")) {
            player.sendMessage(ChatColor.RED + "Invalid currency type. Use 'nexcoin' or 'nexcrypto'.");
            return;
        }

        double balance = currencyType.equalsIgnoreCase("nexcoin")
                ? economyManager.getNexCoinBalance(player)
                : economyManager.getNexCryptoBalance(player);

        player.sendMessage(ChatColor.AQUA + "Your " + currencyType + " balance is: " + ChatColor.WHITE +
                NexCoinUtils.formatCurrency(balance, currencyType));
    }

    @Command("nexcoin pay")
    @CommandPermission("nexcoin.pay")
    public void handlePayCommand(Player player, String targetName, double amount) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.AQUA + "Player " + ChatColor.WHITE + targetName + ChatColor.AQUA + " not found.");
            return;
        }

        if (economyManager.payNexCoin(player, target, amount)) {
            player.sendMessage(ChatColor.AQUA + "Paid " + ChatColor.WHITE +
                    NexCoinUtils.formatCurrency(amount, "NexCoin") + ChatColor.AQUA + " to " + ChatColor.WHITE + target.getName());
            target.sendMessage(ChatColor.AQUA + "Received " + ChatColor.WHITE +
                    NexCoinUtils.formatCurrency(amount, "NexCoin") + ChatColor.AQUA + " from " + ChatColor.WHITE + player.getName());
        } else {
            player.sendMessage(ChatColor.AQUA + "Payment " + ChatColor.WHITE + "failed.");
        }
    }

    @Command("nexcoin add")
    @CommandPermission("nexcoin.add")
    public void handleAddCommand(Player player, String targetName, double amount, String currencyType) {
        if (!currencyType.equalsIgnoreCase("nexcoin") && !currencyType.equalsIgnoreCase("nexcrypto")) {
            player.sendMessage(ChatColor.RED + "Invalid currency type. Use 'nexcoin' or 'nexcrypto'.");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.AQUA + "Player " + ChatColor.WHITE + targetName + ChatColor.AQUA + " not found.");
            return;
        }

        boolean success = currencyType.equalsIgnoreCase("nexcoin")
                ? economyManager.addNexCoin(target, amount)
                : economyManager.addNexCrypto(target, amount);

        if (success) {
            player.sendMessage(ChatColor.AQUA + "Added " + ChatColor.WHITE +
                    NexCoinUtils.formatCurrency(amount, currencyType) + ChatColor.AQUA + " to " + ChatColor.WHITE + target.getName());
            target.sendMessage(ChatColor.AQUA + "You received " + ChatColor.WHITE +
                    NexCoinUtils.formatCurrency(amount, currencyType) + ChatColor.AQUA + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to add " + currencyType + ".");
        }
    }

    @Command("nexcoin sell")
    @CommandPermission("nexcoin.sell")
    public void handleSellCommand(Player player, double amount) {
        if (amount <= 0) {
            player.sendMessage(ChatColor.AQUA + "Amount must be " + ChatColor.WHITE + "greater than zero.");
            return;
        }

        double usdValue = amount * economyManager.getNEXCRYPTO_TO_USD_RATE();
        if (economyManager.sellNexCrypto(player, amount)) {
            player.sendMessage(ChatColor.AQUA + "Sold " + ChatColor.WHITE +
                    NexCoinUtils.formatCurrency(amount, "NexCrypto") + ChatColor.AQUA +
                    ". You received " + ChatColor.WHITE + NexCoinUtils.formatCurrency(usdValue, "USD") + ChatColor.AQUA + ".");
        } else {
            player.sendMessage(ChatColor.AQUA + "Failed to sell NexCrypto.");
        }
    }

    @Command("nexcoin worth")
    @CommandPermission("nexcoin.worth")
    public void handleWorthCommand(Player player) {
        player.sendMessage(ChatColor.AQUA + "The current worth of NexCoin is: " + ChatColor.WHITE +
                Configuration.getInstance().getNexCryptoUSDRate() + ChatColor.AQUA + " USD.");
    }

    @Command("nexcoin invest")
    @CommandPermission("nexcoin.invest")
    public void handleInvestCommand(Player player, double amount, String durationString) {
        String currencyType = "nexcrypto"; // Restrict investment to nexcrypto only (other way around for nexcoin soon)
        int duration = NexCoinUtils.parseDuration(durationString);

        String message = "Investment initiated."; // Default message for investment // TODO

        if (investmentManager.invest(player, amount, duration, currencyType, message)) {
            player.sendMessage(ChatColor.AQUA + "Invested " + ChatColor.WHITE + amount + ChatColor.AQUA +
                    " " + currencyType + " for " + ChatColor.WHITE + NexCoinUtils.formatDuration(duration) + ChatColor.AQUA + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to invest. Please check your balance and try again.");
        }
    }

    @Command("nexcoin adjustvalue")
    @CommandPermission("nexcoin.adjustvalue")
    public void handleAdjustValueCommand(Player player, double percentage) {
        if (economyManager.adjustNexCryptoValue(percentage)) {
            player.sendMessage(ChatColor.AQUA + "NexCoin value adjusted by " + ChatColor.WHITE + percentage + ChatColor.AQUA + "%.");
        } else {
            player.sendMessage(ChatColor.AQUA + "Failed to adjust NexCoin value. Please check the command syntax.");
        }
    }
}
