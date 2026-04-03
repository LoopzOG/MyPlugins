package com.fantasycloud.fantasyskilltrees.listener;

import com.fantasycloud.fantasyskilltrees.gui.GuiManager;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultClaimHolder;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultConfirmHolder;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultHolder;
import com.fantasycloud.fantasyskilltrees.model.LegacyVaultEntry;
import com.fantasycloud.fantasyskilltrees.service.LegacyVaultService;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class LegacyVaultListener implements Listener {
    private final JavaPlugin plugin;
    private final GuiManager guiManager;
    private final LegacyVaultService legacyVaultService;

    public LegacyVaultListener(JavaPlugin plugin, GuiManager guiManager, LegacyVaultService legacyVaultService) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.legacyVaultService = legacyVaultService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof LegacyVaultClaimHolder) {
            handleClaimClick(event, (LegacyVaultClaimHolder) inventory.getHolder());
            return;
        }
        if (inventory.getHolder() instanceof LegacyVaultConfirmHolder) {
            handleConfirmClick(event, (LegacyVaultConfirmHolder) inventory.getHolder());
            return;
        }
        if (!(inventory.getHolder() instanceof LegacyVaultHolder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        LegacyVaultHolder holder = (LegacyVaultHolder) inventory.getHolder();
        int rawSlot = event.getRawSlot();
        event.setCancelled(true);
        if (rawSlot < 0) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (rawSlot == legacyVaultService.getBackSlot()) {
            guiManager.openTreeMenu((Player) event.getWhoClicked(), "rebirth");
            return;
        }

        if (rawSlot < inventory.getSize()) {
            handleVaultTopClick(player, holder, rawSlot);
            return;
        }

        handlePlayerInventoryClick(player, holder, event.getSlot(), event.getCurrentItem());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof LegacyVaultHolder
                || inventory.getHolder() instanceof LegacyVaultConfirmHolder
                || inventory.getHolder() instanceof LegacyVaultClaimHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof LegacyVaultClaimHolder)) {
            return;
        }

        LegacyVaultClaimHolder holder = (LegacyVaultClaimHolder) inventory.getHolder();
        if (!legacyVaultService.hasPendingClaimPrompt(holder.getOwnerUuid())) {
            return;
        }

        if (event.getPlayer() instanceof Player) {
            final Player player = (Player) event.getPlayer();
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    guiManager.openPendingLegacyClaim(player);
                }
            }, 2L);
        }
    }

    private void handleVaultTopClick(Player player, LegacyVaultHolder holder, int rawSlot) {
        if (!legacyVaultService.isStorageSlot(rawSlot, holder.getAccessibleSlots())) {
            return;
        }

        int vaultIndex = legacyVaultService.getVaultIndex(rawSlot, holder.getAccessibleSlots());
        LegacyVaultEntry entry = legacyVaultService.getEntry(holder.getOwnerUuid(), vaultIndex);
        if (entry == null || entry.isEmpty()) {
            if (!holder.isDepositEnabled()) {
                player.sendMessage(ChatUtil.color("&eThere is no claimable item in that slot."));
                return;
            }
            player.sendMessage(ChatUtil.color("&eClick an item in your inventory below to add it to the vault."));
            return;
        }

        if (entry.isLocked()) {
            player.sendMessage(ChatUtil.color("&cThis item is locked until the next map or until an admin runs /skilltree unlockvault."));
            return;
        }

        ItemStack withdrawn = legacyVaultService.withdrawUnlockedItem(holder.getOwnerUuid(), vaultIndex);
        if (withdrawn == null) {
            player.sendMessage(ChatUtil.color("&cThat vault item could not be withdrawn."));
            return;
        }

        if (!player.getInventory().addItem(withdrawn).isEmpty()) {
            legacyVaultService.restoreItem(holder.getOwnerUuid(), vaultIndex, withdrawn, false);
            player.sendMessage(ChatUtil.color("&cYou need at least one free inventory slot to withdraw that item."));
            return;
        }

        player.sendMessage(ChatUtil.color("&aRemoved item from your legacy vault."));
        player.openInventory(legacyVaultService.build(player));
    }

    private void handlePlayerInventoryClick(Player player, LegacyVaultHolder holder, int sourceSlot, ItemStack currentItem) {
        if (!holder.isDepositEnabled()) {
            player.sendMessage(ChatUtil.color("&cYou can only claim legacy vault items right now."));
            return;
        }
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }

        int targetVaultIndex = legacyVaultService.findFirstEmptyUnlockedSlot(holder.getOwnerUuid());
        if (targetVaultIndex < 0) {
            player.sendMessage(ChatUtil.color("&cYour legacy vault has no empty unlocked slots."));
            return;
        }

        ItemStack singleItem = currentItem.clone();
        singleItem.setAmount(1);
        player.openInventory(legacyVaultService.buildConfirmInventory(player, sourceSlot, targetVaultIndex, singleItem));
    }

    private void handleConfirmClick(InventoryClickEvent event, LegacyVaultConfirmHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int rawSlot = event.getRawSlot();
        if (rawSlot == legacyVaultService.getConfirmCancelSlot()) {
            player.openInventory(legacyVaultService.build(player));
            return;
        }
        if (rawSlot != legacyVaultService.getConfirmAcceptSlot()) {
            return;
        }

        PlayerInventory playerInventory = player.getInventory();
        ItemStack currentItem = playerInventory.getItem(holder.getSourceSlot());
        ItemStack pendingItem = holder.getPendingItem();
        if (!matchesPendingItem(currentItem, pendingItem)) {
            player.sendMessage(ChatUtil.color("&cThat item is no longer available to deposit."));
            player.openInventory(legacyVaultService.build(player));
            return;
        }

        if (!legacyVaultService.storeLockedItem(holder.getOwnerUuid(), holder.getTargetVaultIndex(), pendingItem)) {
            player.sendMessage(ChatUtil.color("&cThat legacy vault slot is no longer available."));
            player.openInventory(legacyVaultService.build(player));
            return;
        }

        playerInventory.setItem(holder.getSourceSlot(), reduceOrClear(currentItem, pendingItem.getAmount()));
        player.sendMessage(ChatUtil.color("&aOne item was stored in your legacy vault. It is locked until next map or /skilltree unlockvault."));
        player.openInventory(legacyVaultService.build(player));
    }

    private void handleClaimClick(InventoryClickEvent event, LegacyVaultClaimHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int rawSlot = event.getRawSlot();
        if (rawSlot == legacyVaultService.getClaimAcceptSlot()) {
            int claimedItems = legacyVaultService.claimPendingItems(player);
            player.closeInventory();
            if (claimedItems <= 0) {
                player.sendMessage(ChatUtil.color("&eThere were no legacy vault items left to claim."));
                return;
            }
            player.sendMessage(ChatUtil.color("&aClaimed &f" + claimedItems + "&a legacy vault item(s)."));
            return;
        }

        if (rawSlot == legacyVaultService.getClaimDeclineSlot()) {
            legacyVaultService.discardPendingItems(holder.getOwnerUuid());
            player.closeInventory();
            player.sendMessage(ChatUtil.color("&cYou declined your one-time legacy vault claim. The items were deleted."));
        }
    }

    private boolean matchesPendingItem(ItemStack currentItem, ItemStack pendingItem) {
        if (currentItem == null || pendingItem == null) {
            return false;
        }
        return currentItem.isSimilar(pendingItem) && currentItem.getAmount() >= pendingItem.getAmount();
    }

    private ItemStack reduceOrClear(ItemStack itemStack, int amount) {
        if (itemStack == null) {
            return null;
        }
        int remaining = itemStack.getAmount() - amount;
        if (remaining <= 0) {
            return null;
        }
        ItemStack clone = itemStack.clone();
        clone.setAmount(remaining);
        return clone;
    }
}
