package com.fantasycloud.fantasyskilltrees.listener;

import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.integration.IntegrationManager;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class PlaceholderMobXpListener implements Listener {
    private final SkillTreeServiceImpl skillTreeService;
    private final IntegrationManager integrationManager;

    public PlaceholderMobXpListener(SkillTreeServiceImpl skillTreeService, IntegrationManager integrationManager) {
        this.skillTreeService = skillTreeService;
        this.integrationManager = integrationManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        skillTreeService.recordMobKills(killer.getUniqueId(), 1);

        if (integrationManager.isHooked("mobxp")) {
            return;
        }

        double boost = skillTreeService.getEffectTotal(killer.getUniqueId(), EffectType.MOB_XP_BOOST);
        if (boost <= 0D || event.getDroppedExp() <= 0) {
            return;
        }

        int extra = (int) Math.round(event.getDroppedExp() * boost);
        event.setDroppedExp(event.getDroppedExp() + Math.max(extra, 0));
    }
}
