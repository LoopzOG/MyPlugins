package com.fantasycloud.fantasyskilltrees.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class SkillTreeResetEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final UUID playerUuid;
    private final String treeId;
    private final boolean allTrees;

    public SkillTreeResetEvent(Player player, UUID playerUuid, String treeId, boolean allTrees) {
        this.player = player;
        this.playerUuid = playerUuid;
        this.treeId = treeId;
        this.allTrees = allTrees;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getTreeId() {
        return treeId;
    }

    public boolean isAllTrees() {
        return allTrees;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
