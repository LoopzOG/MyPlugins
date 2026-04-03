package com.fantasycloud.fantasyskilltrees.listener;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.gui.GuiManager;
import com.fantasycloud.fantasyskilltrees.gui.MainMenuHolder;
import com.fantasycloud.fantasyskilltrees.gui.MilestoneMenuHolder;
import com.fantasycloud.fantasyskilltrees.gui.MilestoneRewardEditorHolder;
import com.fantasycloud.fantasyskilltrees.gui.TreeMenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiClickListener implements Listener {
    private final ConfigManager configManager;
    private final GuiManager guiManager;

    public GuiClickListener(ConfigManager configManager, GuiManager guiManager) {
        this.configManager = configManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MainMenuHolder) && !(holder instanceof TreeMenuHolder) && !(holder instanceof MilestoneMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        if (holder instanceof MainMenuHolder) {
            guiManager.handleMainMenuClick(player, slot);
            return;
        }

        if (holder instanceof MilestoneMenuHolder) {
            MilestoneMenuHolder milestoneHolder = (MilestoneMenuHolder) holder;
            guiManager.handleMilestoneMenuClick(player, milestoneHolder.isAdmin(), milestoneHolder.getPage(), slot);
            return;
        }

        TreeMenuHolder treeHolder = (TreeMenuHolder) holder;
        if (slot == configManager.getBackButtonSlot()) {
            guiManager.openMainMenu(player);
            return;
        }

        guiManager.handleTreeMenuClick(player, treeHolder.getTreeId(), slot);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MilestoneRewardEditorHolder)) {
            return;
        }

        guiManager.saveMilestoneRewardEditor((MilestoneRewardEditorHolder) holder, event.getInventory());
    }
}
