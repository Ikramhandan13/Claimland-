package com.claimland;

import org.bukkit.plugin.Plugin;

public final class SchedulerUtil {
    private SchedulerUtil() {}

    public static void scheduleRepeat(Plugin plugin, Runnable task, long delay, long period) {
        try {
            Class<?> regSched = Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            // Folia: schedule via region scheduler reflectively
            var server = plugin.getServer();
            var scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            var method = scheduler.getClass().getMethod("runAtFixedRate", Runnable.class, long.class, long.class);
            method.invoke(scheduler, task, delay, period);
            return;
        } catch (Throwable ex) {
            // Fallback to Bukkit scheduler asynchronous
            plugin.getServer().getScheduler().runTaskTimerAsynchronously((org.bukkit.plugin.java.JavaPlugin) plugin, task, delay, period);
        }
    }
}
