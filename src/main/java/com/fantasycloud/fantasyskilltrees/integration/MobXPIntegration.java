package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class MobXPIntegration extends AbstractPluginIntegration {
    public MobXPIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "mobxp", "MobXP");
    }

    public double getMobXpBoost(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.MOB_XP_BOOST);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your MobXP plugin API/events and apply getMobXpBoost(uuid).
    }
}
