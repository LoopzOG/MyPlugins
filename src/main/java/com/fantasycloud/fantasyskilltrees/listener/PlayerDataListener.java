package com.fantasycloud.fantasyskilltrees.listener;

import com.fantasycloud.fantasyskilltrees.gui.GuiManager;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataListener implements Listener {
    private final JavaPlugin plugin;
    private final SkillTreeServiceImpl skillTreeService;
    private final GuiManager guiManager;

    public PlayerDataListener(JavaPlugin plugin, SkillTreeServiceImpl skillTreeService, GuiManager guiManager) {
        this.plugin = plugin;
        this.skillTreeService = skillTreeService;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        skillTreeService.loadPlayer(player.getUniqueId());
        guiManager.enforcePendingLegacyClaim(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        skillTreeService.saveAndUnloadPlayer(event.getPlayer().getUniqueId());
    }
}
