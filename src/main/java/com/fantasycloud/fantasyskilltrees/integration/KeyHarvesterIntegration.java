package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class KeyHarvesterIntegration extends AbstractPluginIntegration {
    public KeyHarvesterIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "keyharvester", "KeyHarvester");
    }

    public double getKeyDropChance(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.KEY_DROP_CHANCE);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your KeyHarvester API/events and apply getKeyDropChance(uuid).
    }
}
