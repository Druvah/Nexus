package me.caelan.nexuscrypto.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Remain {

    public static void sendActionBar(Player player, String message) {
        String version = Bukkit.getVersion();

        if (version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12") || version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16") || version.contains("1.17") || version.contains("1.18") || version.contains("1.19") || version.contains("1.20") || version.contains("1.21")) {
            // From 1.8 onwards, we use the Bungee API to send the action bar
            sendActionBarSpigot(player, message);
        } else {
            // For versions before 1.8, use legacy method (for 1.7 and earlier)
            sendActionBarLegacy(player, message);
        }
    }

    // Using Spigot API for 1.8 and newer versions
    private static void sendActionBarSpigot(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorize(message)));
    }

    // Using the legacy method for older versions (1.7 and below)
    private static void sendActionBarLegacy(Player player, String message) {
        player.sendMessage(colorize(message));
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
