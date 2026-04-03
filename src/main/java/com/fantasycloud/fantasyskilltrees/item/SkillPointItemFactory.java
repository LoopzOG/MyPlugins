package com.fantasycloud.fantasyskilltrees.item;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillPointItemFactory {
    private final ConfigManager configManager;

    public SkillPointItemFactory(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public ItemStack createItem(int points, int amount) {
        int normalizedPoints = Math.max(1, points);
        int normalizedAmount = Math.max(1, amount);

        ItemStack stack = new ItemStack(configManager.getSkillPointItemMaterial(), normalizedAmount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.color(configManager.getSkillPointItemDisplayName().replace("%points%", String.valueOf(normalizedPoints))));
            List<String> lore = new ArrayList<String>(configManager.getSkillPointItemLore(normalizedPoints));
            lore.add(buildHiddenLore(normalizedPoints));
            meta.setLore(ChatUtil.color(lore));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public int getSkillPoints(ItemStack stack) {
        if (!isSkillPointItem(stack)) {
            return -1;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return -1;
        }

        String prefix = configManager.getSkillPointHiddenLorePrefix();
        for (String line : meta.getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped == null || !stripped.startsWith(prefix)) {
                continue;
            }
            String value = stripped.substring(prefix.length());
            try {
                return Math.max(1, Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    public boolean isSkillPointItem(ItemStack stack) {
        if (!configManager.isSkillPointItemEnabled()) {
            return false;
        }
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        if (stack.getType() != configManager.getSkillPointItemMaterial()) {
            return false;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return false;
        }

        String prefix = configManager.getSkillPointHiddenLorePrefix();
        for (String line : meta.getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped != null && stripped.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String buildHiddenLore(int points) {
        return "&0" + configManager.getSkillPointHiddenLorePrefix() + points;
    }
}
