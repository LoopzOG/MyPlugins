package com.fantasycloud.fantasyskilltrees.gui;

import com.fantasycloud.fantasyskilltrees.model.SkillNode;
import com.fantasycloud.fantasyskilltrees.model.MilestoneDefinition;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import com.fantasycloud.fantasyskilltrees.gui.MainMenuHolder;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultClaimHolder;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultHolder;
import com.fantasycloud.fantasyskilltrees.gui.MilestoneMenuHolder;
import com.fantasycloud.fantasyskilltrees.gui.MilestoneRewardEditorHolder;
import com.fantasycloud.fantasyskilltrees.gui.TreeMenuHolder;
import com.fantasycloud.fantasyskilltrees.service.LegacyVaultService;
import com.fantasycloud.fantasyskilltrees.service.MilestoneClaimResult;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import com.fantasycloud.fantasyskilltrees.service.UnlockResult;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiManager {
    private final JavaPlugin plugin;
    private final SkillTreeServiceImpl skillTreeService;
    private final LegacyVaultService legacyVaultService;
    private final MainMenuGui mainMenuGui;
    private final TreeMenuGui treeMenuGui;
    private final MilestoneMenuGui milestoneMenuGui;

    public GuiManager(JavaPlugin plugin,
                      SkillTreeServiceImpl skillTreeService,
                      LegacyVaultService legacyVaultService,
                      MainMenuGui mainMenuGui,
                      TreeMenuGui treeMenuGui,
                      MilestoneMenuGui milestoneMenuGui) {
        this.plugin = plugin;
        this.skillTreeService = skillTreeService;
        this.legacyVaultService = legacyVaultService;
        this.mainMenuGui = mainMenuGui;
        this.treeMenuGui = treeMenuGui;
        this.milestoneMenuGui = milestoneMenuGui;
    }

    public void openMainMenu(Player player) {
        Inventory inventory = mainMenuGui.build(player);
        player.openInventory(inventory);
    }

    public void openTreeMenu(Player player, String treeId) {
        SkillTree tree = skillTreeService.getTree(treeId);
        if (tree == null) {
            player.sendMessage(ChatUtil.color("&cUnknown tree: " + treeId));
            return;
        }
        player.openInventory(treeMenuGui.build(player, tree));
    }

    public void openMilestoneMenu(Player player) {
        openMilestoneMenu(player, 0);
    }

    public void openMilestoneAdminMenu(Player player) {
        openMilestoneAdminMenu(player, 0);
    }

    public void openMilestoneMenu(Player player, int page) {
        player.openInventory(milestoneMenuGui.build(player, false, page));
    }

    public void openMilestoneAdminMenu(Player player, int page) {
        player.openInventory(milestoneMenuGui.build(player, true, page));
    }

    public void openMilestoneRewardEditor(Player player, String milestoneId) {
        MilestoneDefinition milestone = skillTreeService.getMilestone(milestoneId);
        if (milestone == null) {
            player.sendMessage(ChatUtil.color("&cUnknown milestone: " + milestoneId));
            return;
        }
        player.openInventory(milestoneMenuGui.buildRewardEditor(milestone));
    }

    public void handleMainMenuClick(Player player, int slot) {
        if (slot == mainMenuGuiSlotMilestones()) {
            openMilestoneMenu(player);
            return;
        }
        for (SkillTree tree : skillTreeService.getTrees()) {
            if (tree.getMenuSlot() == slot) {
                openTreeMenu(player, tree.getId());
                return;
            }
        }
    }

    public void handleTreeMenuClick(Player player, String treeId, int slot) {
        SkillTree tree = skillTreeService.getTree(treeId);
        if (tree == null) {
            return;
        }

        if ("rebirth".equalsIgnoreCase(treeId) && slot == TreeMenuGui.REBIRTH_VAULT_BUTTON_SLOT) {
            openLegacyVault(player);
            return;
        }

        for (SkillNode node : tree.getNodes().values()) {
            if (node.getSlot() != slot) {
                continue;
            }

            UnlockResult result = skillTreeService.unlockNode(player, treeId, node.getId());
            if (result == UnlockResult.SUCCESS) {
                player.sendMessage(ChatUtil.color("&aUnlocked node: &f" + node.getDisplayName()));
            } else if (result == UnlockResult.ALREADY_UNLOCKED) {
                player.sendMessage(ChatUtil.color("&eYou already unlocked this node."));
            } else if (result == UnlockResult.INSUFFICIENT_POINTS) {
                player.sendMessage(ChatUtil.color("&cYou do not have enough skill points."));
            } else if (result == UnlockResult.MISSING_PREREQUISITES) {
                player.sendMessage(ChatUtil.color("&cYou are missing prerequisites for this node."));
            } else if (result == UnlockResult.CANCELLED) {
                player.sendMessage(ChatUtil.color("&cThis unlock was cancelled by another plugin."));
            }

            openTreeMenu(player, treeId);
            return;
        }
    }

    public void handleMilestoneMenuClick(Player player, boolean admin, int page, int slot) {
        if (slot == mainMenuGuiBackSlot()) {
            openMainMenu(player);
            return;
        }

        if (slot == MilestoneMenuGui.PREVIOUS_PAGE_SLOT) {
            if (admin) {
                openMilestoneAdminMenu(player, page - 1);
            } else {
                openMilestoneMenu(player, page - 1);
            }
            return;
        }
        if (slot == MilestoneMenuGui.NEXT_PAGE_SLOT) {
            if (admin) {
                openMilestoneAdminMenu(player, page + 1);
            } else {
                openMilestoneMenu(player, page + 1);
            }
            return;
        }

        MilestoneDefinition milestone = milestoneMenuGui.getMilestoneForSlot(page, slot);
        if (milestone == null) {
            return;
        }

        if (admin) {
            openMilestoneRewardEditor(player, milestone.getId());
            return;
        }

        MilestoneClaimResult result = skillTreeService.claimMilestone(player, milestone.getId());
        if (result == MilestoneClaimResult.CLAIMED) {
            openMilestoneMenu(player, page);
            return;
        }
        if (result == MilestoneClaimResult.LOCKED) {
            player.sendMessage(ChatUtil.color(skillTreeService.getConfigManager().getMilestoneLockedMessage()));
        } else if (result == MilestoneClaimResult.ALREADY_CLAIMED) {
            player.sendMessage(ChatUtil.color(skillTreeService.getConfigManager().getMilestoneAlreadyClaimedMessage()));
        } else if (result == MilestoneClaimResult.IP_LOCKED) {
            player.sendMessage(ChatUtil.color(skillTreeService.getConfigManager().getMilestoneIpLockedMessage()));
        } else if (result == MilestoneClaimResult.NO_REWARDS) {
            player.sendMessage(ChatUtil.color(skillTreeService.getConfigManager().getMilestoneNoRewardsMessage()));
        }
        openMilestoneMenu(player, page);
    }

    public void saveMilestoneRewardEditor(MilestoneRewardEditorHolder holder, Inventory inventory) {
        List<ItemStack> rewards = new ArrayList<ItemStack>();
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null && stack.getType() != org.bukkit.Material.AIR) {
                rewards.add(stack.clone());
            }
        }
        skillTreeService.saveMilestoneRewards(holder.getMilestoneId(), rewards);
    }

    public void openLegacyVault(Player player) {
        if (!legacyVaultService.canAccess(player.getUniqueId())) {
            player.sendMessage(ChatUtil.color("&cUnlock Legacy Vault I in the Rebirth tree first."));
            return;
        }
        player.openInventory(legacyVaultService.build(player));
    }

    public boolean openClaimableLegacyVault(Player player) {
        UUID uuid = player.getUniqueId();
        if (!legacyVaultService.hasClaimableItems(uuid)) {
            return false;
        }

        player.openInventory(legacyVaultService.build(player));
        return true;
    }

    public boolean openPendingLegacyClaim(Player player) {
        UUID uuid = player.getUniqueId();
        if (!legacyVaultService.hasPendingClaimPrompt(uuid)) {
            return false;
        }

        player.openInventory(legacyVaultService.buildClaimInventory(player));
        return true;
    }

    public void enforcePendingLegacyClaim(final Player player) {
        new BukkitRunnable() {
            private int attempts = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (!legacyVaultService.hasPendingClaimPrompt(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof LegacyVaultClaimHolder)) {
                    openPendingLegacyClaim(player);
                }

                attempts++;
                if (attempts >= 10) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public boolean unlockLegacyVault(UUID uuid) {
        boolean unlocked = legacyVaultService.unlockStoredItems(uuid);
        if (!unlocked) {
            return false;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.getOpenInventory().getTopInventory().getHolder() instanceof LegacyVaultHolder) {
            player.openInventory(legacyVaultService.build(player));
        }
        return true;
    }

    public int unlockAllLegacyVaults() {
        int unlockedVaults = legacyVaultService.unlockAllStoredItems();
        refreshOpenLegacyVaults();
        return unlockedVaults;
    }

    public int queueLegacyLoginClaims() {
        return legacyVaultService.markAllClaimPromptsPending();
    }

    public void refreshOpenLegacyVaults() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof LegacyVaultHolder) {
                player.openInventory(legacyVaultService.build(player));
            }
        }
    }

    public void refreshOpenSkillTreeMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayerMenus(player);
        }
    }

    public void refreshOpenMilestoneMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MilestoneMenuHolder) {
                MilestoneMenuHolder holder = (MilestoneMenuHolder) player.getOpenInventory().getTopInventory().getHolder();
                if (holder.isAdmin()) {
                    openMilestoneAdminMenu(player, holder.getPage());
                } else {
                    openMilestoneMenu(player, holder.getPage());
                }
            }
        }
    }

    public void refreshPlayerMenus(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof MainMenuHolder) {
            openMainMenu(player);
            return;
        }
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof TreeMenuHolder) {
            TreeMenuHolder holder = (TreeMenuHolder) player.getOpenInventory().getTopInventory().getHolder();
            openTreeMenu(player, holder.getTreeId());
            return;
        }
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof MilestoneMenuHolder) {
            MilestoneMenuHolder holder = (MilestoneMenuHolder) player.getOpenInventory().getTopInventory().getHolder();
            if (holder.isAdmin()) {
                openMilestoneAdminMenu(player, holder.getPage());
            } else {
                openMilestoneMenu(player, holder.getPage());
            }
        }
    }

    private int mainMenuGuiSlotMilestones() {
        return skillTreeService.getConfigManager().getMilestoneButtonSlot();
    }

    private int mainMenuGuiBackSlot() {
        return skillTreeService.getConfigManager().getBackButtonSlot();
    }

}
