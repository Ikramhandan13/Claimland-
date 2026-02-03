package com.claimland;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LandGui implements Listener {
    private final ClaimLandPlugin plugin;
    private final Land land;

    public LandGui(ClaimLandPlugin plugin, Land land) {this.plugin = plugin; this.land = land;}

    public void open(Player p) {
        int size = plugin.getConfig().getInt("gui.page-size", 45);
        String title = plugin.getConfig().getString("gui.land-info.title", "Land Info");
        Inventory inv = Bukkit.createInventory(null, size, title.replace("%land_name%", land.getName()));
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("\u00A76" + land.getName());
        List<String> lore = new ArrayList<>();
        lore.add("Owner: " + Bukkit.getOfflinePlayer(land.getOwner()).getName());
        lore.add("Members: " + land.getMembers().size());
        im.setLore(lore);
        info.setItemMeta(im);
        inv.setItem(13, info);
        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta mm = members.getItemMeta();
        mm.setDisplayName("Members");
        members.setItemMeta(mm);
        inv.setItem(21, members);
        ItemStack perms = new ItemStack(Material.LEVER);
        ItemMeta pm = perms.getItemMeta();
        pm.setDisplayName("Permissions");
        perms.setItemMeta(pm);
        inv.setItem(23, perms);
        ItemStack tax = new ItemStack(Material.GOLD_INGOT);
        ItemMeta tm = tax.getItemMeta();
        tm.setDisplayName("Tax");
        tax.setItemMeta(tm);
        inv.setItem(31, tax);
        p.openInventory(inv);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().contains(land.getName())) return;
        e.setCancelled(true);
    }
}
