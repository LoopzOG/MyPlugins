package com.fantasycloud.fantasyskilltrees.config;

import com.fantasycloud.fantasyskilltrees.effect.EffectOperation;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.effect.SkillEffect;
import com.fantasycloud.fantasyskilltrees.model.NodeRequirement;
import com.fantasycloud.fantasyskilltrees.model.SkillNode;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import com.fantasycloud.fantasyskilltrees.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class TreeConfigLoader {
    private final JavaPlugin plugin;
    private final File treesFolder;
    private final Logger logger;

    public TreeConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.treesFolder = new File(plugin.getDataFolder(), "trees");
        this.logger = plugin.getLogger();
    }

    public void ensureDefaults() {
        if (!treesFolder.exists() && !treesFolder.mkdirs()) {
            logger.warning("Could not create trees folder: " + treesFolder.getAbsolutePath());
        }
        syncBundledTreeDefaults(false);
    }

    public void syncBundledTreeDefaults(boolean replaceExisting) {
        if (!treesFolder.exists() && !treesFolder.mkdirs()) {
            logger.warning("Could not create trees folder: " + treesFolder.getAbsolutePath());
        }
        saveBundledTree("trees/looting.yml", replaceExisting);
        saveBundledTree("trees/luck.yml", replaceExisting);
        saveBundledTree("trees/pve.yml", replaceExisting);
        saveBundledTree("trees/rebirth.yml", replaceExisting);
    }

    public Map<String, SkillTree> loadTrees() {
        Map<String, SkillTree> trees = new LinkedHashMap<String, SkillTree>();
        File[] files = treesFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            logger.warning("No tree files found in " + treesFolder.getAbsolutePath());
            return trees;
        }

        for (File file : files) {
            SkillTree tree = loadTreeFile(file);
            if (tree == null) {
                continue;
            }

            if (trees.containsKey(tree.getId())) {
                logger.warning("Duplicate tree id '" + tree.getId() + "' in file " + file.getName() + ". Skipping.");
                continue;
            }
            trees.put(tree.getId(), tree);
        }
        return trees;
    }

    private SkillTree loadTreeFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String id = normalizeId(config.getString("id", file.getName().replace(".yml", "")));
        if (id.isEmpty()) {
            logger.warning("Tree file " + file.getName() + " has invalid id.");
            return null;
        }

        String displayName = config.getString("display-name", id);
        List<String> description = config.getStringList("description");
        Material icon = MaterialUtil.matchOrDefault(config.getString("icon"), Material.BOOK);
        int menuSlot = config.getInt("menu-slot", 13);

        ConfigurationSection nodesSection = config.getConfigurationSection("nodes");
        if (nodesSection == null) {
            logger.warning("Tree '" + id + "' has no nodes section.");
            return null;
        }

        Map<String, SkillNode> nodes = new LinkedHashMap<String, SkillNode>();
        for (String key : nodesSection.getKeys(false)) {
            ConfigurationSection nodeSection = nodesSection.getConfigurationSection(key);
            if (nodeSection == null) {
                logger.warning("Invalid node '" + key + "' in tree '" + id + "'.");
                continue;
            }

            SkillNode node = parseNode(id, normalizeId(key), nodeSection);
            if (node != null) {
                nodes.put(node.getId(), node);
            }
        }

        if (nodes.isEmpty()) {
            logger.warning("Tree '" + id + "' has zero valid nodes.");
            return null;
        }

        return new SkillTree(id, displayName, description, icon, menuSlot, nodes);
    }

    private SkillNode parseNode(String treeId, String nodeId, ConfigurationSection section) {
        String displayName = section.getString("display-name", nodeId);
        List<String> description = section.getStringList("description");
        Material icon = MaterialUtil.matchOrDefault(section.getString("icon"), Material.PAPER);
        int cost = Math.max(0, section.getInt("cost", 1));
        int slot = Math.max(0, section.getInt("slot", 0));

        List<NodeRequirement> requirements = new ArrayList<NodeRequirement>();
        List<?> rawRequirements = section.getList("prerequisites", Collections.emptyList());
        for (Object rawRequirement : rawRequirements) {
            NodeRequirement requirement = parseRequirement(treeId, rawRequirement);
            if (requirement == null) {
                logger.warning("Skipping invalid requirement on node '" + nodeId + "' in tree '" + treeId + "'.");
                continue;
            }
            requirements.add(requirement);
        }

        List<SkillEffect> effects = parseEffects(section);
        if (effects.isEmpty()) {
            logger.warning("Node '" + nodeId + "' in tree '" + treeId + "' has no valid effects.");
        }

        return new SkillNode(nodeId, displayName, description, icon, cost, slot, requirements, effects);
    }

    private List<SkillEffect> parseEffects(ConfigurationSection rootSection) {
        List<?> list = rootSection.getList("effects", Collections.emptyList());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<SkillEffect> effects = new ArrayList<SkillEffect>();
        for (int i = 0; i < list.size(); i++) {
            Object raw = list.get(i);
            if (!(raw instanceof Map)) {
                logger.warning("Invalid effect entry at index " + i + ". Expected map.");
                continue;
            }

            Map<?, ?> map = (Map<?, ?>) raw;
            String typeRaw = asString(map.get("type"));
            String opRaw = asString(map.get("operation"));
            double value = asDouble(map.get("value"));

            EffectType type = EffectType.fromString(typeRaw);
            EffectOperation operation = EffectOperation.fromString(opRaw);
            if (type == null || operation == null) {
                logger.warning("Invalid effect type/operation: type=" + typeRaw + ", operation=" + opRaw);
                continue;
            }

            effects.add(new SkillEffect(type, value, operation));
        }
        return effects;
    }

    private void saveBundledTree(String path, boolean replaceExisting) {
        File target = new File(plugin.getDataFolder(), path);
        if (!target.exists() || replaceExisting) {
            plugin.saveResource(path, replaceExisting);
        }
    }

    private NodeRequirement parseRequirement(String currentTreeId, Object rawRequirement) {
        if (rawRequirement instanceof String) {
            return NodeRequirement.fromConfigValue(currentTreeId, (String) rawRequirement);
        }
        if (rawRequirement instanceof Map) {
            return NodeRequirement.fromMap(currentTreeId, (Map<?, ?>) rawRequirement);
        }
        return null;
    }

    private String normalizeId(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value == null ? 0D : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return 0D;
        }
    }
}
