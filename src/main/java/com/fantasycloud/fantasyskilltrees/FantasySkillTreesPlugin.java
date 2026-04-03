package com.fantasycloud.fantasyskilltrees;

import com.fantasycloud.fantasyskilltrees.command.SkillTreeCommand;
import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.config.TreeConfigLoader;
import com.fantasycloud.fantasyskilltrees.data.MilestoneClaimStorage;
import com.fantasycloud.fantasyskilltrees.data.MilestoneConfigStorage;
import com.fantasycloud.fantasyskilltrees.data.PlayerDataManager;
import com.fantasycloud.fantasyskilltrees.data.PlayerDataStorage;
import com.fantasycloud.fantasyskilltrees.gui.GuiManager;
import com.fantasycloud.fantasyskilltrees.gui.MainMenuGui;
import com.fantasycloud.fantasyskilltrees.gui.MilestoneMenuGui;
import com.fantasycloud.fantasyskilltrees.gui.TreeMenuGui;
import com.fantasycloud.fantasyskilltrees.integration.IntegrationManager;
import com.fantasycloud.fantasyskilltrees.item.SkillPointItemFactory;
import com.fantasycloud.fantasyskilltrees.listener.GuiClickListener;
import com.fantasycloud.fantasyskilltrees.listener.LegacyVaultListener;
import com.fantasycloud.fantasyskilltrees.listener.PlaceholderBossDamageListener;
import com.fantasycloud.fantasyskilltrees.listener.PlaceholderMobXpListener;
import com.fantasycloud.fantasyskilltrees.listener.PlayerDataListener;
import com.fantasycloud.fantasyskilltrees.listener.SkillPointItemListener;
import com.fantasycloud.fantasyskilltrees.service.LegacyVaultService;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class FantasySkillTreesPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private SkillTreeServiceImpl skillTreeService;
    private IntegrationManager integrationManager;
    private GuiManager guiManager;
    private SkillPointItemFactory skillPointItemFactory;
    private LegacyVaultService legacyVaultService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        TreeConfigLoader treeConfigLoader = new TreeConfigLoader(this);
        treeConfigLoader.syncBundledTreeDefaults(configManager.isTreeDefaultAutoSyncEnabled());

        this.skillPointItemFactory = new SkillPointItemFactory(configManager);
        PlayerDataStorage playerDataStorage = new PlayerDataStorage(this);
        MilestoneConfigStorage milestoneConfigStorage = new MilestoneConfigStorage(this, skillPointItemFactory);
        MilestoneClaimStorage milestoneClaimStorage = new MilestoneClaimStorage(this);
        PlayerDataManager playerDataManager = new PlayerDataManager(playerDataStorage);
        this.skillTreeService = new SkillTreeServiceImpl(this, configManager, treeConfigLoader, playerDataManager, milestoneConfigStorage, milestoneClaimStorage);
        this.skillTreeService.reloadTrees();
        this.skillTreeService.reloadMilestones();
        getServer().getServicesManager().register(SkillTreeService.class, skillTreeService, this, ServicePriority.Normal);

        this.integrationManager = new IntegrationManager(this, configManager, skillTreeService);
        this.integrationManager.initialize();
        this.legacyVaultService = new LegacyVaultService(this, skillTreeService);

        MainMenuGui mainMenuGui = new MainMenuGui(configManager, skillTreeService);
        TreeMenuGui treeMenuGui = new TreeMenuGui(configManager, skillTreeService);
        MilestoneMenuGui milestoneMenuGui = new MilestoneMenuGui(configManager, skillTreeService);
        this.guiManager = new GuiManager(this, skillTreeService, legacyVaultService, mainMenuGui, treeMenuGui, milestoneMenuGui);
        this.skillTreeService.setGuiManager(guiManager);

        SkillTreeCommand skillTreeCommand = new SkillTreeCommand(configManager, skillTreeService, guiManager, integrationManager, skillPointItemFactory, treeConfigLoader);
        PluginCommand command = getCommand("skilltree");
        if (command != null) {
            command.setExecutor(skillTreeCommand);
            command.setTabCompleter(skillTreeCommand);
        } else {
            getLogger().warning("Command /skilltree is missing from plugin.yml");
        }
        PluginCommand milestonesCommand = getCommand("milestones");
        if (milestonesCommand != null) {
            milestonesCommand.setExecutor(skillTreeCommand);
            milestonesCommand.setTabCompleter(skillTreeCommand);
        } else {
            getLogger().warning("Command /milestones is missing from plugin.yml");
        }
        PluginCommand openAllVaultsCommand = getCommand("openallvaults");
        if (openAllVaultsCommand != null) {
            openAllVaultsCommand.setExecutor(skillTreeCommand);
            openAllVaultsCommand.setTabCompleter(skillTreeCommand);
        } else {
            getLogger().warning("Command /openallvaults is missing from plugin.yml");
        }
        PluginCommand unlockAllVaultsCommand = getCommand("unlockallvaults");
        if (unlockAllVaultsCommand != null) {
            unlockAllVaultsCommand.setExecutor(skillTreeCommand);
            unlockAllVaultsCommand.setTabCompleter(skillTreeCommand);
        } else {
            getLogger().warning("Command /unlockallvaults is missing from plugin.yml");
        }

        getServer().getPluginManager().registerEvents(new PlayerDataListener(this, skillTreeService, guiManager), this);
        getServer().getPluginManager().registerEvents(new GuiClickListener(configManager, guiManager), this);
        getServer().getPluginManager().registerEvents(new LegacyVaultListener(this, guiManager, legacyVaultService), this);
        getServer().getPluginManager().registerEvents(new SkillPointItemListener(configManager, skillPointItemFactory, skillTreeService), this);

        if (configManager.isMobXpPlaceholderEnabled()) {
            getServer().getPluginManager().registerEvents(new PlaceholderMobXpListener(skillTreeService, integrationManager), this);
            getLogger().info("Registered placeholder Mob XP listener.");
        }
        if (configManager.isBossDamagePlaceholderEnabled()) {
            getServer().getPluginManager().registerEvents(new PlaceholderBossDamageListener(configManager, skillTreeService, integrationManager), this);
            getLogger().info("Registered placeholder Boss Damage listener.");
        }

        getLogger().info("FantasySkillTrees enabled with " + skillTreeService.getTrees().size() + " tree(s).");
    }

    @Override
    public void onDisable() {
        if (skillTreeService != null) {
            skillTreeService.saveAllPlayers();
        }
        if (integrationManager != null) {
            integrationManager.shutdown();
        }
        getServer().getServicesManager().unregisterAll(this);
    }

    public SkillTreeServiceImpl getSkillTreeService() {
        return skillTreeService;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public LegacyVaultService getLegacyVaultService() {
        return legacyVaultService;
    }
}
