package me.caelan.nexuscrypto.hook;

import me.caelan.nexuscrypto.manager.EconomyManager;
import me.caelan.nexuscrypto.manager.InvestmentManager;
import me.caelan.nexuscrypto.util.SQLiteHelper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceHolderHook extends PlaceholderExpansion {

    private final EconomyManager economyManager;
    private final InvestmentManager investmentManager;
    private final SQLiteHelper sqLiteHelper;

    public PlaceHolderHook(EconomyManager economyManager, InvestmentManager investmentManager, SQLiteHelper sqLiteHelper) {
        this.economyManager = economyManager;
        this.investmentManager = investmentManager;
        this.sqLiteHelper = sqLiteHelper;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nexus";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Vorzya";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer != null && offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();

            if (player != null) {
                try {
                    switch (params.toLowerCase()) {
                        case "nexcoin_balance":
                            return String.valueOf(economyManager.getNexCoinBalance(player));
                        case "nexcrypto_balance":
                            return String.valueOf(economyManager.getNexCryptoBalance(player));
                        case "nexcrypto_invested":
                            return String.valueOf(investmentManager.getTotalNexCryptoInvested(player));
                        case "nexcrypto_worth":
                            return String.valueOf(economyManager.getNEXCRYPTO_TO_USD_RATE());
                        default:
                            return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Coudlnt fetch";
                }
            }
        }
        return null;
    }

    public static void registerHook(EconomyManager economyManager, InvestmentManager investmentManager, SQLiteHelper sqLiteHelper) {
        new PlaceHolderHook(economyManager, investmentManager, sqLiteHelper).register();
    }
}
