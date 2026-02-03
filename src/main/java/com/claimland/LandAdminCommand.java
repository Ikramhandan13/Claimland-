package com.claimland;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.UUID;

public class LandAdminCommand implements CommandExecutor {
    private final ClaimLandPlugin plugin;

    public LandAdminCommand(ClaimLandPlugin plugin) {this.plugin = plugin;}

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("claimland.admin")) {sender.sendMessage(plugin.getConfig().getString("no-permission")); return true;}
        if (args.length == 0) {sender.sendMessage("Usage: /landadmin <list|info|delland|transfer|settax|forceclaim|bypass|audit|reload>"); return true;}
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage("Reloaded");
                return true;
            case "delland":
                if (args.length < 2) {sender.sendMessage("Usage: /landadmin delland <land-id>"); return true;}
                String id = args[1];
                var land = plugin.getLandManager().findById(id);
                if (land != null) {
                    plugin.getLandManager().unclaimLand(land);
                    sender.sendMessage("Deleted " + id);
                    return true;
                }
                sender.sendMessage("Not found");
                return true;
        }
        return true;
    }
}
