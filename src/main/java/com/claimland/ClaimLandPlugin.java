package com.claimland;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import java.util.logging.Level;

public class ClaimLandPlugin extends JavaPlugin {
    private static ClaimLandPlugin instance;
    private DatabaseManager databaseManager;
    private LandManager landManager;
    private Economy economy;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);
        validateConfig();
        databaseManager.initialize();
        landManager = new LandManager(this, databaseManager);
        registerCommands();
        registerListeners();
        hookVault();
        scheduleTaxTask();
        registerPAPI();
        getLogger().log(Level.INFO, "ClaimLand enabled");
    }

    private void validateConfig() {
        String type = getConfig().getString("database.type", "sqlite");
        if (!type.equalsIgnoreCase("sqlite") && !type.equalsIgnoreCase("mysql")) {
            getLogger().warning("Invalid database.type, falling back to sqlite");
            getConfig().set("database.type", "sqlite");
            saveConfig();
        }
        int page = getConfig().getInt("gui.page-size", 45);
        if (page % 9 != 0 || page < 9 || page > 54) {
            getLogger().warning("Invalid gui.page-size, resetting to 45");
            getConfig().set("gui.page-size", 45);
            saveConfig();
        }
    }

    public void onDisable() {
        if (landManager != null) {
            landManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().log(Level.INFO, "ClaimLand disabled");
    }

    private void registerCommands() {
        getCommand("land").setExecutor(new LandCommand(this));
        getCommand("land").setTabCompleter(new LandCommand(this));
        getCommand("landadmin").setExecutor(new LandAdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ActionBarListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);
    }

    private void hookVault() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    private void registerPAPI() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ClaimLandPlaceholder(this).register();
        }
    }

    private void scheduleTaxTask() {
        long ticksPerWeek = 20L * 60 * 60 * 24 * 7;
        long period = ticksPerWeek;
        SchedulerUtil.scheduleRepeat(this, () -> landManager.runWeeklyTax(), period, period);
    }

    public static ClaimLandPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public LandManager getLandManager() {
        return landManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
