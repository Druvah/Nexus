package me.caelan.nexuscrypto.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private static Economy econ = null;

    public static boolean setupEconomy() {
        Plugin vaultPlugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");

        if (vaultPlugin == null) {
            Bukkit.getLogger().warning("Vault plugin not found! Economy system will be disabled.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            Bukkit.getLogger().warning("No Economy service found! Economy system will be disabled.");
            return false;
        }

        econ = rsp.getProvider();

        if (econ == null) {
            Bukkit.getLogger().warning("Failed to retrieve the Economy provider from Vault. Economy system will be disabled.");
            return false;
        }
        return true;
    }

    public static Economy getEconomy() {
        return econ;
    }
}
