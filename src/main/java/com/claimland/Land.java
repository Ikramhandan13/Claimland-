package com.claimland;

import java.util.*;

public class Land {
    private final String id;
    private String name;
    private final UUID owner;
    private final String world;
    private final int cx;
    private final int cz;
    private final Set<UUID> members = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Boolean> flags = Collections.synchronizedMap(new LinkedHashMap<>());
    private long taxPaidUntil;
    private final long createdAt;

    public Land(String id, String name, UUID owner, String world, int cx, int cz, long createdAt) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.cx = cx;
        this.cz = cz;
        this.createdAt = createdAt;
        defaultFlags();
    }

    private void defaultFlags() {
        flags.put("break", false);
        flags.put("place", false);
        flags.put("open_chests", false);
        flags.put("use_doors", false);
        flags.put("use_buttons", false);
        flags.put("armor_stand", false);
        flags.put("lead_mobs", false);
        flags.put("damage_entities", false);
        flags.put("explosions", false);
        flags.put("fire_spread", false);
    }

    public String getId() {return id;}
    public String getName() {return name;}
    public UUID getOwner() {return owner;}
    public String getWorld() {return world;}
    public int getCx() {return cx;}
    public int getCz() {return cz;}
    public Set<UUID> getMembers() {return members;}
    public Map<String, Boolean> getFlags() {return flags;}
    public long getTaxPaidUntil() {return taxPaidUntil;}
    public void setTaxPaidUntil(long t) {taxPaidUntil = t;}
    public long getCreatedAt() {return createdAt;}

    public String getMembersCsv() {
        StringBuilder sb = new StringBuilder();
        synchronized (members) {
            boolean first = true;
            for (UUID u : members) {
                if (!first) sb.append(',');
                sb.append(u.toString());
                first = false;
            }
        }
        return sb.toString();
    }

    public String getFlagsCsv() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Boolean> e : flags.entrySet()) {
            if (!first) sb.append(',');
            sb.append(e.getKey()).append(':').append(e.getValue());
            first = false;
        }
        return sb.toString();
    }
}
