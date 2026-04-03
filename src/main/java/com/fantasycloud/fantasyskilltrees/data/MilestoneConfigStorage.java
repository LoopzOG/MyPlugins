package com.fantasycloud.fantasyskilltrees.data;

import com.fantasycloud.fantasyskilltrees.item.SkillPointItemFactory;
import com.fantasycloud.fantasyskilltrees.model.MilestoneDefinition;
import com.fantasycloud.fantasyskilltrees.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MilestoneConfigStorage {
    private static final int LAYOUT_VERSION = 2;
    private final JavaPlugin plugin;
    private final File file;
    private final SkillPointItemFactory skillPointItemFactory;

    public MilestoneConfigStorage(JavaPlugin plugin, SkillPointItemFactory skillPointItemFactory) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "milestones.yml");
        this.skillPointItemFactory = skillPointItemFactory;
        if (!file.exists()) {
            plugin.saveResource("milestones.yml", false);
        }
    }

    public Map<String, MilestoneDefinition> load() {
        if (!file.exists()) {
            return Collections.emptyMap();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (shouldRegenerate(config)) {
            config = createDefaultConfiguration();
            saveConfiguration(config);
        }

        ConfigurationSection section = config.getConfigurationSection("milestones");
        if (section == null) {
            return Collections.emptyMap();
        }

        Map<String, MilestoneDefinition> milestones = new LinkedHashMap<String, MilestoneDefinition>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }

            String id = key.toLowerCase();
            int requiredLevel = Math.max(1, entry.getInt("level", 1));
            int slot = Math.max(0, entry.getInt("slot", milestones.size()));
            Material icon = MaterialUtil.matchOrDefault(entry.getString("material", "CHEST"), Material.CHEST);
            short iconData = (short) Math.max(0, Math.min(15, entry.getInt("data", 0)));
            String displayName = entry.getString("display-name", "&eLevel " + requiredLevel + " Milestone");
            List<String> description = entry.getStringList("description");
            List<ItemStack> rewards = readRewards(entry.getList("rewards"));
            milestones.put(id, new MilestoneDefinition(id, requiredLevel, slot, icon, iconData, displayName, description, rewards));
        }
        return milestones;
    }

    public void save(Map<String, MilestoneDefinition> milestones) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("layout-version", LAYOUT_VERSION);
        for (MilestoneDefinition milestone : milestones.values()) {
            String base = "milestones." + milestone.getId();
            config.set(base + ".level", milestone.getRequiredLevel());
            config.set(base + ".slot", milestone.getSlot());
            config.set(base + ".material", milestone.getIcon().name());
            config.set(base + ".data", milestone.getIconData());
            config.set(base + ".display-name", milestone.getDisplayName());
            config.set(base + ".description", milestone.getDescription());
            config.set(base + ".rewards", milestone.getRewards());
        }

        saveConfiguration(config);
    }

    private List<ItemStack> readRewards(List<?> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> rewards = new ArrayList<ItemStack>();
        for (Object entry : source) {
            if (entry instanceof ItemStack) {
                rewards.add(((ItemStack) entry).clone());
            }
        }
        return rewards;
    }

    private boolean shouldRegenerate(YamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("milestones");
        if (section == null) {
            return true;
        }
        if (config.getInt("layout-version", 0) < LAYOUT_VERSION) {
            return true;
        }
        return section.getKeys(false).size() < 100
                || !section.contains("level_1")
                || !section.contains("level_100");
    }

    private YamlConfiguration createDefaultConfiguration() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("layout-version", LAYOUT_VERSION);
        for (int level = 1; level <= 100; level++) {
            String base = "milestones.level_" + level;
            config.set(base + ".level", level);
            config.set(base + ".slot", level - 1);
            config.set(base + ".material", Material.STAINED_GLASS_PANE.name());
            config.set(base + ".data", getPaneDataForLevel(level));
            config.set(base + ".display-name", "&fLevel " + level);
            List<String> description = new ArrayList<String>();
            description.add("&7Claim this level milestone once.");
            description.add("&7Claimable once per map per IP.");
            description.add("&7Default Reward: &f" + getSkillPointReward(level) + " Skill Point" + (getSkillPointReward(level) == 1 ? "" : "s"));
            config.set(base + ".description", description);
            config.set(base + ".rewards", Collections.singletonList(skillPointItemFactory.createItem(getSkillPointReward(level), 1)));
        }
        return config;
    }

    private int getSkillPointReward(int level) {
        if (level <= 25) {
            return 1;
        }
        if (level <= 50) {
            return 2;
        }
        if (level <= 75) {
            return 3;
        }
        return 4;
    }

    private int getPaneDataForLevel(int level) {
        if (level <= 25) {
            return 14;
        }
        if (level <= 50) {
            return 1;
        }
        if (level <= 75) {
            return 4;
        }
        return 5;
    }

    private void saveConfiguration(YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save milestone configuration.", exception);
        }
    }
}
