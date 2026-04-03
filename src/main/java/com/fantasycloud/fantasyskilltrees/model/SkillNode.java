package com.fantasycloud.fantasyskilltrees.model;

import com.fantasycloud.fantasyskilltrees.effect.SkillEffect;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

public class SkillNode {
    private final String id;
    private final String displayName;
    private final List<String> description;
    private final Material icon;
    private final int cost;
    private final int slot;
    private final List<NodeRequirement> prerequisites;
    private final List<SkillEffect> effects;

    public SkillNode(String id,
                     String displayName,
                     List<String> description,
                     Material icon,
                     int cost,
                     int slot,
                     List<NodeRequirement> prerequisites,
                     List<SkillEffect> effects) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.cost = cost;
        this.slot = slot;
        this.prerequisites = prerequisites;
        this.effects = effects;
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

    public int getCost() {
        return cost;
    }

    public int getSlot() {
        return slot;
    }

    public List<NodeRequirement> getPrerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    public List<SkillEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }
}
