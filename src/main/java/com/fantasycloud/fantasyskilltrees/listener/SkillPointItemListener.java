package com.fantasycloud.fantasyskilltrees.listener;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.item.SkillPointItemFactory;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SkillPointItemListener implements Listener {
    private final ConfigManager configManager;
    private final SkillPointItemFactory skillPointItemFactory;
    private final SkillTreeServiceImpl skillTreeService;

    public SkillPointItemListener(ConfigManager configManager,
                                  SkillPointItemFactory skillPointItemFactory,
                                  SkillTreeServiceImpl skillTreeService) {
        this.configManager = configManager;
        this.skillPointItemFactory = skillPointItemFactory;
        this.skillTreeService = skillTreeService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!configManager.isSkillPointItemEnabled()) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        int points = skillPointItemFactory.getSkillPoints(item);
        if (points <= 0) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        skillTreeService.grantSkillPoints(player, points);
        consumeOne(player);

        String message = configManager.getSkillPointRedeemMessage().replace("%points%", String.valueOf(points));
        player.sendMessage(ChatUtil.color(message));
    }

    private void consumeOne(Player player) {
        ItemStack inHand = player.getItemInHand();
        if (inHand == null) {
            return;
        }

        if (inHand.getAmount() <= 1) {
            player.setItemInHand(new ItemStack(Material.AIR));
            return;
        }

        inHand.setAmount(inHand.getAmount() - 1);
        player.setItemInHand(inHand);
    }
}
