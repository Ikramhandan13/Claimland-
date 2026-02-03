package com.claimland;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

public class LandCommand implements CommandExecutor, TabCompleter {
    private final ClaimLandPlugin plugin;

    public LandCommand(ClaimLandPlugin plugin) {this.plugin = plugin;}

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage(plugin.getConfig().getString("messages.land-info-format", "Land commands"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "claim":
                if (!p.hasPermission("claimland.land.claim")) {p.sendMessage(plugin.getConfig().getString("no-permission")); return true;}
                Chunk c = p.getLocation().getChunk();
                if (plugin.getLandManager().getLandAt(c) != null) {p.sendMessage(plugin.getConfig().getString("already-claimed").replace("%owner%", "")); return true;}
                String name = args.length > 1 ? args[1] : "Land";
                plugin.getLandManager().claimLand(p, c, name).thenAccept(land -> p.sendMessage(plugin.getConfig().getString("claim-success").replace("%land_id%", land.getId())));
                return true;
            case "unclaim":
                Chunk cc = p.getLocation().getChunk();
                var land = plugin.getLandManager().getLandAt(cc);
                if (land == null) {p.sendMessage(plugin.getConfig().getString("not-claimed")); return true;}
                if (!land.getOwner().equals(p.getUniqueId()) && !p.hasPermission("claimland.admin")) {p.sendMessage(plugin.getConfig().getString("no-permission")); return true;}
                plugin.getLandManager().unclaimLand(land).thenRun(() -> p.sendMessage(plugin.getConfig().getString("unclaim-success").replace("%land_id%", land.getId())));
                return true;
            case "info":
                var li = plugin.getLandManager().getLandAt(p.getLocation().getChunk());
                if (li == null) {p.sendMessage(plugin.getConfig().getString("not-claimed")); return true;}
                p.sendMessage(plugin.getConfig().getString("land-info-format").replace("%land_name%", li.getName()).replace("%land_owner%", p.getServer().getOfflinePlayer(li.getOwner()).getName()).replace("%member_count%", String.valueOf(li.getMembers().size())));
                return true;
            case "gui":
                var landg = plugin.getLandManager().getLandAt(p.getLocation().getChunk());
                if (landg == null) {p.sendMessage(plugin.getConfig().getString("not-claimed")); return true;}
                new LandGui(plugin, landg).open(p);
                return true;
            case "addmember":
                if (args.length < 2) {p.sendMessage("Usage: /land addmember <player>"); return true;}
                var target = p.getServer().getPlayer(args[1]);
                if (target == null) {p.sendMessage("Player not found"); return true;}
                var landm = plugin.getLandManager().getLandAt(p.getLocation().getChunk());
                if (landm == null) {p.sendMessage(plugin.getConfig().getString("not-claimed")); return true;}
                if (!landm.getOwner().equals(p.getUniqueId())) {p.sendMessage(plugin.getConfig().getString("no-permission")); return true;}
                landm.getMembers().add(target.getUniqueId());
                plugin.getDatabaseManager().saveLandAsync(landm);
                p.sendMessage("Member added");
                return true;
            case "delmember":
                if (args.length < 2) {p.sendMessage("Usage: /land delmember <player>"); return true;}
                var target2 = p.getServer().getPlayer(args[1]);
                if (target2 == null) {p.sendMessage("Player not found"); return true;}
                var landd = plugin.getLandManager().getLandAt(p.getLocation().getChunk());
                if (landd == null) {p.sendMessage(plugin.getConfig().getString("not-claimed")); return true;}
                if (!landd.getOwner().equals(p.getUniqueId())) {p.sendMessage(plugin.getConfig().getString("no-permission")); return true;}
                landd.getMembers().remove(target2.getUniqueId());
                plugin.getDatabaseManager().saveLandAsync(landd);
                p.sendMessage("Member removed");
                return true;
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> res = new ArrayList<>();
        if (args.length == 1) {
            res.add("claim"); res.add("unclaim"); res.add("info"); res.add("gui"); res.add("addmember"); res.add("delmember");
        }
        return res;
    }
}
