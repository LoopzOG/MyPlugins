package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class RevealerIntegration extends AbstractPluginIntegration {
    public RevealerIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "revealer", "Revealer");
    }

    public double getGemSaveChance(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.GEM_SAVE_CHANCE);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your Revealer API/events and apply getGemSaveChance(uuid).
    }
}
