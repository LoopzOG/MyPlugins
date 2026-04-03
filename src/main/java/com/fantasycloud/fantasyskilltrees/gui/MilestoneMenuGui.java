package com.fantasycloud.fantasyskilltrees.gui;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.model.MilestoneDefinition;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import com.fantasycloud.fantasyskilltrees.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MilestoneMenuGui {
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int INFO_SLOT = 47;
    public static final int NEXT_PAGE_SLOT = 53;
    private static final int[] CONTENT_SLOTS = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    private final ConfigManager configManager;
    private final SkillTreeServiceImpl skillTreeService;

    public MilestoneMenuGui(ConfigManager configManager, SkillTreeServiceImpl skillTreeService) {
        this.configManager = configManager;
        this.skillTreeService = skillTreeService;
    }

    public Inventory build(Player player, boolean admin, int requestedPage) {
        List<MilestoneDefinition> milestones = getSortedMilestones();
        int maxPage = Math.max(0, (milestones.size() - 1) / CONTENT_SLOTS.length);
        int page = Math.max(0, Math.min(requestedPage, maxPage));

        MilestoneMenuHolder holder = new MilestoneMenuHolder(admin, page);
        String baseTitle = admin ? configManager.getMilestoneMenuTitle() + " &7(Editor)" : configManager.getMilestoneMenuTitle();
        Inventory inventory = Bukkit.createInventory(holder, configManager.getMilestoneMenuSize(), trimTitle(ChatUtil.color(baseTitle)));
        holder.setInventory(inventory);

        fillBackground(inventory);
        populatePage(player, inventory, milestones, admin, page);
        inventory.setItem(INFO_SLOT, buildInfoItem(player.getUniqueId(), page, maxPage, admin));
        inventory.setItem(configManager.getBackButtonSlot(), ItemUtil.createItem(Material.ARROW, "&cBack", Collections.singletonList("&7Return to the main menu")));
        if (page > 0) {
            inventory.setItem(PREVIOUS_PAGE_SLOT, ItemUtil.createItem(Material.ARROW, "&ePrevious Page", Collections.singletonList("&7View earlier milestones")));
        }
        if (page < maxPage) {
            inventory.setItem(NEXT_PAGE_SLOT, ItemUtil.createItem(Material.ARROW, "&eNext Page", Collections.singletonList("&7View later milestones")));
        }

        return inventory;
    }

    public Inventory buildRewardEditor(MilestoneDefinition milestone) {
        MilestoneRewardEditorHolder holder = new MilestoneRewardEditorHolder(milestone.getId());
        String title = configManager.getMilestoneEditorTitle().replace("%milestone%", ChatColor.stripColor(ChatUtil.color(milestone.getDisplayName())));
        Inventory inventory = Bukkit.createInventory(holder, 54, trimTitle(ChatUtil.color(title)));
        holder.setInventory(inventory);

        int slot = 0;
        for (ItemStack reward : milestone.getRewards()) {
            if (slot >= inventory.getSize()) {
                break;
            }
            inventory.setItem(slot++, reward.clone());
        }
        return inventory;
    }

    public MilestoneDefinition getMilestoneForSlot(int page, int slot) {
        int indexOnPage = -1;
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (CONTENT_SLOTS[i] == slot) {
                indexOnPage = i;
                break;
            }
        }
        if (indexOnPage < 0) {
            return null;
        }

        List<MilestoneDefinition> milestones = getSortedMilestones();
        int absoluteIndex = (page * CONTENT_SLOTS.length) + indexOnPage;
        if (absoluteIndex < 0 || absoluteIndex >= milestones.size()) {
            return null;
        }
        return milestones.get(absoluteIndex);
    }

    private void populatePage(Player player, Inventory inventory, List<MilestoneDefinition> milestones, boolean admin, int page) {
        int start = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            int milestoneIndex = start + i;
            if (milestoneIndex >= milestones.size()) {
                break;
            }
            inventory.setItem(CONTENT_SLOTS[i], buildMilestoneItem(player, milestones.get(milestoneIndex), admin));
        }
    }

    private ItemStack buildMilestoneItem(Player player, MilestoneDefinition milestone, boolean admin) {
        UUID uuid = player.getUniqueId();
        boolean claimed = skillTreeService.hasClaimedMilestone(uuid, milestone.getId());
        boolean ipLocked = !claimed && skillTreeService.isMilestoneIpLocked(player, milestone.getId());
        boolean unlocked = skillTreeService.getLevel(uuid) >= milestone.getRequiredLevel();

        List<String> lore = new ArrayList<String>();
        lore.add("&f&lRewards:");
        appendRewardLore(lore, milestone);
        lore.add("");
        if (admin) {
            lore.add("&eClick to edit reward contents");
        } else if (claimed) {
            lore.add(configManager.getClaimedLoreLine());
        } else if (ipLocked) {
            lore.add(configManager.getIpLockedLoreLine());
        } else if (unlocked) {
            lore.add(configManager.getClaimableLoreLine());
        } else {
            lore.add(configManager.getLockedLoreLine());
        }

        short paneData = resolvePaneData(milestone, claimed, unlocked);
        String prefix = claimed ? "&a" : unlocked ? "&e" : "&c";
        return ItemUtil.createItem(
                Material.STAINED_GLASS_PANE,
                1,
                paneData,
                prefix + ChatColor.stripColor(ChatUtil.color(milestone.getDisplayName())),
                lore
        );
    }

    private void appendRewardLore(List<String> lore, MilestoneDefinition milestone) {
        List<ItemStack> rewards = milestone.getRewards();
        if (rewards.isEmpty()) {
            lore.add("&7None configured");
            return;
        }

        for (ItemStack reward : rewards) {
            if (reward == null || reward.getType() == Material.AIR) {
                continue;
            }
            lore.add("&7- &f" + getRewardName(reward));
        }
    }

    private String getRewardName(ItemStack reward) {
        if (reward.hasItemMeta() && reward.getItemMeta() != null && reward.getItemMeta().hasDisplayName()) {
            return reward.getItemMeta().getDisplayName();
        }

        String materialName = reward.getType().name().toLowerCase(java.util.Locale.US).replace('_', ' ');
        StringBuilder builder = new StringBuilder(materialName.length());
        boolean capitalize = true;
        for (int i = 0; i < materialName.length(); i++) {
            char current = materialName.charAt(i);
            if (capitalize && Character.isLetter(current)) {
                builder.append(Character.toUpperCase(current));
                capitalize = false;
            } else {
                builder.append(current);
            }
            if (current == ' ') {
                capitalize = true;
            }
        }
        return builder.toString();
    }

    private ItemStack buildInfoItem(UUID uuid, int page, int maxPage, boolean admin) {
        List<String> lore = new ArrayList<String>();
        lore.add("&7Level: &f" + skillTreeService.getLevel(uuid));
        lore.add("&7XP: &f" + format(skillTreeService.getExperience(uuid)) + "&7/&f" + skillTreeService.getXpForNextLevel(uuid));
        lore.add(skillTreeService.getProgressBar(uuid));
        lore.add("");
        lore.add("&7Page: &f" + (page + 1) + "&7/&f" + (maxPage + 1));
        lore.add(admin ? "&7Mode: &fReward Editor" : "&7Mode: &fClaim Rewards");
        return ItemUtil.createItem(Material.EXP_BOTTLE, "&bMilestone Track", lore);
    }

    private short resolvePaneData(MilestoneDefinition milestone, boolean claimed, boolean unlocked) {
        if (claimed) {
            return 5;
        }
        if (unlocked) {
            return 5;
        }
        return 14;
    }

    private List<MilestoneDefinition> getSortedMilestones() {
        List<MilestoneDefinition> milestones = new ArrayList<MilestoneDefinition>(skillTreeService.getMilestones());
        Collections.sort(milestones, new Comparator<MilestoneDefinition>() {
            @Override
            public int compare(MilestoneDefinition first, MilestoneDefinition second) {
                int levelCompare = Integer.compare(first.getRequiredLevel(), second.getRequiredLevel());
                if (levelCompare != 0) {
                    return levelCompare;
                }
                return first.getId().compareToIgnoreCase(second.getId());
            }
        });
        return milestones;
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

    private String trimTitle(String input) {
        return input.length() <= 32 ? input : input.substring(0, 32);
    }
}
