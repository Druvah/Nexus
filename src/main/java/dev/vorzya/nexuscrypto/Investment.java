package dev.vorzya.nexuscrypto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Investment {

    private final UUID playerUUID;
    private final double amount;
    private final long startTime;
    private final long endTime;
    private final String currencyType;
    private final String message;

    public Investment(UUID playerUUID, double amount, long startTime, long endTime, String currencyType, String message) {
        this.playerUUID = playerUUID;
        this.amount = amount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currencyType = currencyType;
        this.message = message;
    }

    public boolean isComplete() {
        return System.currentTimeMillis() >= endTime;
    }
}
