package me.clip.placeholderapi.expansion;

import org.bukkit.OfflinePlayer;

public abstract class PlaceholderExpansion {
    public boolean persist() {return false;}
    public boolean canRegister() {return true;}
    public String getIdentifier() {return "placeholder";}
    public String getAuthor() {return "unknown";}
    public String getVersion() {return "0.0.0";}
    public String onPlaceholderRequest(OfflinePlayer player, String identifier) {return "";}
    public boolean register() {return true;}
}
