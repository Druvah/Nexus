package dev.vorzya.nexuscrypto.settings;

import lombok.Getter;
import org.mineacademy.fo.settings.YamlConfig;

@Getter
public final class Configuration extends YamlConfig {

    @Getter
    private static final Configuration instance = new Configuration();

    private double baseRate;
    private double volatility;
    private double profitChance;
    private double lossFactor;
    private double nexCryptoUSDRate;

    public Configuration() {
        loadConfiguration(NO_DEFAULT, "settings.yml");
    }

    @Override
    protected void onLoad() {
        setPathPrefix("investment");
        this.baseRate = getDouble("baseRate", 1.05);
        this.volatility = getDouble("volatilityFactor", 1.0);
        this.profitChance = getDouble("profitChance", 0.5);
        this.lossFactor = getDouble("lossFactor", 0.5);
        this.nexCryptoUSDRate = getDouble("NexCryptoToUSDRate");
    }

    @Override
    protected void onSave() {
        set("baseRate", baseRate);
        set("volatilityFactor", volatility);
        set("profitChance", profitChance);
        set("lossFactor", lossFactor);
        set("NexCryptoToUSDRate", nexCryptoUSDRate);
    }
}
