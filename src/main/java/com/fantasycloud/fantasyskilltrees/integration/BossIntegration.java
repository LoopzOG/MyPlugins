package com.fantasycloud.fantasyskilltrees.integration;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class BossIntegration extends AbstractPluginIntegration {
    public BossIntegration(JavaPlugin plugin, ConfigManager configManager, SkillTreeService skillTreeService) {
        super(plugin, configManager, skillTreeService, "boss", "Bosses");
    }

    public double getBossDamageModifier(UUID uuid) {
        return skillTreeService.getEffectTotal(uuid, EffectType.BOSS_DAMAGE_MULTIPLIER);
    }

    @Override
    protected void onHook(Plugin targetPlugin) {
        // TODO Connect to your Boss plugin API/events and apply getBossDamageModifier(uuid).
    }
}
