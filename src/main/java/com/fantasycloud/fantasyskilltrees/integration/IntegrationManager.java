package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntegrationManager {
    private final List<PluginIntegration> integrations = new ArrayList<PluginIntegration>();

    public IntegrationManager(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        integrations.add(new ConquestIntegration(plugin, configManager, skillTreeService));
        integrations.add(new EnvoyIntegration(plugin, configManager, skillTreeService));
        integrations.add(new JackpotIntegration(plugin, configManager, skillTreeService));
        integrations.add(new CoinFlipIntegration(plugin, configManager, skillTreeService));
        integrations.add(new RevealerIntegration(plugin, configManager, skillTreeService));
        integrations.add(new OutpostIntegration(plugin, configManager, skillTreeService));
        integrations.add(new KeyHarvesterIntegration(plugin, configManager, skillTreeService));
        integrations.add(new BossIntegration(plugin, configManager, skillTreeService));
        integrations.add(new MobXPIntegration(plugin, configManager, skillTreeService));
    }

    public void initialize() {
        for (PluginIntegration integration : integrations) {
            integration.initialize();
        }
    }

    public void shutdown() {
        for (PluginIntegration integration : integrations) {
            integration.shutdown();
        }
    }

    public boolean isHooked(String id) {
        for (PluginIntegration integration : integrations) {
            if (integration.getId().equalsIgnoreCase(id)) {
                return integration.isHooked();
            }
        }
        return false;
    }

    public List<PluginIntegration> getIntegrations() {
        return Collections.unmodifiableList(integrations);
    }
}
