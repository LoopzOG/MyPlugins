package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EnvoyIntegration extends AbstractPluginIntegration {
    public EnvoyIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "envoy", "Envoy");
    }

    public double getLootRevealSpeedModifier(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.LOOT_REVEAL_SPEED);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your Envoy API/events and apply getLootRevealSpeedModifier(uuid).
    }
}
