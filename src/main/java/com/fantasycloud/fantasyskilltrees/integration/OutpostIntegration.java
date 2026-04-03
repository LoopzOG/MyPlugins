package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class OutpostIntegration extends AbstractPluginIntegration {
    public OutpostIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "outpost", "Outpost");
    }

    public double getCaptureSpeedModifier(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.OUTPOST_CAPTURE_SPEED);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your Outpost API/events and apply getCaptureSpeedModifier(uuid).
    }
}
