package com.claimland;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {
    private final ClaimLandPlugin plugin;
    private HikariDataSource ds;

    public DatabaseManager(ClaimLandPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            HikariConfig config = new HikariConfig();
            String type = plugin.getConfig().getString("database.type", "sqlite");
            if (type.equalsIgnoreCase("mysql")) {
                String host = plugin.getConfig().getString("database.mysql.host");
                int port = plugin.getConfig().getInt("database.mysql.port");
                String db = plugin.getConfig().getString("database.mysql.database");
                String user = plugin.getConfig().getString("database.mysql.user");
                String pass = plugin.getConfig().getString("database.mysql.password");
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false");
                config.setUsername(user);
                config.setPassword(pass);
            } else {
                String file = plugin.getConfig().getString("database.sqlite-file", "data.db");
                plugin.getDataFolder().mkdirs();
                config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + file);
            }
            config.setMaximumPoolSize(4);
            ds = new HikariDataSource(config);
            setupTables();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", ex);
        }
    }

    private void setupTables() throws SQLException {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS lands (id TEXT PRIMARY KEY, name TEXT, owner TEXT, world TEXT, cx INTEGER, cz INTEGER, members TEXT, flags TEXT, tax_paid_until INTEGER, created_at INTEGER)");
            s.execute("CREATE TABLE IF NOT EXISTS metadata (k TEXT PRIMARY KEY, v TEXT)");
        }
    }

    public CompletableFuture<Void> saveLandAsync(Land land) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("REPLACE INTO lands(id,name,owner,world,cx,cz,members,flags,tax_paid_until,created_at) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, land.getId());
                ps.setString(2, land.getName());
                ps.setString(3, land.getOwner().toString());
                ps.setString(4, land.getWorld());
                ps.setInt(5, land.getCx());
                ps.setInt(6, land.getCz());
                ps.setString(7, land.getMembersCsv());
                ps.setString(8, land.getFlagsCsv());
                ps.setLong(9, land.getTaxPaidUntil());
                ps.setLong(10, land.getCreatedAt());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save land", ex);
            }
        });
    }

    public ResultSet syncQuery(String sql, Object... args) throws SQLException {
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
        return ps.executeQuery();
    }

    public CompletableFuture<Integer> nextIdAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT v FROM metadata WHERE k='last_land_id'")) {
                ResultSet rs = ps.executeQuery();
                int next = 1;
                if (rs.next()) next = Integer.parseInt(rs.getString("v")) + 1;
                try (PreparedStatement up = c.prepareStatement("REPLACE INTO metadata(k,v) VALUES('last_land_id',?)")) {
                    up.setString(1, String.valueOf(next));
                    up.executeUpdate();
                }
                return next;
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get next id", ex);
                return -1;
            }
        });
    }

    public void shutdown() {
        if (ds != null) ds.close();
    }
}
