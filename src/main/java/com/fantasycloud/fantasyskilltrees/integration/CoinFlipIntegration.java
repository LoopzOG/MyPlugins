package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class CoinFlipIntegration extends AbstractPluginIntegration {
    public CoinFlipIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "coinflip", "CoinFlip");
    }

    public double getTaxReduction(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.CF_TAX_REDUCTION);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your CoinFlip API/events and apply getTaxReduction(uuid).
    }
}
