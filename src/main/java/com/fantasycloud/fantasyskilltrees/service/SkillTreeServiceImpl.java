package com.fantasycloud.fantasyskilltrees.service;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.config.TreeConfigLoader;
import com.fantasycloud.fantasyskilltrees.data.MilestoneClaimStorage;
import com.fantasycloud.fantasyskilltrees.data.MilestoneConfigStorage;
import com.fantasycloud.fantasyskilltrees.data.PlayerDataManager;
import com.fantasycloud.fantasyskilltrees.effect.EffectOperation;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.effect.SkillEffect;
import com.fantasycloud.fantasyskilltrees.event.SkillNodeUnlockEvent;
import com.fantasycloud.fantasyskilltrees.event.SkillTreeResetEvent;
import com.fantasycloud.fantasyskilltrees.gui.GuiManager;
import com.fantasycloud.fantasyskilltrees.model.MilestoneDefinition;
import com.fantasycloud.fantasyskilltrees.model.NodeRequirement;
import com.fantasycloud.fantasyskilltrees.model.PlayerSkillData;
import com.fantasycloud.fantasyskilltrees.model.RequirementType;
import com.fantasycloud.fantasyskilltrees.model.SkillNode;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SkillTreeServiceImpl implements SkillTreeService {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final TreeConfigLoader treeConfigLoader;
    private final PlayerDataManager playerDataManager;
    private final MilestoneConfigStorage milestoneConfigStorage;
    private final MilestoneClaimStorage milestoneClaimStorage;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.##");
    private Map<String, SkillTree> trees = new LinkedHashMap<String, SkillTree>();
    private Map<String, MilestoneDefinition> milestones = new LinkedHashMap<String, MilestoneDefinition>();
    private GuiManager guiManager;

    public SkillTreeServiceImpl(JavaPlugin plugin,
                                ConfigManager configManager,
                                TreeConfigLoader treeConfigLoader,
                                PlayerDataManager playerDataManager,
                                MilestoneConfigStorage milestoneConfigStorage,
                                MilestoneClaimStorage milestoneClaimStorage) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.treeConfigLoader = treeConfigLoader;
        this.playerDataManager = playerDataManager;
        this.milestoneConfigStorage = milestoneConfigStorage;
        this.milestoneClaimStorage = milestoneClaimStorage;
    }

    public void setGuiManager(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadTrees() {
        this.trees = treeConfigLoader.loadTrees();
    }

    public void reloadMilestones() {
        this.milestones = milestoneConfigStorage.load();
    }

    public Collection<SkillTree> getTrees() {
        return Collections.unmodifiableCollection(trees.values());
    }

    public Collection<MilestoneDefinition> getMilestones() {
        return Collections.unmodifiableCollection(milestones.values());
    }

    public SkillTree getTree(String treeId) {
        if (treeId == null) {
            return null;
        }
        return trees.get(treeId.toLowerCase());
    }

    public MilestoneDefinition getMilestone(String milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        return milestones.get(milestoneId.toLowerCase());
    }

    @Override
    public boolean hasUnlocked(UUID uuid, String treeId, String nodeId) {
        return playerDataManager.getOrLoad(uuid).hasUnlocked(treeId, nodeId);
    }

    public boolean hasUnlocked(Player player, String treeId, String nodeId) {
        return player != null && hasUnlocked(player.getUniqueId(), treeId, nodeId);
    }

    @Override
    public double getEffectTotal(UUID uuid, EffectType type) {
        return getTotalEffectValue(uuid, type);
    }

    @Override
    public int getAvailablePoints(UUID uuid) {
        return playerDataManager.getOrLoad(uuid).getAvailableSkillPoints();
    }

    @Override
    public Set<String> getUnlockedNodes(UUID uuid, String treeId) {
        return playerDataManager.getOrLoad(uuid).getUnlockedNodes(treeId);
    }

    public int getLevel(UUID uuid) {
        return playerDataManager.getOrLoad(uuid).getLevel();
    }

    public double getExperience(UUID uuid) {
        return playerDataManager.getOrLoad(uuid).getExperience();
    }

    public int getMobKillProgress(UUID uuid) {
        return playerDataManager.getOrLoad(uuid).getMobKillProgress();
    }

    public int getMobKillsRemainingForNextXp(UUID uuid) {
        int batchSize = configManager.getMobKillBatchSize();
        int progress = getMobKillProgress(uuid);
        return progress <= 0 ? batchSize : batchSize - progress;
    }

    public int getXpForNextLevel(UUID uuid) {
        return getXpForNextLevel(getLevel(uuid));
    }

    public int getXpForNextLevel(int level) {
        return Math.max(1, configManager.getBaseXpPerLevel() * (level + 1));
    }

    public double getExperienceProgress(UUID uuid) {
        int nextLevel = getXpForNextLevel(uuid);
        if (nextLevel <= 0) {
            return 0D;
        }
        return clamp01(getExperience(uuid) / nextLevel);
    }

    public String getProgressBar(UUID uuid) {
        int length = configManager.getLevelBarLength();
        int filled = (int) Math.round(getExperienceProgress(uuid) * length);
        StringBuilder builder = new StringBuilder(length * 4);
        for (int i = 0; i < length; i++) {
            builder.append(i < filled ? configManager.getLevelBarFilled() : configManager.getLevelBarEmpty());
        }
        return builder.toString();
    }

    public double getTotalEffectValue(UUID uuid, EffectType effectType) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        double additive = 0D;
        double multiplier = 1D;
        double reduction = 0D;
        double proc = 0D;

        for (SkillTree tree : trees.values()) {
            for (SkillNode node : tree.getNodes().values()) {
                if (!data.hasUnlocked(tree.getId(), node.getId())) {
                    continue;
                }

                for (SkillEffect effect : node.getEffects()) {
                    if (effect.getType() != effectType) {
                        continue;
                    }

                    if (effect.getOperation() == EffectOperation.ADDITIVE) {
                        additive += effect.getValue();
                    } else if (effect.getOperation() == EffectOperation.MULTIPLIER) {
                        multiplier *= (1D + effect.getValue());
                    } else if (effect.getOperation() == EffectOperation.PERCENT_REDUCTION) {
                        reduction += effect.getValue();
                    } else if (effect.getOperation() == EffectOperation.PROC_CHANCE) {
                        proc += effect.getValue();
                    }
                }
            }
        }

        if (reduction > 0D) {
            return clamp01(reduction);
        }
        if (proc > 0D) {
            return clamp01(proc);
        }
        if (additive == 0D && multiplier == 1D) {
            return 0D;
        }
        return ((1D + additive) * multiplier) - 1D;
    }

    public double getTotalEffectValue(Player player, EffectType effectType) {
        if (player == null) {
            return 0D;
        }
        return getTotalEffectValue(player.getUniqueId(), effectType);
    }

    public int getLegacyVaultSlots(UUID uuid) {
        int slots = (int) Math.round(getTotalEffectValue(uuid, EffectType.REBIRTH_VAULT_SLOTS));
        return Math.max(0, Math.min(2, slots));
    }

    public void grantSkillPoints(Player player, int amount) {
        if (player != null) {
            grantSkillPoints(player.getUniqueId(), amount);
        }
    }

    public void grantSkillPoints(UUID uuid, int amount) {
        if (amount <= 0) {
            return;
        }

        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        data.addAvailablePoints(amount);
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
    }

    public int removeSkillPoints(UUID uuid, int amount) {
        if (amount <= 0) {
            return 0;
        }

        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        int removed = Math.min(amount, data.getAvailableSkillPoints());
        if (removed <= 0) {
            return 0;
        }

        data.addAvailablePoints(-removed);
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
        return removed;
    }

    public int addLevels(UUID uuid, int amount) {
        if (amount <= 0) {
            return 0;
        }
        return setLevel(uuid, getLevel(uuid) + amount);
    }

    public int removeLevels(UUID uuid, int amount) {
        if (amount <= 0) {
            return 0;
        }
        return setLevel(uuid, Math.max(0, getLevel(uuid) - amount));
    }

    public int setLevel(UUID uuid, int newLevel) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        int normalized = Math.max(0, newLevel);
        int previous = data.getLevel();
        int delta = normalized - previous;
        if (delta == 0) {
            return previous;
        }

        data.setLevel(normalized);
        data.setExperience(0D);
        int pointDelta = delta * configManager.getSkillPointsPerLevel();
        if (pointDelta > 0) {
            data.addAvailablePoints(pointDelta);
            data.addLevelSkillPointsAwarded(pointDelta);
        } else {
            data.addAvailablePoints(-Math.min(data.getAvailableSkillPoints(), Math.abs(pointDelta)));
            data.addLevelSkillPointsAwarded(pointDelta);
        }

        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
        return previous;
    }

    public int addExperience(UUID uuid, double amount, String reason) {
        if (amount <= 0D) {
            return 0;
        }

        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        data.addExperience(amount);
        int levelsGained = 0;
        int levelPointsEarned = 0;
        while (data.getExperience() >= getXpForNextLevel(data.getLevel())) {
            data.addExperience(-getXpForNextLevel(data.getLevel()));
            data.setLevel(data.getLevel() + 1);
            data.addAvailablePoints(configManager.getSkillPointsPerLevel());
            data.addLevelSkillPointsAwarded(configManager.getSkillPointsPerLevel());
            levelsGained++;
            levelPointsEarned += configManager.getSkillPointsPerLevel();
        }

        playerDataManager.save(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(ChatUtil.color(configManager.getLevelXpGainMessage()
                    .replace("%xp%", decimalFormat.format(amount))
                    .replace("%reason%", reason == null ? "Unknown" : reason)));
            if (levelsGained > 0) {
                player.sendMessage(ChatUtil.color(configManager.getLevelUpMessage()
                        .replace("%level%", String.valueOf(data.getLevel()))
                        .replace("%points%", String.valueOf(levelPointsEarned))));
            }
        }
        refreshPlayerViews(uuid);
        return levelsGained;
    }

    public double awardRealmLevels(UUID uuid, int realmLevels) {
        if (realmLevels <= 0) {
            return 0D;
        }
        int steps = realmLevels / configManager.getRealmLevelStep();
        if (steps <= 0) {
            return 0D;
        }
        double xp = steps * configManager.getRealmLevelXp();
        addExperience(uuid, xp, "Realm Levels");
        return xp;
    }

    public double awardOutpostCaptures(UUID uuid, int captures) {
        return awardScaledExperience(uuid, captures, configManager.getOutpostCaptureXp(), "Outpost Capture");
    }

    public double awardKothCaptures(UUID uuid, int captures) {
        return awardScaledExperience(uuid, captures, configManager.getKothCaptureXp(), "KOTH Capture");
    }

    public double awardCoinFlipWins(UUID uuid, int wins) {
        return awardScaledExperience(uuid, wins, configManager.getCoinFlipWinXp(), "CoinFlip Win");
    }

    public double awardJackpotWins(UUID uuid, int wins) {
        return awardScaledExperience(uuid, wins, configManager.getJackpotWinXp(), "Jackpot Win");
    }

    public double recordMobKills(UUID uuid, int kills) {
        if (kills <= 0) {
            return 0D;
        }

        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        data.addMobKillProgress(kills);
        int batchSize = configManager.getMobKillBatchSize();
        int completedBatches = data.getMobKillProgress() / batchSize;
        if (completedBatches <= 0) {
            refreshPlayerViews(uuid);
            return 0D;
        }

        data.setMobKillProgress(data.getMobKillProgress() % batchSize);
        double xp = completedBatches * configManager.getMobKillBatchXp();
        playerDataManager.save(uuid);
        addExperience(uuid, xp, "Mob Kills");
        return xp;
    }

    public boolean hasClaimedMilestone(UUID uuid, String milestoneId) {
        return playerDataManager.getOrLoad(uuid).hasClaimedMilestone(configManager.getCurrentMapId(), milestoneId);
    }

    public boolean isMilestoneIpLocked(Player player, String milestoneId) {
        if (player == null) {
            return false;
        }
        String claimer = milestoneClaimStorage.getClaimer(configManager.getCurrentMapId(), getIpKey(player), milestoneId);
        return claimer != null && !claimer.equalsIgnoreCase(player.getUniqueId().toString());
    }

    public MilestoneClaimResult claimMilestone(Player player, String milestoneId) {
        if (player == null) {
            return MilestoneClaimResult.NOT_FOUND;
        }

        MilestoneDefinition milestone = getMilestone(milestoneId);
        if (milestone == null) {
            return MilestoneClaimResult.NOT_FOUND;
        }

        UUID uuid = player.getUniqueId();
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        String mapId = configManager.getCurrentMapId();
        if (data.hasClaimedMilestone(mapId, milestone.getId())) {
            return MilestoneClaimResult.ALREADY_CLAIMED;
        }
        if (data.getLevel() < milestone.getRequiredLevel()) {
            return MilestoneClaimResult.LOCKED;
        }
        if (milestone.getRewards().isEmpty()) {
            return MilestoneClaimResult.NO_REWARDS;
        }

        String ipKey = getIpKey(player);
        String claimer = milestoneClaimStorage.getClaimer(mapId, ipKey, milestone.getId());
        if (claimer != null && !claimer.equalsIgnoreCase(uuid.toString())) {
            return MilestoneClaimResult.IP_LOCKED;
        }

        for (ItemStack reward : milestone.getRewards()) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(reward.clone());
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        data.addClaimedMilestone(mapId, milestone.getId());
        playerDataManager.save(uuid);
        milestoneClaimStorage.setClaim(mapId, ipKey, milestone.getId(), uuid);
        player.sendMessage(ChatUtil.color(configManager.getMilestoneClaimMessage().replace("%milestone%", milestone.getDisplayName())));
        refreshPlayerViews(uuid);
        return MilestoneClaimResult.CLAIMED;
    }

    public void resetMilestones(UUID uuid, String mapId) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        data.clearClaimedMilestones(mapId);
        playerDataManager.save(uuid);
        milestoneClaimStorage.clearClaimsForPlayer(uuid, mapId);
        refreshPlayerViews(uuid);
    }

    public void autocompleteMilestones(UUID uuid, String mapId) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        String normalizedMap = mapId == null ? configManager.getCurrentMapId() : mapId;
        Player player = Bukkit.getPlayer(uuid);
        String ipKey = player != null ? getIpKey(player) : null;

        for (MilestoneDefinition milestone : milestones.values()) {
            data.addClaimedMilestone(normalizedMap, milestone.getId());
            if (ipKey != null) {
                milestoneClaimStorage.setClaim(normalizedMap, ipKey, milestone.getId(), uuid);
            }
        }

        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
    }

    public void saveMilestoneRewards(String milestoneId, List<ItemStack> rewards) {
        MilestoneDefinition milestone = getMilestone(milestoneId);
        if (milestone == null) {
            return;
        }

        MilestoneDefinition updated = new MilestoneDefinition(
                milestone.getId(),
                milestone.getRequiredLevel(),
                milestone.getSlot(),
                milestone.getIcon(),
                milestone.getIconData(),
                milestone.getDisplayName(),
                milestone.getDescription(),
                rewards
        );
        milestones.put(updated.getId(), updated);
        milestoneConfigStorage.save(milestones);
        refreshPlayerViews();
    }

    public UnlockResult unlockNode(Player player, String treeId, String nodeId) {
        if (player == null) {
            return UnlockResult.NODE_NOT_FOUND;
        }
        return unlockNode(player.getUniqueId(), treeId, nodeId, false, false);
    }

    public UnlockResult forceUnlockNode(UUID uuid, String treeId, String nodeId) {
        return unlockNode(uuid, treeId, nodeId, true, true);
    }

    public UnlockResult unlockNode(UUID uuid, String treeId, String nodeId, boolean ignoreCost, boolean ignorePrerequisites) {
        SkillTree tree = getTree(treeId);
        if (tree == null) {
            return UnlockResult.TREE_NOT_FOUND;
        }

        SkillNode node = tree.getNode(nodeId);
        if (node == null) {
            return UnlockResult.NODE_NOT_FOUND;
        }

        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        if (data.hasUnlocked(tree.getId(), node.getId())) {
            return UnlockResult.ALREADY_UNLOCKED;
        }

        if (!ignorePrerequisites && !meetsPrerequisites(uuid, node)) {
            return UnlockResult.MISSING_PREREQUISITES;
        }

        if (!ignoreCost && data.getAvailableSkillPoints() < node.getCost()) {
            return UnlockResult.INSUFFICIENT_POINTS;
        }

        Player player = Bukkit.getPlayer(uuid);
        SkillNodeUnlockEvent unlockEvent = new SkillNodeUnlockEvent(player, uuid, tree, node);
        Bukkit.getPluginManager().callEvent(unlockEvent);
        if (unlockEvent.isCancelled()) {
            return UnlockResult.CANCELLED;
        }

        data.addUnlockedNode(tree.getId(), node.getId());
        if (!ignoreCost) {
            data.addAvailablePoints(-node.getCost());
            data.addSpentPoints(node.getCost());
        }
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
        return UnlockResult.SUCCESS;
    }

    public boolean resetTree(UUID uuid, String treeId) {
        SkillTree tree = getTree(treeId);
        if (tree == null) {
            return false;
        }

        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        Set<String> unlocked = data.getUnlockedNodesByTree().get(tree.getId());
        if (unlocked == null || unlocked.isEmpty()) {
            return true;
        }

        int refund = 0;
        List<String> unlockedCopy = new ArrayList<String>(unlocked);
        for (String nodeId : unlockedCopy) {
            SkillNode node = tree.getNode(nodeId);
            if (node != null && data.removeUnlockedNode(tree.getId(), nodeId)) {
                refund += node.getCost();
            }
        }

        data.addAvailablePoints(refund);
        data.addSpentPoints(-refund);

        Player player = Bukkit.getPlayer(uuid);
        Bukkit.getPluginManager().callEvent(new SkillTreeResetEvent(player, uuid, tree.getId(), false));
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
        return true;
    }

    public boolean resetTree(Player player, String treeId) {
        if (player == null) {
            return false;
        }
        return resetTree(player.getUniqueId(), treeId);
    }

    public void resetAll(UUID uuid) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        int refund = 0;

        for (Map.Entry<String, Set<String>> entry : new LinkedHashMap<String, Set<String>>(data.getUnlockedNodesByTree()).entrySet()) {
            SkillTree tree = getTree(entry.getKey());
            if (tree == null) {
                continue;
            }

            for (String nodeId : new ArrayList<String>(entry.getValue())) {
                SkillNode node = tree.getNode(nodeId);
                if (node != null && data.removeUnlockedNode(tree.getId(), nodeId)) {
                    refund += node.getCost();
                }
            }
        }

        data.addAvailablePoints(refund);
        data.addSpentPoints(-refund);

        Player player = Bukkit.getPlayer(uuid);
        Bukkit.getPluginManager().callEvent(new SkillTreeResetEvent(player, uuid, null, true));
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
    }

    public void resetAll(Player player) {
        if (player != null) {
            resetAll(player.getUniqueId());
        }
    }

    public void resetLevelProgression(UUID uuid) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        int levelPoints = data.getLevelSkillPointsAwarded();
        if (levelPoints > 0) {
            data.addAvailablePoints(-Math.min(data.getAvailableSkillPoints(), levelPoints));
        }
        data.resetProgression();
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
    }

    public void resetEverything(UUID uuid) {
        resetAll(uuid);
        resetLevelProgression(uuid);
        resetSkillPointBank(uuid);
    }

    public void resetSkillPointBank(UUID uuid) {
        PlayerSkillData data = playerDataManager.getOrLoad(uuid);
        data.setAvailableSkillPoints(0);
        data.setSpentSkillPoints(0);
        playerDataManager.save(uuid);
        refreshPlayerViews(uuid);
    }

    public List<String> getMissingPrerequisites(UUID uuid, SkillNode node) {
        List<String> missing = new ArrayList<String>();
        for (NodeRequirement requirement : node.getPrerequisites()) {
            if (!isRequirementMet(uuid, node, requirement)) {
                missing.add(describeRequirement(requirement));
            }
        }
        return missing;
    }

    public boolean meetsPrerequisites(UUID uuid, SkillNode node) {
        for (NodeRequirement requirement : node.getPrerequisites()) {
            if (!isRequirementMet(uuid, node, requirement)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTreeFullyUnlocked(UUID uuid, String treeId) {
        SkillTree tree = getTree(treeId);
        if (tree == null) {
            return false;
        }
        Set<String> unlockedNodes = getUnlockedNodes(uuid, tree.getId());
        return unlockedNodes.size() >= tree.getNodes().size();
    }

    public boolean areAllOtherTreesFullyUnlocked(UUID uuid, String excludedTreeId) {
        for (SkillTree tree : trees.values()) {
            if (tree.getId().equalsIgnoreCase(excludedTreeId)) {
                continue;
            }
            if (!isTreeFullyUnlocked(uuid, tree.getId())) {
                return false;
            }
        }
        return true;
    }

    public void loadPlayer(UUID uuid) {
        playerDataManager.getOrLoad(uuid);
    }

    public void saveAndUnloadPlayer(UUID uuid) {
        playerDataManager.saveAndUnload(uuid);
    }

    public void saveAllPlayers() {
        playerDataManager.saveAll();
    }

    public int resetAllPlayers() {
        int resetPlayers = 0;
        for (UUID uuid : playerDataManager.getKnownPlayerUuids()) {
            resetAll(uuid);
            resetPlayers++;
        }
        return resetPlayers;
    }

    public int resetAllPlayerLevels() {
        int resetPlayers = 0;
        for (UUID uuid : playerDataManager.getKnownPlayerUuids()) {
            resetLevelProgression(uuid);
            resetPlayers++;
        }
        return resetPlayers;
    }

    public int resetAllPlayerProgression() {
        int resetPlayers = 0;
        for (UUID uuid : playerDataManager.getKnownPlayerUuids()) {
            resetEverything(uuid);
            resetPlayers++;
        }
        return resetPlayers;
    }

    private boolean isRequirementMet(UUID uuid, SkillNode node, NodeRequirement requirement) {
        if (requirement.getType() == RequirementType.ALL_OTHER_TREES_FULLY_UNLOCKED) {
            return areAllOtherTreesFullyUnlocked(uuid, findOwningTreeId(node));
        }
        if (requirement.getType() == RequirementType.TREE_FULLY_UNLOCKED) {
            return isTreeFullyUnlocked(uuid, requirement.getTreeId());
        }
        return hasUnlocked(uuid, requirement.getTreeId(), requirement.getNodeId());
    }

    private String describeRequirement(NodeRequirement requirement) {
        if (requirement.getType() == RequirementType.ALL_OTHER_TREES_FULLY_UNLOCKED) {
            return "all_other_trees_complete";
        }
        if (requirement.getType() == RequirementType.TREE_FULLY_UNLOCKED) {
            return "tree_complete:" + requirement.getTreeId();
        }
        return requirement.getTreeId() + ":" + requirement.getNodeId();
    }

    private String findOwningTreeId(SkillNode node) {
        for (SkillTree tree : trees.values()) {
            if (tree.getNode(node.getId()) == node) {
                return tree.getId();
            }
        }
        return null;
    }

    private double awardScaledExperience(UUID uuid, int amount, double xpPerUnit, String reason) {
        if (amount <= 0 || xpPerUnit <= 0D) {
            return 0D;
        }

        double xp = amount * xpPerUnit;
        addExperience(uuid, xp, reason);
        return xp;
    }

    private String getIpKey(Player player) {
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return "unknown";
        }
        return address.getAddress().getHostAddress();
    }

    private void refreshPlayerViews(UUID uuid) {
        if (guiManager == null) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            guiManager.refreshPlayerMenus(player);
        }
    }

    private void refreshPlayerViews() {
        if (guiManager != null) {
            guiManager.refreshOpenSkillTreeMenus();
            guiManager.refreshOpenMilestoneMenus();
        }
    }

    private double clamp01(double value) {
        return Math.max(0D, Math.min(1D, value));
    }
}
