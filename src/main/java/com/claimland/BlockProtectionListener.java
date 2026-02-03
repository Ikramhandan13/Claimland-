package com.claimland;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {
    private final ClaimLandPlugin plugin;

    public BlockProtectionListener(ClaimLandPlugin plugin) {this.plugin = plugin;}

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        var land = plugin.getLandManager().getLandAt(e.getBlock().getChunk());
        if (land == null) return;
        var p = e.getPlayer();
        if (p.hasPermission("claimland.bypass")) return;
        if (!p.getUniqueId().equals(land.getOwner()) && !land.getMembers().contains(p.getUniqueId())) {
            if (!land.getFlags().getOrDefault("break", false)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        var land = plugin.getLandManager().getLandAt(e.getBlock().getChunk());
        if (land == null) return;
        var p = e.getPlayer();
        if (p.hasPermission("claimland.bypass")) return;
        if (!p.getUniqueId().equals(land.getOwner()) && !land.getMembers().contains(p.getUniqueId())) {
            if (!land.getFlags().getOrDefault("place", false)) e.setCancelled(true);
        }
    }
}
