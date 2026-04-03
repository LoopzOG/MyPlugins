package com.fantasycloud.fantasyskilltrees.service;

import com.fantasycloud.fantasyskilltrees.effect.EffectType;

import java.util.Set;
import java.util.UUID;

public interface SkillTreeService {
    boolean hasUnlocked(UUID uuid, String treeId, String nodeId);

    double getEffectTotal(UUID uuid, EffectType type);

    int getAvailablePoints(UUID uuid);

    Set<String> getUnlockedNodes(UUID uuid, String treeId);
}
