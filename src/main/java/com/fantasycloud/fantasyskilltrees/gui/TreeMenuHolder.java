package com.fantasycloud.fantasyskilltrees.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class TreeMenuHolder implements InventoryHolder {
    private final String treeId;
    private Inventory inventory;

    public TreeMenuHolder(String treeId) {
        this.treeId = treeId;
    }

    public String getTreeId() {
        return treeId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
