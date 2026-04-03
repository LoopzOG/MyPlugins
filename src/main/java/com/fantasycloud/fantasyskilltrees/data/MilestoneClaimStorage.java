package com.fantasycloud.fantasyskilltrees.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MilestoneClaimStorage {
    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, Map<String, Map<String, String>>> claimsByMap = new LinkedHashMap<String, Map<String, Map<String, String>>>();

    public MilestoneClaimStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "milestone-claims.yml");
        load();
    }

    public synchronized void load() {
        claimsByMap.clear();
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection maps = config.getConfigurationSection("maps");
        if (maps == null) {
            return;
        }

        for (String mapKey : maps.getKeys(false)) {
            ConfigurationSection ipSection = maps.getConfigurationSection(mapKey);
            if (ipSection == null) {
                continue;
            }

            Map<String, Map<String, String>> ipClaims = new LinkedHashMap<String, Map<String, String>>();
            for (String ipKey : ipSection.getKeys(false)) {
                ConfigurationSection milestoneSection = ipSection.getConfigurationSection(ipKey);
                if (milestoneSection == null) {
                    continue;
                }

                Map<String, String> milestoneClaims = new LinkedHashMap<String, String>();
                for (String milestoneId : milestoneSection.getKeys(false)) {
                    String uuid = milestoneSection.getString(milestoneId);
                    if (uuid != null && !uuid.trim().isEmpty()) {
                        milestoneClaims.put(milestoneId.toLowerCase(), uuid);
                    }
                }
                if (!milestoneClaims.isEmpty()) {
                    ipClaims.put(ipKey.toLowerCase(), milestoneClaims);
                }
            }
            if (!ipClaims.isEmpty()) {
                claimsByMap.put(mapKey.toLowerCase(), ipClaims);
            }
        }
    }

    public synchronized String getClaimer(String mapId, String ipKey, String milestoneId) {
        Map<String, Map<String, String>> mapClaims = claimsByMap.get(normalize(mapId));
        if (mapClaims == null) {
            return null;
        }

        Map<String, String> ipClaims = mapClaims.get(normalize(ipKey));
        if (ipClaims == null) {
            return null;
        }
        return ipClaims.get(normalize(milestoneId));
    }

    public synchronized void setClaim(String mapId, String ipKey, String milestoneId, UUID uuid) {
        if (uuid == null) {
            return;
        }

        String normalizedMap = normalize(mapId);
        String normalizedIp = normalize(ipKey);
        String normalizedMilestone = normalize(milestoneId);
        Map<String, Map<String, String>> mapClaims = claimsByMap.get(normalizedMap);
        if (mapClaims == null) {
            mapClaims = new LinkedHashMap<String, Map<String, String>>();
            claimsByMap.put(normalizedMap, mapClaims);
        }
        Map<String, String> ipClaims = mapClaims.get(normalizedIp);
        if (ipClaims == null) {
            ipClaims = new LinkedHashMap<String, String>();
            mapClaims.put(normalizedIp, ipClaims);
        }
        ipClaims.put(normalizedMilestone, uuid.toString());
        save();
    }

    public synchronized void clearClaimsForPlayer(UUID uuid, String mapId) {
        if (uuid == null) {
            return;
        }

        String expected = uuid.toString();
        if (mapId == null) {
            for (String currentMap : claimsByMap.keySet().toArray(new String[0])) {
                clearClaimsForPlayerInMap(currentMap, expected);
            }
        } else {
            clearClaimsForPlayerInMap(normalize(mapId), expected);
        }
        save();
    }

    public synchronized Map<String, Map<String, Map<String, String>>> snapshot() {
        Map<String, Map<String, Map<String, String>>> copy = new LinkedHashMap<String, Map<String, Map<String, String>>>();
        for (Map.Entry<String, Map<String, Map<String, String>>> mapEntry : claimsByMap.entrySet()) {
            Map<String, Map<String, String>> ipCopy = new LinkedHashMap<String, Map<String, String>>();
            for (Map.Entry<String, Map<String, String>> ipEntry : mapEntry.getValue().entrySet()) {
                ipCopy.put(ipEntry.getKey(), new LinkedHashMap<String, String>(ipEntry.getValue()));
            }
            copy.put(mapEntry.getKey(), ipCopy);
        }
        return Collections.unmodifiableMap(copy);
    }

    private void clearClaimsForPlayerInMap(String mapId, String uuid) {
        Map<String, Map<String, String>> mapClaims = claimsByMap.get(mapId);
        if (mapClaims == null) {
            return;
        }

        for (String ipKey : mapClaims.keySet().toArray(new String[0])) {
            Map<String, String> ipClaims = mapClaims.get(ipKey);
            if (ipClaims == null) {
                continue;
            }

            for (String milestoneId : ipClaims.keySet().toArray(new String[0])) {
                if (uuid.equalsIgnoreCase(ipClaims.get(milestoneId))) {
                    ipClaims.remove(milestoneId);
                }
            }

            if (ipClaims.isEmpty()) {
                mapClaims.remove(ipKey);
            }
        }

        if (mapClaims.isEmpty()) {
            claimsByMap.remove(mapId);
        }
    }

    private synchronized void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Map<String, Map<String, String>>> mapEntry : claimsByMap.entrySet()) {
            for (Map.Entry<String, Map<String, String>> ipEntry : mapEntry.getValue().entrySet()) {
                for (Map.Entry<String, String> milestoneEntry : ipEntry.getValue().entrySet()) {
                    config.set("maps." + mapEntry.getKey() + "." + ipEntry.getKey() + "." + milestoneEntry.getKey(), milestoneEntry.getValue());
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save milestone claims.", exception);
        }
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        return value.toLowerCase().replace('.', '_');
    }
}
