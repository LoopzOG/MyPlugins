package com.fantasycloud.fantasyskilltrees.model;

import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillTree {
    private final String id;
    private final String displayName;
    private final List<String> description;
    private final Material icon;
    private final int menuSlot;
    private final Map<String, SkillNode> nodes;

    public SkillTree(String id,
                     String displayName,
                     List<String> description,
                     Material icon,
                     int menuSlot,
                     Map<String, SkillNode> nodes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.menuSlot = menuSlot;
        this.nodes = new LinkedHashMap<String, SkillNode>(nodes);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return Collections.unmodifiableList(description);
    }

    public Material getIcon() {
        return icon;
    }

    public int getMenuSlot() {
        return menuSlot;
    }

    public Map<String, SkillNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public SkillNode getNode(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        return nodes.get(nodeId.toLowerCase());
    }
}
