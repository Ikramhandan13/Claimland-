package com.claimland;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class LandManager {
    private final ClaimLandPlugin plugin;
    private final DatabaseManager db;
    private final Map<String, Land> cache = new ConcurrentHashMap<>();

    public LandManager(ClaimLandPlugin p, DatabaseManager db) {
        this.plugin = p;
        this.db = db;
        loadAll();
    }

    private void loadAll() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                db.querySync("SELECT * FROM lands", rs -> {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String name = rs.getString("name");
                        UUID owner = UUID.fromString(rs.getString("owner"));
                        String world = rs.getString("world");
                        int cx = rs.getInt("cx");
                        int cz = rs.getInt("cz");
                        long created = rs.getLong("created_at");
                        Land land = new Land(id, name, owner, world, cx, cz, created);
                        cache.put(key(world, cx, cz), land);
                    }
                });
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load lands", ex);
            }
        });
    }

    public String key(String world, int cx, int cz) {
        return world + ':' + cx + ':' + cz;
    }

    public CompletableFuture<Land> claimLand(Player p, Chunk c, String name) {
        return db.nextIdAsync().thenCompose(next -> {
            if (next <= 0) return CompletableFuture.failedFuture(new RuntimeException("id-fail"));
            String id = String.format("land-%04d", next);
            Land land = new Land(id, name, p.getUniqueId(), c.getWorld().getName(), c.getX(), c.getZ(), System.currentTimeMillis());
            cache.put(key(land.getWorld(), land.getCx(), land.getCz()), land);
            return db.saveLandAsync(land).thenApply(v -> land);
        });
    }

    public Land getLandAt(Chunk c) {
        return cache.get(key(c.getWorld().getName(), c.getX(), c.getZ()));
    }

    public CompletableFuture<Void> unclaimLand(Land land) {
        return CompletableFuture.runAsync(() -> {
            try {
                cache.remove(key(land.getWorld(), land.getCx(), land.getCz()));
                db.executeUpdateSync("DELETE FROM lands WHERE id=?", land.getId());
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to unclaim", ex);
            }
        });
    }

    public Land findById(String id) {
        for (Land land : cache.values()) if (land.getId().equalsIgnoreCase(id)) return land;
        return null;
    }

    public long countByOwner(java.util.UUID owner) {
        return cache.values().stream().filter(l -> l.getOwner().equals(owner)).count();
    }

    public void runWeeklyTax() {
        for (Land land : cache.values()) {
            double amount = plugin.getConfig().getDouble("tax.weekly-amount", 100.0);
            if (plugin.getEconomy() != null) {
                try {
                    var eco = plugin.getEconomy();
                    UUID owner = land.getOwner();
                    var offline = Bukkit.getOfflinePlayer(owner);
                    if (offline.isOnline()) {
                        var balance = eco.getBalance(offline);
                        if (balance >= amount) {
                            eco.withdrawPlayer(offline, amount);
                            land.setTaxPaidUntil(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7);
                            db.saveLandAsync(land);
                        } else {
                            if (plugin.getConfig().getBoolean("tax.auto-unclaim-if-unpaid", true)) {
                                unclaimLand(land);
                            }
                        }
                    } else {
                        // schedule or mark for later
                    }
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Tax run failed for " + land.getId(), ex);
                }
            }
        }
    }

    public java.util.concurrent.CompletableFuture<Void> saveAllAsync() {
        var futures = new java.util.ArrayList<java.util.concurrent.CompletableFuture<Void>>();
        for (Land land : cache.values()) futures.add(db.saveLandAsync(land));
        return java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0]));
    }

    public void shutdown() {
        saveAllAsync().join();
        cache.clear();
    }
}
