package com.fantasycloud.fantasyskilltrees.data;

import com.fantasycloud.fantasyskilltrees.model.PlayerSkillData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataStorage {
    private final JavaPlugin plugin;
    private final File dataFolder;

    public PlayerDataStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create player data folder: " + this.dataFolder.getAbsolutePath());
        }
    }

    public PlayerSkillData load(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return new PlayerSkillData(uuid);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int available = config.getInt("points.available", 0);
        int spent = config.getInt("points.spent", 0);
        int levelSkillPointsAwarded = config.getInt("points.level-awarded", 0);
        int level = config.getInt("progression.level", 0);
        double experience = config.getDouble("progression.experience", 0D);
        int mobKillProgress = config.getInt("progression.mob-kill-progress", 0);

        Map<String, Set<String>> unlocked = new HashMap<String, Set<String>>();
        ConfigurationSection section = config.getConfigurationSection("unlocked");
        if (section != null) {
            for (String treeId : section.getKeys(false)) {
                Set<String> nodes = new HashSet<String>();
                for (String nodeId : section.getStringList(treeId)) {
                    nodes.add(nodeId.toLowerCase());
                }
                unlocked.put(treeId.toLowerCase(), nodes);
            }
        }

        Map<String, Set<String>> claimedMilestones = new HashMap<String, Set<String>>();
        ConfigurationSection milestonesSection = config.getConfigurationSection("claimed-milestones");
        if (milestonesSection != null) {
            for (String mapId : milestonesSection.getKeys(false)) {
                Set<String> milestoneIds = new HashSet<String>();
                for (String milestoneId : milestonesSection.getStringList(mapId)) {
                    milestoneIds.add(milestoneId.toLowerCase());
                }
                claimedMilestones.put(mapId.toLowerCase(), milestoneIds);
            }
        }

        return new PlayerSkillData(uuid, unlocked, claimedMilestones, available, spent, levelSkillPointsAwarded, level, experience, mobKillProgress);
    }

    public void save(PlayerSkillData data) {
        File file = getPlayerFile(data.getUuid());
        YamlConfiguration config = new YamlConfiguration();
        config.set("points.available", data.getAvailableSkillPoints());
        config.set("points.spent", data.getSpentSkillPoints());
        config.set("points.level-awarded", data.getLevelSkillPointsAwarded());
        config.set("progression.level", data.getLevel());
        config.set("progression.experience", data.getExperience());
        config.set("progression.mob-kill-progress", data.getMobKillProgress());

        for (Map.Entry<String, Set<String>> entry : data.getUnlockedNodesByTree().entrySet()) {
            config.set("unlocked." + entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        for (Map.Entry<String, Set<String>> entry : data.getClaimedMilestonesByMap().entrySet()) {
            config.set("claimed-milestones." + entry.getKey(), entry.getValue().toArray(new String[0]));
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player skill data for " + data.getUuid(), exception);
        }
    }

    public Set<UUID> getStoredPlayerUuids() {
        File[] files = dataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null || files.length == 0) {
            return Collections.emptySet();
        }

        Set<UUID> uuids = new HashSet<UUID>();
        for (File file : files) {
            String rawName = file.getName();
            String uuidValue = rawName.substring(0, rawName.length() - 4);
            try {
                uuids.add(UUID.fromString(uuidValue));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed filenames in the player data folder.
            }
        }
        return uuids;
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }
}
