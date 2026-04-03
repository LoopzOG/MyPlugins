package com.fantasycloud.fantasyskilltrees.gui;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.model.NodeRequirement;
import com.fantasycloud.fantasyskilltrees.model.SkillNode;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import com.fantasycloud.fantasyskilltrees.util.EffectFormatUtil;
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
import java.util.UUID;

public class TreeMenuGui {
    public static final int REBIRTH_VAULT_BUTTON_SLOT = 31;
    private static final int COMING_SOON_SLOT = 33;

    private final ConfigManager configManager;
    private final SkillTreeServiceImpl skillTreeService;

    public TreeMenuGui(ConfigManager configManager, SkillTreeServiceImpl skillTreeService) {
        this.configManager = configManager;
        this.skillTreeService = skillTreeService;
    }

    public Inventory build(Player player, SkillTree tree) {
        TreeMenuHolder holder = new TreeMenuHolder(tree.getId());
        String title = configManager.getTreeMenuTitleFormat().replace("%tree%", tree.getDisplayName());
        Inventory inventory = Bukkit.createInventory(holder, configManager.getTreeMenuSize(), ChatUtil.color(title));
        holder.setInventory(inventory);

        fillBackground(inventory);
        UUID uuid = player.getUniqueId();

        inventory.setItem(4, ItemUtil.createItem(
                tree.getIcon(),
                tree.getDisplayName(),
                Arrays.asList(
                        "&7Level: &f" + skillTreeService.getLevel(uuid),
                        "&7XP: &f" + format(skillTreeService.getExperience(uuid)) + "&7/&f" + skillTreeService.getXpForNextLevel(uuid),
                        skillTreeService.getProgressBar(uuid),
                        "",
                        "&7Unlocked Nodes: &f" + skillTreeService.getUnlockedNodes(uuid, tree.getId()).size() + "/" + tree.getNodes().size(),
                        "&7Available Points: &f" + skillTreeService.getAvailablePoints(uuid),
                        "",
                        "&7Locked nodes require prior tiers",
                        "&7before they can be purchased."
                )
        ));

        for (SkillNode node : tree.getNodes().values()) {
            if (node.getSlot() >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(node.getSlot(), buildNodeItem(uuid, tree, node));
        }

        addComingSoonPlaceholder(inventory, tree);
        addLegacyVaultButton(inventory, uuid, tree);

        int backSlot = configManager.getBackButtonSlot();
        if (backSlot >= 0 && backSlot < inventory.getSize()) {
            inventory.setItem(backSlot, ItemUtil.createItem(Material.ARROW, "&cBack", Collections.singletonList("&7Return to branch list")));
        }

        return inventory;
    }

    private ItemStack buildNodeItem(UUID uuid, SkillTree tree, SkillNode node) {
        boolean unlocked = skillTreeService.hasUnlocked(uuid, tree.getId(), node.getId());
        boolean unlockable = !unlocked && skillTreeService.meetsPrerequisites(uuid, node)
                && skillTreeService.getAvailablePoints(uuid) >= node.getCost();

        List<String> lore = new ArrayList<String>();
        lore.addAll(node.getDescription());
        lore.add("");
        lore.add("&7Cost: &f" + node.getCost() + " points");

        appendPrerequisiteLore(lore, uuid, node);
        appendEffectLore(lore, node);

        lore.add("");
        if (unlocked) {
            lore.add(configManager.getUnlockedLoreLine());
        } else if (unlockable) {
            lore.add(configManager.getUnlockableLoreLine());
        } else {
            lore.add(configManager.getLockedLoreLine());
        }

        return ItemUtil.createItem(node.getIcon(), statePrefix(unlocked, unlockable) + node.getDisplayName(), lore);
    }

    private void appendPrerequisiteLore(List<String> lore, UUID uuid, SkillNode node) {
        if (node.getPrerequisites().isEmpty()) {
            return;
        }

        lore.add("&7Prerequisites:");
        for (NodeRequirement requirement : node.getPrerequisites()) {
            boolean met = skillTreeService.hasUnlocked(uuid, requirement.getTreeId(), requirement.getNodeId());
            String marker = met ? "&a[OK] " : "&c[MISSING] ";
            lore.add(marker + "&7" + resolveRequirementName(requirement));
        }
    }

    private void appendEffectLore(List<String> lore, SkillNode node) {
        if (node.getEffects().isEmpty()) {
            return;
        }
        lore.add("&7Effects:");
        for (com.fantasycloud.fantasyskilltrees.effect.SkillEffect effect : node.getEffects()) {
            lore.add(EffectFormatUtil.toLoreLine(effect));
        }
    }

    private String statePrefix(boolean unlocked, boolean unlockable) {
        if (unlocked) {
            return "&a";
        }
        if (unlockable) {
            return "&e";
        }
        return "&c";
    }

    private String resolveRequirementName(NodeRequirement requirement) {
        if (requirement.getType() == com.fantasycloud.fantasyskilltrees.model.RequirementType.ALL_OTHER_TREES_FULLY_UNLOCKED) {
            return "Fully unlock every other skill tree";
        }
        if (requirement.getType() == com.fantasycloud.fantasyskilltrees.model.RequirementType.TREE_FULLY_UNLOCKED) {
            SkillTree requiredTree = skillTreeService.getTree(requirement.getTreeId());
            if (requiredTree == null) {
                return "Fully unlock tree: " + requirement.getTreeId();
            }
            return "Fully unlock " + ChatUtil.color(requiredTree.getDisplayName());
        }

        SkillTree tree = skillTreeService.getTree(requirement.getTreeId());
        if (tree == null) {
            return requirement.getTreeId() + ":" + requirement.getNodeId();
        }

        SkillNode requiredNode = tree.getNode(requirement.getNodeId());
        if (requiredNode == null) {
            return requirement.getTreeId() + ":" + requirement.getNodeId();
        }

        return ChatUtil.color(tree.getDisplayName()) + "&7 - " + requiredNode.getDisplayName();
    }

    private void addComingSoonPlaceholder(Inventory inventory, SkillTree tree) {
        if (!"looting".equalsIgnoreCase(tree.getId())) {
            return;
        }
        if (COMING_SOON_SLOT < 0 || COMING_SOON_SLOT >= inventory.getSize()) {
            return;
        }

        inventory.setItem(COMING_SOON_SLOT, ItemUtil.createItem(
                Material.BARRIER,
                "&7Coming Soon",
                Arrays.asList(
                        "&8Placeholder",
                        "",
                        "&7A fourth skill path will",
                        "&7be added here later."
                )
        ));
    }

    private void addLegacyVaultButton(Inventory inventory, UUID uuid, SkillTree tree) {
        if (!"rebirth".equalsIgnoreCase(tree.getId())) {
            return;
        }
        if (REBIRTH_VAULT_BUTTON_SLOT < 0 || REBIRTH_VAULT_BUTTON_SLOT >= inventory.getSize()) {
            return;
        }

        int unlockedSlots = skillTreeService.getLegacyVaultSlots(uuid);
        List<String> lore = new ArrayList<String>();
        lore.add("&7Access your preserved item storage.");
        lore.add("");
        lore.add("&7Unlocked Slots: &f" + unlockedSlots + "&7/2");
        lore.add("");
        if (unlockedSlots > 0) {
            lore.add("&eClick to open your legacy vault");
        } else {
            lore.add("&cUnlock Legacy Vault I first");
        }

        inventory.setItem(REBIRTH_VAULT_BUTTON_SLOT, ItemUtil.createItem(
                Material.ENDER_CHEST,
                unlockedSlots > 0 ? "&dOpen Legacy Vault" : "&8Legacy Vault Locked",
                lore
        ));
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

    private String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}
