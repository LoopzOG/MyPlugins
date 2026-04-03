package com.fantasycloud.fantasyskilltrees.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public final class ItemUtil {
    private ItemUtil() {
    }

    public static ItemStack createItem(Material material, int amount, short durability, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material, amount, durability);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.color(name));
            meta.setLore(ChatUtil.color(lore));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, 1, (short) 0, name, lore);
    }

    public static ItemStack createItem(Material material, String name) {
        return createItem(material, 1, (short) 0, name, Collections.<String>emptyList());
    }
}
