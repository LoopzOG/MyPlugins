package com.fantasycloud.fantasyskilltrees.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MilestoneRewardEditorHolder implements InventoryHolder {
    private final String milestoneId;
    private Inventory inventory;

    public MilestoneRewardEditorHolder(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
