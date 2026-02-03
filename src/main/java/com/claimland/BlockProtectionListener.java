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
            if (!land.getFlags().getOrDefault("break", false)) {
                e.setCancelled(true);
                plugin.getDatabaseManager().logAuditAsync(System.currentTimeMillis(), land.getId(), p.getUniqueId().toString(), "BLOCK_BREAK", "Denied");
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        var land = plugin.getLandManager().getLandAt(e.getBlock().getChunk());
        if (land == null) return;
        var p = e.getPlayer();
        if (p.hasPermission("claimland.bypass")) return;
        if (!p.getUniqueId().equals(land.getOwner()) && !land.getMembers().contains(p.getUniqueId())) {
            if (!land.getFlags().getOrDefault("place", false)) {
                e.setCancelled(true);
                plugin.getDatabaseManager().logAuditAsync(System.currentTimeMillis(), land.getId(), p.getUniqueId().toString(), "BLOCK_PLACE", "Denied");
            }
        }
    }

    @EventHandler
    public void onPiston(org.bukkit.event.block.BlockPistonExtendEvent e) {
        for (var block : e.getBlocks()) {
            var land = plugin.getLandManager().getLandAt(block.getChunk());
            if (land == null) continue;
            if (!land.getFlags().getOrDefault("place", false)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onHopperMove(org.bukkit.event.inventory.InventoryMoveItemEvent e) {
        var inv = e.getDestination();
        if (inv == null || inv.getLocation() == null) return;
        var land = plugin.getLandManager().getLandAt(inv.getLocation().getChunk());
        if (land == null) return;
        if (!land.getFlags().getOrDefault("open_chests", false)) e.setCancelled(true);
    }

    @EventHandler
    public void onExplode(org.bukkit.event.entity.EntityExplodeEvent e) {
        var blocks = e.blockList();
        for (var b : blocks) {
            var land = plugin.getLandManager().getLandAt(b.getChunk());
            if (land != null && !land.getFlags().getOrDefault("explosions", false)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onFireSpread(org.bukkit.event.block.BlockIgniteEvent e) {
        var land = plugin.getLandManager().getLandAt(e.getBlock().getChunk());
        if (land != null && !land.getFlags().getOrDefault("fire_spread", false)) {
            e.setCancelled(true);
        }
    }
}

