package dev.vorzya.nexuscrypto;

import dev.vorzya.nexuscrypto.commands.NexCoinCmd;
import dev.vorzya.nexuscrypto.hook.PlaceHolderHook;
import dev.vorzya.nexuscrypto.manager.EconomyManager;
import dev.vorzya.nexuscrypto.manager.InvestmentManager;
import dev.vorzya.nexuscrypto.util.SQLiteHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;

public final class NexusCrypto extends SimplePlugin {

    private static Economy econ = null;
    private EconomyManager economyManager;
    private InvestmentManager investmentManager;
    private SQLiteHelper sqliteHelper;

    @Override
    protected void onPluginStart() {
        if (!setupEconomy()) {
            Common.log("&cDisabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        sqliteHelper = new SQLiteHelper();

        economyManager = new EconomyManager(econ);
        investmentManager = new InvestmentManager(econ, sqliteHelper, economyManager);

        PlaceHolderHook.registerHook(economyManager, investmentManager, sqliteHelper);

        NexCoinCmd.setManagers(economyManager, investmentManager);
        Common.log("&aManagers have been set in NexCoinCmd!");


        Common.log("&aNexusCrypto has been enabled!");

    }

    @Override
    protected void onPluginStop() {
        Common.log("&aNexusCrypto has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static boolean hasEconomy() {
        return econ != null;
    }

    public static String formatCurrencySymbol(double amount) {
        if (!hasEconomy())
            throw new UnsupportedOperationException("Vault not found.");

        return econ.format(amount);
        //return amount + " " + (((int) amount) == 1 ? economy.currencyNameSingular() : economy.currencyNamePlural());
    }

}
