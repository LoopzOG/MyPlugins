package com.fantasycloud.fantasyskilltrees.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class LegacyVaultConfirmHolder implements InventoryHolder {
    private final UUID ownerUuid;
    private final int sourceSlot;
    private final int targetVaultIndex;
    private final ItemStack pendingItem;
    private Inventory inventory;

    public LegacyVaultConfirmHolder(UUID ownerUuid, int sourceSlot, int targetVaultIndex, ItemStack pendingItem) {
        this.ownerUuid = ownerUuid;
        this.sourceSlot = sourceSlot;
        this.targetVaultIndex = targetVaultIndex;
        this.pendingItem = pendingItem == null ? null : pendingItem.clone();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getSourceSlot() {
        return sourceSlot;
    }

    public int getTargetVaultIndex() {
        return targetVaultIndex;
    }

    public ItemStack getPendingItem() {
        return pendingItem == null ? null : pendingItem.clone();
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
