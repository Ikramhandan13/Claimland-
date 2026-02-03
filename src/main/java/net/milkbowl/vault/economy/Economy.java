package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;

public interface Economy {
    double getBalance(OfflinePlayer player);
    EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
    EconomyResponse depositPlayer(OfflinePlayer player, double amount);

    class EconomyResponse {
        public final double amount;
        public final ResponseType type;
        public EconomyResponse(double amount, ResponseType type) {this.amount = amount; this.type = type;}
        public enum ResponseType {SUCCESS, FAILURE}
    }
}
