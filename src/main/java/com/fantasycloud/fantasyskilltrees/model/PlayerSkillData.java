package com.fantasycloud.fantasyskilltrees.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerSkillData {
    private final UUID uuid;
    private final Map<String, Set<String>> unlockedNodesByTree;
    private final Map<String, Set<String>> claimedMilestonesByMap;
    private int availableSkillPoints;
    private int spentSkillPoints;
    private int levelSkillPointsAwarded;
    private int level;
    private double experience;
    private int mobKillProgress;

    public PlayerSkillData(UUID uuid) {
        this(uuid, new HashMap<String, Set<String>>(), new HashMap<String, Set<String>>(), 0, 0, 0, 0, 0D, 0);
    }

    public PlayerSkillData(UUID uuid,
                           Map<String, Set<String>> unlockedNodesByTree,
                           Map<String, Set<String>> claimedMilestonesByMap,
                           int availableSkillPoints,
                           int spentSkillPoints,
                           int levelSkillPointsAwarded,
                           int level,
                           double experience,
                           int mobKillProgress) {
        this.uuid = uuid;
        this.unlockedNodesByTree = unlockedNodesByTree;
        this.claimedMilestonesByMap = claimedMilestonesByMap;
        this.availableSkillPoints = availableSkillPoints;
        this.spentSkillPoints = spentSkillPoints;
        this.levelSkillPointsAwarded = Math.max(0, levelSkillPointsAwarded);
        this.level = Math.max(0, level);
        this.experience = Math.max(0D, experience);
        this.mobKillProgress = Math.max(0, mobKillProgress);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, Set<String>> getUnlockedNodesByTree() {
        return unlockedNodesByTree;
    }

    public Set<String> getUnlockedNodes(String treeId) {
        if (treeId == null) {
            return Collections.emptySet();
        }

        Set<String> nodes = unlockedNodesByTree.get(treeId.toLowerCase());
        if (nodes == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(nodes);
    }

    public boolean hasUnlocked(String treeId, String nodeId) {
        if (treeId == null || nodeId == null) {
            return false;
        }
        Set<String> nodes = unlockedNodesByTree.get(treeId.toLowerCase());
        return nodes != null && nodes.contains(nodeId.toLowerCase());
    }

    public void addUnlockedNode(String treeId, String nodeId) {
        String normalizedTree = treeId.toLowerCase();
        Set<String> nodes = unlockedNodesByTree.get(normalizedTree);
        if (nodes == null) {
            nodes = new HashSet<String>();
            unlockedNodesByTree.put(normalizedTree, nodes);
        }
        nodes.add(nodeId.toLowerCase());
    }

    public boolean removeUnlockedNode(String treeId, String nodeId) {
        Set<String> nodes = unlockedNodesByTree.get(treeId.toLowerCase());
        return nodes != null && nodes.remove(nodeId.toLowerCase());
    }

    public Map<String, Set<String>> getClaimedMilestonesByMap() {
        return claimedMilestonesByMap;
    }

    public Set<String> getClaimedMilestones(String mapId) {
        if (mapId == null) {
            return Collections.emptySet();
        }

        Set<String> milestones = claimedMilestonesByMap.get(mapId.toLowerCase());
        if (milestones == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(milestones);
    }

    public boolean hasClaimedMilestone(String mapId, String milestoneId) {
        if (mapId == null || milestoneId == null) {
            return false;
        }
        Set<String> milestones = claimedMilestonesByMap.get(mapId.toLowerCase());
        return milestones != null && milestones.contains(milestoneId.toLowerCase());
    }

    public void addClaimedMilestone(String mapId, String milestoneId) {
        String normalizedMap = mapId.toLowerCase();
        Set<String> milestones = claimedMilestonesByMap.get(normalizedMap);
        if (milestones == null) {
            milestones = new HashSet<String>();
            claimedMilestonesByMap.put(normalizedMap, milestones);
        }
        milestones.add(milestoneId.toLowerCase());
    }

    public boolean removeClaimedMilestone(String mapId, String milestoneId) {
        Set<String> milestones = claimedMilestonesByMap.get(mapId.toLowerCase());
        return milestones != null && milestones.remove(milestoneId.toLowerCase());
    }

    public void clearClaimedMilestones(String mapId) {
        if (mapId == null) {
            claimedMilestonesByMap.clear();
            return;
        }
        claimedMilestonesByMap.remove(mapId.toLowerCase());
    }

    public int getAvailableSkillPoints() {
        return availableSkillPoints;
    }

    public void setAvailableSkillPoints(int availableSkillPoints) {
        this.availableSkillPoints = Math.max(0, availableSkillPoints);
    }

    public int getSpentSkillPoints() {
        return spentSkillPoints;
    }

    public void setSpentSkillPoints(int spentSkillPoints) {
        this.spentSkillPoints = Math.max(0, spentSkillPoints);
    }

    public void addAvailablePoints(int amount) {
        setAvailableSkillPoints(this.availableSkillPoints + amount);
    }

    public void addSpentPoints(int amount) {
        setSpentSkillPoints(this.spentSkillPoints + amount);
    }

    public int getLevelSkillPointsAwarded() {
        return levelSkillPointsAwarded;
    }

    public void setLevelSkillPointsAwarded(int levelSkillPointsAwarded) {
        this.levelSkillPointsAwarded = Math.max(0, levelSkillPointsAwarded);
    }

    public void addLevelSkillPointsAwarded(int amount) {
        setLevelSkillPointsAwarded(this.levelSkillPointsAwarded + amount);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = Math.max(0D, experience);
    }

    public void addExperience(double amount) {
        setExperience(this.experience + amount);
    }

    public int getMobKillProgress() {
        return mobKillProgress;
    }

    public void setMobKillProgress(int mobKillProgress) {
        this.mobKillProgress = Math.max(0, mobKillProgress);
    }

    public void addMobKillProgress(int amount) {
        setMobKillProgress(this.mobKillProgress + amount);
    }

    public void resetProgression() {
        this.level = 0;
        this.experience = 0D;
        this.mobKillProgress = 0;
        this.levelSkillPointsAwarded = 0;
    }
}
