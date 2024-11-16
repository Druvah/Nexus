package me.caelan.nexuscrypto.settings;

import lombok.Getter;
import me.caelan.nexuscrypto.NexusCrypto;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public final class Configuration {

    @Getter
    private static final Configuration instance = new Configuration();

    private double baseRate;
    private double volatility;
    private double profitChance;
    private double lossFactor;
    private double nexCryptoUSDRate;

    public Configuration() {
        loadConfiguration();
    }

    public void loadConfiguration() {
        FileConfiguration config = NexusCrypto.getInstance().getConfig();

        config.options().copyDefaults(true);
        NexusCrypto.getInstance().saveDefaultConfig();

        this.baseRate = config.getDouble("investment.baseRate", 1.05);
        this.volatility = config.getDouble("investment.volatilityFactor", 1.0);
        this.profitChance = config.getDouble("investment.profitChance", 0.5);
        this.lossFactor = config.getDouble("investment.lossFactor", 0.5);
        this.nexCryptoUSDRate = config.getDouble("investment.NexCryptoToUSDRate");
    }

    public void saveConfiguration() {
        FileConfiguration config = NexusCrypto.getInstance().getConfig();

        config.set("investment.baseRate", baseRate);
        config.set("investment.volatilityFactor", volatility);
        config.set("investment.profitChance", profitChance);
        config.set("investment.lossFactor", lossFactor);
        config.set("investment.NexCryptoToUSDRate", nexCryptoUSDRate);

        NexusCrypto.getInstance().saveConfig();
    }
}
