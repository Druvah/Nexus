package me.caelan.nexuscrypto;

import lombok.Getter;
import me.caelan.nexuscrypto.commands.NexCoinCmd;
import me.caelan.nexuscrypto.hook.PlaceHolderHook;
import me.caelan.nexuscrypto.manager.EconomyManager;
import me.caelan.nexuscrypto.manager.InvestmentManager;
import me.caelan.nexuscrypto.settings.Configuration;
import me.caelan.nexuscrypto.util.SQLiteHelper;
import me.caelan.nexuscrypto.util.UpdateCrypto;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

@Getter
public final class NexusCrypto extends JavaPlugin {

    @Getter
    private static NexusCrypto instance;
    private static Economy econ = null;
    private EconomyManager economyManager;
    private InvestmentManager investmentManager;
    private SQLiteHelper sqliteHelper;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().warning("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupConfig();

        sqliteHelper = new SQLiteHelper();
        economyManager = new EconomyManager(econ);
        investmentManager = new InvestmentManager(econ, sqliteHelper, economyManager);
        PlaceHolderHook.registerHook(economyManager, investmentManager, sqliteHelper);
        registerCommands();
        startUpdateTask();
    }


    @Override
    public void onDisable() {
        Configuration.getInstance().saveConfiguration();
    }

    private void registerCommands() {
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new NexCoinCmd());
    }

    private void setupConfig() {
        Configuration config = Configuration.getInstance();
        config.loadConfiguration();
    }

    private void startUpdateTask() {
        UpdateCrypto.startNexCryptoValueUpdateTask();
    }

    private boolean setupEconomy() {
        Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");

        if (vaultPlugin == null) {
            getLogger().warning("Vault plugin not found! Make sure it is installed.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No Economy service found from Vault. Make sure you have EssentialsX or any other plugin for it installed.");
            return false;
        }

        econ = rsp.getProvider();

        if (econ == null) {
            return false;
        }

        getLogger().info("Successfully hooked into Vault Economy.");
        return true;
    }

}
