package com.fantasycloud.fantasyskilltrees.gui;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import com.fantasycloud.fantasyskilltrees.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainMenuGui {
    private final ConfigManager configManager;
    private final SkillTreeServiceImpl skillTreeService;

    public MainMenuGui(ConfigManager configManager, SkillTreeServiceImpl skillTreeService) {
        this.configManager = configManager;
        this.skillTreeService = skillTreeService;
    }

    public Inventory build(Player player) {
        MainMenuHolder holder = new MainMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, configManager.getMainMenuSize(), ChatUtil.color(configManager.getMainMenuTitle()));
        holder.setInventory(inventory);

        fillBackground(inventory);
        int points = skillTreeService.getAvailablePoints(player.getUniqueId());
        int progressSlot = configManager.getProgressInfoSlot();
        if (progressSlot >= 0 && progressSlot < inventory.getSize()) {
            inventory.setItem(progressSlot, ItemUtil.createItem(
                Material.NETHER_STAR,
                "&bYour Progress",
                Arrays.asList(
                        "&7Level: &f" + skillTreeService.getLevel(player.getUniqueId()),
                        "&7XP: &f" + format(skillTreeService.getExperience(player.getUniqueId())) + "&7/&f" + skillTreeService.getXpForNextLevel(player.getUniqueId()),
                        skillTreeService.getProgressBar(player.getUniqueId()),
                        "",
                        "&7Available Skill Points: &f" + points,
                        "&7Mob Kills: &f" + skillTreeService.getMobKillProgress(player.getUniqueId()) + "&7/&f" + configManager.getMobKillBatchSize(),
                        "",
                        "&7Choose a branch below to",
                        "&7view and unlock nodes."
                )
            ));
        }

        for (SkillTree tree : skillTreeService.getTrees()) {
            List<String> lore = new ArrayList<String>();
            lore.addAll(tree.getDescription());
            lore.add("");
            lore.add("&7Nodes: &f" + tree.getNodes().size());
            lore.add("&7Unlocked: &f" + skillTreeService.getUnlockedNodes(player.getUniqueId(), tree.getId()).size());
            lore.add("&7Complete: &f" + (skillTreeService.isTreeFullyUnlocked(player.getUniqueId(), tree.getId()) ? "Yes" : "No"));
            lore.add("&7Available Points: &f" + points);
            lore.add("");
            lore.add("&eClick to open");

            ItemStack icon = ItemUtil.createItem(tree.getIcon(), tree.getDisplayName(), lore);
            if (tree.getMenuSlot() < inventory.getSize()) {
                inventory.setItem(tree.getMenuSlot(), icon);
            }
        }

        int milestoneSlot = configManager.getMilestoneButtonSlot();
        if (milestoneSlot >= 0 && milestoneSlot < inventory.getSize()) {
            inventory.setItem(milestoneSlot, ItemUtil.createItem(
                    Material.CHEST,
                    "&eMilestones",
                    Arrays.asList(
                            "&7Claim milestone rewards tied",
                            "&7to your level progression.",
                            "",
                            "&7Current Map: &f" + configManager.getCurrentMapId(),
                            "&eClick to open"
                    )
            ));
        }

        int guideSlot = configManager.getGuideButtonSlot();
        if (guideSlot >= 0 && guideSlot < inventory.getSize()) {
            inventory.setItem(guideSlot, ItemUtil.createItem(
                Material.BOOK,
                "&7Branch Guide",
                Arrays.asList(
                        "&6Looting &7- loot speed bonuses",
                        "&aLuck &7- proc, tax, and capture bonuses",
                        "&cGrinding &7- boss, key, and XP bonuses",
                        "&dRebirth &7- endgame item preservation"
                )
            ));
        }

        return inventory;
    }

    private String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = ItemUtil.createItem(
                configManager.getFillerMaterial(),
                1,
                configManager.getFillerData(),
                "&8",
                Collections.<String>emptyList()
        );

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, filler);
            }
        }
    }
}
