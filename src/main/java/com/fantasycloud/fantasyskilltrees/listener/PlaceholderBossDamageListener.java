package com.fantasycloud.fantasyskilltrees.listener;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.integration.IntegrationManager;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Locale;

public class PlaceholderBossDamageListener implements Listener {
    private final ConfigManager configManager;
    private final SkillTreeServiceImpl skillTreeService;
    private final IntegrationManager integrationManager;

    public PlaceholderBossDamageListener(ConfigManager configManager,
                                         SkillTreeServiceImpl skillTreeService,
                                         IntegrationManager integrationManager) {
        this.configManager = configManager;
        this.skillTreeService = skillTreeService;
        this.integrationManager = integrationManager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (integrationManager.isHooked("boss")) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!looksLikeBoss(event.getEntity())) {
            return;
        }

        Player player = (Player) event.getDamager();
        double bonus = skillTreeService.getEffectTotal(player.getUniqueId(), EffectType.BOSS_DAMAGE_MULTIPLIER);
        if (bonus <= 0D) {
            return;
        }

        event.setDamage(event.getDamage() * (1D + bonus));
    }

    private boolean looksLikeBoss(Entity entity) {
        if (entity.hasMetadata("boss")) {
            return true;
        }
        if (entity.getCustomName() == null) {
            return false;
        }

        String name = entity.getCustomName().toLowerCase(Locale.ROOT);
        for (String keyword : configManager.getBossNameContains()) {
            if (name.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
