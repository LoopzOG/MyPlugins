package com.fantasycloud.fantasyskilltrees.model;

import org.bukkit.inventory.ItemStack;

public class LegacyVaultEntry {
    private final ItemStack itemStack;
    private final boolean locked;

    public LegacyVaultEntry(ItemStack itemStack, boolean locked) {
        this.itemStack = itemStack == null ? null : itemStack.clone();
        this.locked = locked;
    }

    public ItemStack getItemStack() {
        return itemStack == null ? null : itemStack.clone();
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isEmpty() {
        return itemStack == null;
    }
}
