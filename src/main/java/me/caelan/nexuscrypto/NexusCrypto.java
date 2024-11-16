package me.caelan.nexuscrypto;

import lombok.Getter;
import me.caelan.nexuscrypto.commands.NexCoinCmd;
import me.caelan.nexuscrypto.hook.PlaceHolderHook;
import me.caelan.nexuscrypto.hook.VaultHook;
import me.caelan.nexuscrypto.manager.EconomyManager;
import me.caelan.nexuscrypto.manager.InvestmentManager;
import me.caelan.nexuscrypto.settings.Configuration;
import me.caelan.nexuscrypto.util.SQLiteHelper;
import me.caelan.nexuscrypto.util.UpdateCrypto;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
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
        setupConfig();
        setupVault();

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

    private void setupVault() {
        if (!VaultHook.setupEconomy()) {
            Bukkit.getLogger().info("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        econ = VaultHook.getEconomy();
    }

    private void startUpdateTask() {
        UpdateCrypto.startNexCryptoValueUpdateTask();
    }
}
