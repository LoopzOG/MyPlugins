package com.fantasycloud.fantasyskilltrees.event;

import com.fantasycloud.fantasyskilltrees.model.SkillNode;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class SkillNodeUnlockEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final UUID playerUuid;
    private final SkillTree tree;
    private final SkillNode node;
    private boolean cancelled;

    public SkillNodeUnlockEvent(Player player, UUID playerUuid, SkillTree tree, SkillNode node) {
        this.player = player;
        this.playerUuid = playerUuid;
        this.tree = tree;
        this.node = node;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public SkillTree getTree() {
        return tree;
    }

    public SkillNode getNode() {
        return node;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
