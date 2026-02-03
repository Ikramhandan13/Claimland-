package com.claimland;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {
    private final ClaimLandPlugin plugin;
    private Object hikariDs;
    private boolean useHikari = false;
    private String jdbcUrl;
    private String dbUser;
    private String dbPass;

    public DatabaseManager(ClaimLandPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            String type = plugin.getConfig().getString("database.type", "sqlite");
            if (type.equalsIgnoreCase("mysql")) {
                String host = plugin.getConfig().getString("database.mysql.host");
                int port = plugin.getConfig().getInt("database.mysql.port");
                String db = plugin.getConfig().getString("database.mysql.database");
                dbUser = plugin.getConfig().getString("database.mysql.user");
                dbPass = plugin.getConfig().getString("database.mysql.password");
                jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false";
            } else {
                String file = plugin.getConfig().getString("database.sqlite-file", "data.db");
                plugin.getDataFolder().mkdirs();
                jdbcUrl = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + file;
                dbUser = null; dbPass = null;
            }

            try {
                // Use reflection to avoid a hard compile-time dependency on HikariCP
                Class<?> hikariConfigClass = Class.forName("com.zaxxer.hikari.HikariConfig");
                Object config = hikariConfigClass.getDeclaredConstructor().newInstance();
                hikariConfigClass.getMethod("setJdbcUrl", String.class).invoke(config, jdbcUrl);
                if (dbUser != null) hikariConfigClass.getMethod("setUsername", String.class).invoke(config, dbUser);
                if (dbPass != null) hikariConfigClass.getMethod("setPassword", String.class).invoke(config, dbPass);
                hikariConfigClass.getMethod("setMaximumPoolSize", int.class).invoke(config, 4);

                Class<?> hikariDataSourceClass = Class.forName("com.zaxxer.hikari.HikariDataSource");
                hikariDs = hikariDataSourceClass.getDeclaredConstructor(hikariConfigClass).newInstance(config);
                useHikari = true;
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().warning("HikariCP not found on classpath, falling back to DriverManager connections (SQLite/MySQL). This is less optimized but functional.");
                useHikari = false;
                if (jdbcUrl.startsWith("jdbc:sqlite:")) {
                    try { Class.forName("org.sqlite.JDBC"); } catch (Throwable ignore) {}
                }
            } catch (ReflectiveOperationException rex) {
                plugin.getLogger().log(Level.WARNING, "Failed to initialize HikariCP via reflection, falling back to DriverManager", rex);
                useHikari = false;
            }

            setupTables();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", ex);
        }
    }

    private Connection getConnection() throws SQLException {
        if (useHikari && hikariDs != null) {
            try {
                Object conn = hikariDs.getClass().getMethod("getConnection").invoke(hikariDs);
                return (Connection) conn;
            } catch (Exception ex) {
                throw new SQLException("Failed to get connection from HikariCP", ex);
            }
        }
        if (dbUser != null) return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
        return DriverManager.getConnection(jdbcUrl);
    }

    private void setupTables() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS lands (id TEXT PRIMARY KEY, name TEXT, owner TEXT, world TEXT, cx INTEGER, cz INTEGER, members TEXT, flags TEXT, tax_paid_until INTEGER, created_at INTEGER)");
            s.execute("CREATE TABLE IF NOT EXISTS metadata (k TEXT PRIMARY KEY, v TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS audit (time INTEGER, land_id TEXT, player TEXT, action TEXT, details TEXT)");
        }
    }

    public CompletableFuture<Void> saveLandAsync(Land land) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("REPLACE INTO lands(id,name,owner,world,cx,cz,members,flags,tax_paid_until,created_at) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
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

    public CompletableFuture<Void> logAuditAsync(long at, String landId, String player, String action, String details) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO audit(time, land_id, player, action, details) VALUES(?,?,?,?,?)")) {
                ps.setLong(1, at);
                ps.setString(2, landId);
                ps.setString(3, player);
                ps.setString(4, action);
                ps.setString(5, details);
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to log audit", ex);
            }
        });
    }

    public interface ResultSetHandler { void handle(ResultSet rs) throws SQLException; }

    public void querySync(String sql, ResultSetHandler handler, Object... args) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) {
                handler.handle(rs);
            }
        }
    }

    public int executeUpdateSync(String sql, Object... args) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps.executeUpdate();
        }
    }

    public CompletableFuture<Integer> nextIdAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT v FROM metadata WHERE k='last_land_id'")) {
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
        try {
            if (useHikari && hikariDs != null) {
                try {
                    hikariDs.getClass().getMethod("close").invoke(hikariDs);
                } catch (Exception ignore) {}
            }
        } catch (Throwable ignore) {}
    }
}
