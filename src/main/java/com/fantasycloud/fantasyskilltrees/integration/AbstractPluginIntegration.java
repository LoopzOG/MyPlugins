package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractPluginIntegration implements PluginIntegration {
    protected final JavaPlugin plugin;
    protected final ConfigManager configManager;
    protected final SkillTreeService skillTreeService;
    private final String id;
    private final String defaultPluginName;
    private boolean hooked;

    protected AbstractPluginIntegration(JavaPlugin plugin,
                                        ConfigManager configManager,
                                        SkillTreeService skillTreeService,
                                        String id,
                                        String defaultPluginName) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.skillTreeService = skillTreeService;
        this.id = id;
        this.defaultPluginName = defaultPluginName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isHooked() {
        return hooked;
    }

    @Override
    public void initialize() {
        if (!configManager.isIntegrationEnabled(id)) {
            plugin.getLogger().info("[Integration] " + id + " disabled in config.");
            return;
        }

        String targetName = configManager.getIntegrationPluginName(id, defaultPluginName);
        Plugin target = Bukkit.getPluginManager().getPlugin(targetName);
        if (target == null || !target.isEnabled()) {
            plugin.getLogger().info("[Integration] " + id + " target plugin not found: " + targetName);
            return;
        }

        hooked = true;
        onHook(target);
        plugin.getLogger().info("[Integration] Hooked " + id + " -> " + targetName);
    }

    @Override
    public void shutdown() {
        if (!hooked) {
            return;
        }
        hooked = false;
        onUnhook();
    }

    protected abstract void onHook(Plugin targetPlugin);

    protected void onUnhook() {
        // Optional override.
    }
}
