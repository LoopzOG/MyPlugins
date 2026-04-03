package com.fantasycloud.fantasyskilltrees.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MilestoneDefinition {
    private final String id;
    private final int requiredLevel;
    private final int slot;
    private final Material icon;
    private final short iconData;
    private final String displayName;
    private final List<String> description;
    private final List<ItemStack> rewards;

    public MilestoneDefinition(String id,
                               int requiredLevel,
                               int slot,
                               Material icon,
                               short iconData,
                               String displayName,
                               List<String> description,
                               List<ItemStack> rewards) {
        this.id = id;
        this.requiredLevel = Math.max(1, requiredLevel);
        this.slot = Math.max(0, slot);
        this.icon = icon;
        this.iconData = iconData;
        this.displayName = displayName;
        this.description = description == null ? Collections.<String>emptyList() : new ArrayList<String>(description);
        this.rewards = rewards == null ? Collections.<ItemStack>emptyList() : cloneRewards(rewards);
    }

    public String getId() {
        return id;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getSlot() {
        return slot;
    }

    public Material getIcon() {
        return icon;
    }

    public short getIconData() {
        return iconData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return Collections.unmodifiableList(description);
    }

    public List<ItemStack> getRewards() {
        return cloneRewards(rewards);
    }

    private List<ItemStack> cloneRewards(List<ItemStack> source) {
        List<ItemStack> copy = new ArrayList<ItemStack>();
        for (ItemStack stack : source) {
            if (stack != null) {
                copy.add(stack.clone());
            }
        }
        return copy;
    }
}
