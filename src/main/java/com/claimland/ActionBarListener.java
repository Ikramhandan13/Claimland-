package com.claimland;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Bukkit;

public class ActionBarListener implements Listener {
    private final ClaimLandPlugin plugin;

    public ActionBarListener(ClaimLandPlugin plugin) {this.plugin = plugin;}

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getWorld() == null) return;
        int fromCx = e.getFrom().getChunk().getX();
        int fromCz = e.getFrom().getChunk().getZ();
        int toCx = e.getTo().getChunk().getX();
        int toCz = e.getTo().getChunk().getZ();
        if (fromCx == toCx && fromCz == toCz) return;
        var land = plugin.getLandManager().getLandAt(p.getLocation().getChunk());
        if (land != null && plugin.getConfig().getBoolean("action-bar-enabled", true)) {
            String fmt = plugin.getConfig().getString("action-bar-format", "Land: %land_name% | Owner: %land_owner%");
            String msg = fmt.replace("%land_name%", land.getName()).replace("%land_owner%", Bukkit.getOfflinePlayer(land.getOwner()).getName());
            p.sendActionBar(msg);
        }
    }
}
