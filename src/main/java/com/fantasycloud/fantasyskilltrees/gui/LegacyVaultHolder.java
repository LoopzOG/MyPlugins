package com.fantasycloud.fantasyskilltrees.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class LegacyVaultHolder implements InventoryHolder {
    private final UUID ownerUuid;
    private final int accessibleSlots;
    private final boolean depositEnabled;
    private Inventory inventory;

    public LegacyVaultHolder(UUID ownerUuid, int accessibleSlots, boolean depositEnabled) {
        this.ownerUuid = ownerUuid;
        this.accessibleSlots = accessibleSlots;
        this.depositEnabled = depositEnabled;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getAccessibleSlots() {
        return accessibleSlots;
    }

    public boolean isDepositEnabled() {
        return depositEnabled;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
