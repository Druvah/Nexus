package dev.vorzya.nexuscrypto.manager;

import dev.vorzya.nexuscrypto.settings.Configuration;
import dev.vorzya.nexuscrypto.util.RandomUtil;
import dev.vorzya.nexuscrypto.util.SQLiteHelper;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public final class EconomyManager {

    private final Economy economy;
    private final double NEXCRYPTO_TO_USD_RATE;
    private final SQLiteHelper sqliteHelper;
    private double nexCryptoValue;

    public EconomyManager(Economy economy) {
        this.economy = economy;
        this.sqliteHelper = new SQLiteHelper();
        this.NEXCRYPTO_TO_USD_RATE = Configuration.getInstance().getNexCryptoUSDRate();
        this.nexCryptoValue = 1.0; //def
    }


    //nexcoin
    public double getNexCoinBalance(Player player) {
        if (player == null) {
            return 0;
        }
        return economy.getBalance(player);
    }

    public boolean payNexCoin(Player from, Player to, double amount) {
        if (from == null || to == null) {
            return false;
        }
        return economy.withdrawPlayer(from, amount).transactionSuccess() &&
                economy.depositPlayer(to, amount).transactionSuccess();
    }

    public boolean addNexCoin(Player player, double amount) {
        if (player == null) {
            return false;
        }
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean addMoney(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    //nexcrypto
    public double getNexCryptoBalance(Player player) {
        if (player == null) {
            return 0;
        }
        return sqliteHelper.getNexCryptoBalance(player.getUniqueId().toString());
    }

    public boolean payNexCrypto(Player from, Player to, double amount) {
        if (from == null || amount <= 0) {
            return false;
        }

        double fromBalance = getNexCryptoBalance(from);
        if (fromBalance < amount) {
            return false;
        }

        if (sqliteHelper.updateNexCryptoBalance(from.getUniqueId().toString(), fromBalance - amount)) {
            if (to != null) {
                double toBalance = getNexCryptoBalance(to);
                sqliteHelper.updateNexCryptoBalance(to.getUniqueId().toString(), toBalance + amount);
            }
            return true;
        }
        return false;
    }

    public boolean addNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }
        double currentBalance = getNexCryptoBalance(player);
        return sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance + amount);
    }

    public boolean subtractNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }
        double currentBalance = getNexCryptoBalance(player);
        if (currentBalance < amount) {
            return false;
        }
        return sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance - amount);
    }

    public boolean sellNexCrypto(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }
        double currentBalance = getNexCryptoBalance(player);
        if (currentBalance < amount) {
            return false;
        }
        double usdAmount = amount * NEXCRYPTO_TO_USD_RATE;
        sqliteHelper.updateNexCryptoBalance(player.getUniqueId().toString(), currentBalance - amount);
        return economy.depositPlayer(player, usdAmount).transactionSuccess();
    }

    //nexcrypto fluctuation
    public void updateNexCryptoValues() {
        for (UUID uuid : sqliteHelper.getAllPlayerUUIDs()) {
            double currentValue = sqliteHelper.getNexCryptoValue(uuid.toString());
            double newValue = calculateNewValue(currentValue);
            sqliteHelper.updateNexCryptoValue(uuid.toString(), newValue);
        }
    }

    private double calculateNewValue(double currentValue) {
        double changePercentage;

        //50% chance of increase or decrease with different ranges
        if (RandomUtil.getRandomBoolean()) {
            changePercentage = RandomUtil.getRandomDouble(-0.05, 0.05); //random change between -5% to +5%
        } else {
            changePercentage = RandomUtil.getRandomDouble(-0.10, 0.10); //random change between -10% to +10%
        }

        // Quarterly change
        long daysSinceEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        long daysSinceStart = daysSinceEpoch % 90;
        if (daysSinceStart == 0) {
            //every 90 days a change of 35%
            if (RandomUtil.getRandomBoolean()) {
                changePercentage += 0.35; //35% increase
            } else {
                changePercentage -= 0.35; //35% decrease
            }
        }

        double newValue = currentValue * (1.0 + changePercentage);
        return Math.max(newValue, 0.01); //value will not drop below 0.01
    }

    public double getNexCryptoValue(Player player) {
        if (player == null) {
            return 0;
        }
        return sqliteHelper.getNexCryptoValue(player.getUniqueId().toString());
    }

    public void setNexCryptoValue(Player player, double value) {
        if (player == null) {
            return;
        }
        sqliteHelper.updateNexCryptoValue(player.getUniqueId().toString(), value);
    }

    public boolean adjustNexCryptoValue(double percentage) {
        double currentValue = nexCryptoValue;
        double newValue = currentValue * (1.0 + (percentage / 100.0));
        nexCryptoValue = Math.max(newValue, 0.01);
        return true;
    }
}
