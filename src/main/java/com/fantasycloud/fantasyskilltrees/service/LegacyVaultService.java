package com.fantasycloud.fantasyskilltrees.service;

import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultConfirmHolder;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultClaimHolder;
import com.fantasycloud.fantasyskilltrees.gui.LegacyVaultHolder;
import com.fantasycloud.fantasyskilltrees.model.LegacyVaultEntry;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import com.fantasycloud.fantasyskilltrees.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class LegacyVaultService {
    private static final int INVENTORY_SIZE = 9;
    private static final int CONFIRM_SIZE = 27;
    private static final int INFO_SLOT = 4;
    private static final int BACK_SLOT = 8;
    private static final int CONFIRM_ITEM_SLOT = 13;
    private static final int CONFIRM_ACCEPT_SLOT = 11;
    private static final int CONFIRM_CANCEL_SLOT = 15;
    private static final int CLAIM_INVENTORY_SIZE = 27;
    private static final int CLAIM_INFO_SLOT = 4;
    private static final int CLAIM_ACCEPT_SLOT = 20;
    private static final int CLAIM_DECLINE_SLOT = 24;
    private static final int[] CLAIM_ITEM_SLOTS = new int[]{11, 15};
    private static final int[] STORAGE_SLOTS = new int[]{2, 6};
    private static final String CLAIM_ON_LOGIN_PATH = "claim-on-login";

    private final JavaPlugin plugin;
    private final SkillTreeServiceImpl skillTreeService;
    private final File dataFolder;

    public LegacyVaultService(JavaPlugin plugin, SkillTreeServiceImpl skillTreeService) {
        this.plugin = plugin;
        this.skillTreeService = skillTreeService;
        this.dataFolder = new File(plugin.getDataFolder(), "legacyvaults");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create legacy vault folder: " + dataFolder.getAbsolutePath());
        }
    }

    public boolean canAccess(UUID uuid) {
        return getUnlockedSlotCount(uuid) > 0 || hasClaimableItems(uuid);
    }

    public int getUnlockedSlotCount(UUID uuid) {
        return skillTreeService.getLegacyVaultSlots(uuid);
    }

    public boolean hasClaimableItems(UUID uuid) {
        LegacyVaultEntry[] entries = loadEntries(uuid);
        for (int index = 0; index < entries.length; index++) {
            LegacyVaultEntry entry = entries[index];
            if (entry != null && !entry.isEmpty() && !entry.isLocked()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPendingClaimPrompt(UUID uuid) {
        File file = getVaultFile(uuid);
        if (!file.exists()) {
            return false;
        }
        if (!hasClaimableItems(uuid)) {
            clearPendingClaimPrompt(uuid);
            return false;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getBoolean(CLAIM_ON_LOGIN_PATH, false);
    }

    public int markAllClaimPromptsPending() {
        File[] files = dataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null || files.length == 0) {
            return 0;
        }

        int preparedClaims = 0;
        for (File file : files) {
            String rawName = file.getName();
            String uuidValue = rawName.substring(0, rawName.length() - 4);
            try {
                UUID uuid = UUID.fromString(uuidValue);
                if (hasClaimableItems(uuid)) {
                    setPendingClaimPrompt(uuid, true);
                    preparedClaims++;
                } else {
                    setPendingClaimPrompt(uuid, false);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed filenames in the vault folder.
            }
        }
        return preparedClaims;
    }

    public void clearPendingClaimPrompt(UUID uuid) {
        setPendingClaimPrompt(uuid, false);
    }

    public Inventory build(Player player) {
        UUID uuid = player.getUniqueId();
        int unlockedSlots = getUnlockedSlotCount(uuid);
        int accessibleSlots = getAccessibleSlotCount(uuid);
        boolean depositEnabled = unlockedSlots > 0;
        LegacyVaultHolder holder = new LegacyVaultHolder(uuid, accessibleSlots, depositEnabled);
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, ChatUtil.color("&5Legacy Vault"));
        holder.setInventory(inventory);

        fillBackground(inventory);

        inventory.setItem(INFO_SLOT, ItemUtil.createItem(
                Material.ENDER_CHEST,
                "&dLegacy Vault",
                Arrays.asList(
                        "&7Stored items remain outside",
                        "&7normal skill reset data.",
                        "",
                        "&7Unlocked Slots: &f" + unlockedSlots + "&7/2",
                        "&7Each vault slot stores exactly",
                        "&7one item only.",
                        "",
                        depositEnabled ? "&7Click an item in your inventory" : "&7Click a stored item to claim it.",
                        depositEnabled ? "&7below to deposit it." : "&7Deposits are disabled after reset."
                )
        ));

        inventory.setItem(BACK_SLOT, ItemUtil.createItem(
                Material.ARROW,
                "&cBack",
                Collections.singletonList("&7Return to the Rebirth tree")
        ));

        LegacyVaultEntry[] entries = loadEntries(uuid);
        for (int index = 0; index < STORAGE_SLOTS.length; index++) {
            int inventorySlot = STORAGE_SLOTS[index];
            if (index < accessibleSlots) {
                LegacyVaultEntry entry = entries[index];
                if (entry != null && !entry.isEmpty()) {
                    inventory.setItem(inventorySlot, entry.isLocked() ? createLockedDisplay(entry.getItemStack()) : entry.getItemStack());
                }
                continue;
            }

            inventory.setItem(inventorySlot, ItemUtil.createItem(
                    Material.BARRIER,
                    "&cLocked Slot",
                    Arrays.asList(
                            "&7Unlock the next Legacy Vault tier",
                            "&7in the Rebirth tree to use this slot."
                    )
            ));
        }

        return inventory;
    }

    public Inventory buildClaimInventory(Player player) {
        UUID uuid = player.getUniqueId();
        LegacyVaultClaimHolder holder = new LegacyVaultClaimHolder(uuid);
        Inventory inventory = Bukkit.createInventory(holder, CLAIM_INVENTORY_SIZE, ChatUtil.color("&5Legacy Vault Claim"));
        holder.setInventory(inventory);

        fillClaimBackground(inventory);
        inventory.setItem(CLAIM_INFO_SLOT, ItemUtil.createItem(
                Material.ENDER_CHEST,
                "&dLegacy Vault Claim",
                Arrays.asList(
                        "&7This is your one-time legacy vault claim.",
                        "&7Claim the items below now.",
                        "",
                        "&cIf you decline this menu,",
                        "&cyour legacy vault items are deleted."
                )
        ));
        inventory.setItem(CLAIM_ACCEPT_SLOT, ItemUtil.createItem(
                Material.EMERALD_BLOCK,
                "&aClaim Items",
                Arrays.asList(
                        "&7Move all shown items into your inventory.",
                        "&7If your inventory is full, extras drop",
                        "&7at your feet."
                )
        ));
        inventory.setItem(CLAIM_DECLINE_SLOT, ItemUtil.createItem(
                Material.REDSTONE_BLOCK,
                "&cDecline Claim",
                Arrays.asList(
                        "&7Delete all legacy vault claim items.",
                        "&cThis cannot be undone."
                )
        ));

        LegacyVaultEntry[] entries = loadEntries(uuid);
        int claimSlotIndex = 0;
        for (LegacyVaultEntry entry : entries) {
            if (entry == null || entry.isEmpty() || entry.isLocked()) {
                continue;
            }
            if (claimSlotIndex >= CLAIM_ITEM_SLOTS.length) {
                break;
            }
            inventory.setItem(CLAIM_ITEM_SLOTS[claimSlotIndex], entry.getItemStack());
            claimSlotIndex++;
        }

        return inventory;
    }

    public Inventory buildConfirmInventory(Player player, int sourceSlot, int targetVaultIndex, ItemStack pendingItem) {
        LegacyVaultConfirmHolder holder = new LegacyVaultConfirmHolder(player.getUniqueId(), sourceSlot, targetVaultIndex, pendingItem);
        Inventory inventory = Bukkit.createInventory(holder, CONFIRM_SIZE, ChatUtil.color("&4Confirm Vault Deposit"));
        holder.setInventory(inventory);

        fillConfirmBackground(inventory);
        inventory.setItem(CONFIRM_ITEM_SLOT, pendingItem == null ? new ItemStack(Material.AIR) : pendingItem.clone());
        inventory.setItem(CONFIRM_ACCEPT_SLOT, ItemUtil.createItem(
                Material.EMERALD_BLOCK,
                "&aConfirm",
                Arrays.asList(
                        "&7Add one copy of this item",
                        "&7to your legacy vault.",
                        "&cYou will not be able to retrieve it",
                        "&cuntil the next map or an admin unlocks it."
                )
        ));
        inventory.setItem(CONFIRM_CANCEL_SLOT, ItemUtil.createItem(
                Material.REDSTONE_BLOCK,
                "&cCancel",
                Collections.singletonList("&7Return to the legacy vault")
        ));
        return inventory;
    }

    public boolean isStorageSlot(int slot, int unlockedSlots) {
        for (int index = 0; index < unlockedSlots && index < STORAGE_SLOTS.length; index++) {
            if (STORAGE_SLOTS[index] == slot) {
                return true;
            }
        }
        return false;
    }

    public int getVaultIndex(int inventorySlot, int unlockedSlots) {
        for (int index = 0; index < unlockedSlots && index < STORAGE_SLOTS.length; index++) {
            if (STORAGE_SLOTS[index] == inventorySlot) {
                return index;
            }
        }
        return -1;
    }

    public int findFirstEmptyUnlockedSlot(UUID uuid) {
        int unlockedSlots = getUnlockedSlotCount(uuid);
        LegacyVaultEntry[] entries = loadEntries(uuid);
        for (int index = 0; index < unlockedSlots && index < STORAGE_SLOTS.length; index++) {
            LegacyVaultEntry entry = entries[index];
            if (entry == null || entry.isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    public int getAccessibleSlotCount(UUID uuid) {
        int accessibleSlots = getUnlockedSlotCount(uuid);
        LegacyVaultEntry[] entries = loadEntries(uuid);
        for (int index = 0; index < entries.length; index++) {
            LegacyVaultEntry entry = entries[index];
            if (entry != null && !entry.isEmpty()) {
                accessibleSlots = Math.max(accessibleSlots, index + 1);
            }
        }
        return Math.min(STORAGE_SLOTS.length, accessibleSlots);
    }

    public LegacyVaultEntry getEntry(UUID uuid, int vaultIndex) {
        if (vaultIndex < 0 || vaultIndex >= STORAGE_SLOTS.length) {
            return null;
        }
        return loadEntries(uuid)[vaultIndex];
    }

    public int getBackSlot() {
        return BACK_SLOT;
    }

    public int getConfirmAcceptSlot() {
        return CONFIRM_ACCEPT_SLOT;
    }

    public int getConfirmCancelSlot() {
        return CONFIRM_CANCEL_SLOT;
    }

    public int getClaimAcceptSlot() {
        return CLAIM_ACCEPT_SLOT;
    }

    public int getClaimDeclineSlot() {
        return CLAIM_DECLINE_SLOT;
    }

    public boolean storeLockedItem(UUID uuid, int vaultIndex, ItemStack itemStack) {
        return storeItem(uuid, vaultIndex, itemStack, true, false);
    }

    public boolean restoreItem(UUID uuid, int vaultIndex, ItemStack itemStack, boolean locked) {
        return storeItem(uuid, vaultIndex, itemStack, locked, true);
    }

    private boolean storeItem(UUID uuid, int vaultIndex, ItemStack itemStack, boolean locked, boolean overwrite) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        if (vaultIndex < 0 || vaultIndex >= STORAGE_SLOTS.length || vaultIndex >= getUnlockedSlotCount(uuid)) {
            return false;
        }

        LegacyVaultEntry[] entries = loadEntries(uuid);
        LegacyVaultEntry existing = entries[vaultIndex];
        if (!overwrite && existing != null && !existing.isEmpty()) {
            return false;
        }

        entries[vaultIndex] = new LegacyVaultEntry(singleItem(itemStack), locked);
        saveEntries(uuid, entries);
        return true;
    }

    public ItemStack withdrawUnlockedItem(UUID uuid, int vaultIndex) {
        if (vaultIndex < 0 || vaultIndex >= STORAGE_SLOTS.length) {
            return null;
        }

        LegacyVaultEntry[] entries = loadEntries(uuid);
        LegacyVaultEntry entry = entries[vaultIndex];
        if (entry == null || entry.isEmpty() || entry.isLocked()) {
            return null;
        }

        entries[vaultIndex] = new LegacyVaultEntry(null, false);
        saveEntries(uuid, entries);
        return entry.getItemStack();
    }

    public boolean unlockStoredItems(UUID uuid) {
        boolean changed = false;
        LegacyVaultEntry[] entries = loadEntries(uuid);
        for (int index = 0; index < entries.length; index++) {
            LegacyVaultEntry entry = entries[index];
            if (entry == null || entry.isEmpty() || !entry.isLocked()) {
                continue;
            }
            entries[index] = new LegacyVaultEntry(entry.getItemStack(), false);
            changed = true;
        }

        if (changed) {
            saveEntries(uuid, entries);
        }
        return changed;
    }

    public int claimPendingItems(Player player) {
        UUID uuid = player.getUniqueId();
        LegacyVaultEntry[] entries = loadEntries(uuid);
        int claimedItems = 0;
        boolean changed = false;
        for (int index = 0; index < entries.length; index++) {
            LegacyVaultEntry entry = entries[index];
            if (entry == null || entry.isEmpty() || entry.isLocked()) {
                continue;
            }

            ItemStack itemStack = entry.getItemStack();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                entries[index] = new LegacyVaultEntry(null, false);
                changed = true;
                continue;
            }

            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack.clone());
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }

            entries[index] = new LegacyVaultEntry(null, false);
            claimedItems++;
            changed = true;
        }

        if (changed) {
            saveEntries(uuid, entries);
        }
        clearPendingClaimPrompt(uuid);
        return claimedItems;
    }

    public boolean discardPendingItems(UUID uuid) {
        LegacyVaultEntry[] entries = loadEntries(uuid);
        boolean changed = false;
        for (int index = 0; index < entries.length; index++) {
            LegacyVaultEntry entry = entries[index];
            if (entry == null || entry.isEmpty() || entry.isLocked()) {
                continue;
            }
            entries[index] = new LegacyVaultEntry(null, false);
            changed = true;
        }

        if (changed) {
            saveEntries(uuid, entries);
        }
        clearPendingClaimPrompt(uuid);
        return changed;
    }

    public int unlockAllStoredItems() {
        File[] files = dataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null || files.length == 0) {
            return 0;
        }

        int unlockedVaults = 0;
        for (File file : files) {
            String rawName = file.getName();
            String uuidValue = rawName.substring(0, rawName.length() - 4);
            try {
                UUID uuid = UUID.fromString(uuidValue);
                if (unlockStoredItems(uuid)) {
                    unlockedVaults++;
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed filenames in the vault folder.
            }
        }
        return unlockedVaults;
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = ItemUtil.createItem(
                Material.STAINED_GLASS_PANE,
                1,
                (short) 15,
                "&8",
                Collections.<String>emptyList()
        );

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot == INFO_SLOT || slot == BACK_SLOT || isAnyStorageSlot(slot)) {
                continue;
            }
            inventory.setItem(slot, filler);
        }
    }

    private void fillConfirmBackground(Inventory inventory) {
        ItemStack filler = ItemUtil.createItem(
                Material.STAINED_GLASS_PANE,
                1,
                (short) 15,
                "&8",
                Collections.<String>emptyList()
        );

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot == CONFIRM_ITEM_SLOT || slot == CONFIRM_ACCEPT_SLOT || slot == CONFIRM_CANCEL_SLOT) {
                continue;
            }
            inventory.setItem(slot, filler);
        }
    }

    private void fillClaimBackground(Inventory inventory) {
        ItemStack filler = ItemUtil.createItem(
                Material.STAINED_GLASS_PANE,
                1,
                (short) 15,
                "&8",
                Collections.<String>emptyList()
        );

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot == CLAIM_INFO_SLOT || slot == CLAIM_ACCEPT_SLOT || slot == CLAIM_DECLINE_SLOT) {
                continue;
            }
            if (isClaimItemSlot(slot)) {
                continue;
            }
            inventory.setItem(slot, filler);
        }
    }

    private boolean isAnyStorageSlot(int slot) {
        for (int storageSlot : STORAGE_SLOTS) {
            if (storageSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private boolean isClaimItemSlot(int slot) {
        for (int claimSlot : CLAIM_ITEM_SLOTS) {
            if (claimSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private LegacyVaultEntry[] loadEntries(UUID uuid) {
        LegacyVaultEntry[] entries = new LegacyVaultEntry[STORAGE_SLOTS.length];
        File file = getVaultFile(uuid);
        if (!file.exists()) {
            return entries;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (int index = 0; index < entries.length; index++) {
            ItemStack itemStack = cloneOrNull(config.getItemStack("slots." + index + ".item"));
            boolean locked = config.getBoolean("slots." + index + ".locked", itemStack != null);
            entries[index] = new LegacyVaultEntry(singleItem(itemStack), locked);
        }
        return entries;
    }

    private void saveEntries(UUID uuid, LegacyVaultEntry[] entries) {
        File file = getVaultFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        for (int index = 0; index < STORAGE_SLOTS.length; index++) {
            LegacyVaultEntry entry = index < entries.length ? entries[index] : null;
            config.set("slots." + index + ".item", entry == null ? null : entry.getItemStack());
            config.set("slots." + index + ".locked", entry != null && !entry.isEmpty() && entry.isLocked());
        }
        config.set(CLAIM_ON_LOGIN_PATH, hasStoredClaimPrompt(uuid));

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save legacy vault for " + uuid, exception);
        }
    }

    private File getVaultFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    private void setPendingClaimPrompt(UUID uuid, boolean pending) {
        File file = getVaultFile(uuid);
        if (!pending && !file.exists()) {
            return;
        }
        YamlConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        config.set(CLAIM_ON_LOGIN_PATH, pending);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save legacy vault prompt state for " + uuid, exception);
        }
    }

    private boolean hasStoredClaimPrompt(UUID uuid) {
        File file = getVaultFile(uuid);
        if (!file.exists()) {
            return false;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getBoolean(CLAIM_ON_LOGIN_PATH, false);
    }

    private ItemStack createLockedDisplay(ItemStack itemStack) {
        ItemStack display = itemStack.clone();
        ItemMeta meta = display.getItemMeta();
        List<String> lore = new ArrayList<String>();
        if (meta != null && meta.getLore() != null) {
            lore.addAll(meta.getLore());
        }
        if (!lore.isEmpty()) {
            lore.add("");
        }
        lore.add(ChatUtil.color("&4Locked In Vault"));
        lore.add(ChatUtil.color("&7Available next map or after"));
        lore.add(ChatUtil.color("&7an admin runs /skilltree unlockvault"));
        if (meta != null) {
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    private ItemStack cloneOrNull(ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }

    private ItemStack singleItem(ItemStack itemStack) {
        ItemStack clone = cloneOrNull(itemStack);
        if (clone != null && clone.getAmount() > 1) {
            clone.setAmount(1);
        }
        return clone;
    }
}
