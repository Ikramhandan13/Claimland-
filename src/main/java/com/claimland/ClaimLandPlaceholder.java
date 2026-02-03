package com.claimland;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class ClaimLandPlaceholder extends PlaceholderExpansion {
    private final ClaimLandPlugin plugin;

    public ClaimLandPlaceholder(ClaimLandPlugin plugin) {this.plugin = plugin;}

    public boolean persist() {return true;}
    public boolean canRegister() {return true;}
    public String getIdentifier() {return "claimland";}
    public String getAuthor() {return "DZAKIRI";}
    public String getVersion() {return "0.1.0";}

    public String onPlaceholderRequest(OfflinePlayer player, String identifier) {
        if (player == null) return "";
        switch (identifier) {
            case "land_count":
                return String.valueOf(plugin.getLandManager().countByOwner(player.getUniqueId()));
            case "land_max":
                var cfg = plugin.getConfig();
                return String.valueOf(cfg.getInt("ranks.default.max-lands", 3));
            default:
                return "";
        }
    }
}
