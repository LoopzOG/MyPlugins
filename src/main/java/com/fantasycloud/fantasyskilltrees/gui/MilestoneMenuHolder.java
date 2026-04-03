package com.fantasycloud.fantasyskilltrees.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MilestoneMenuHolder implements InventoryHolder {
    private final boolean admin;
    private final int page;
    private Inventory inventory;

    public MilestoneMenuHolder(boolean admin, int page) {
        this.admin = admin;
        this.page = Math.max(0, page);
    }

    public boolean isAdmin() {
        return admin;
    }

    public int getPage() {
        return page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
