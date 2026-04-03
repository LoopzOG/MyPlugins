package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ConquestIntegration extends AbstractPluginIntegration {
    public ConquestIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "conquest", "Conquest");
    }

    public double getLootRevealSpeedModifier(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.LOOT_REVEAL_SPEED);
    }

    public double getRoyalCacheChanceModifier(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.ROYAL_CACHE_CHANCE);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your Conquest API/events and apply getLootRevealSpeedModifier(uuid)
        // and getRoyalCacheChanceModifier(uuid).
    }
}
