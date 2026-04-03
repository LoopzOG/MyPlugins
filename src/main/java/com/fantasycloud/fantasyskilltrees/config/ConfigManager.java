package com.fantasycloud.fantasyskilltrees.config;

import com.fantasycloud.fantasyskilltrees.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getMainMenuTitle() {
        return config.getString("gui.main-title", "&8Fantasy Skill Trees");
    }

    public String getTreeMenuTitleFormat() {
        return config.getString("gui.tree-title-format", "&8%tree% Skill Tree");
    }

    public String getMilestoneMenuTitle() {
        return config.getString("gui.milestone-title", "&8Level Milestones");
    }

    public String getMilestoneEditorTitle() {
        return config.getString("gui.milestone-editor-title", "&8Edit %milestone%");
    }

    public int getMainMenuSize() {
        return normalizeInventorySize(config.getInt("gui.main-size", 27));
    }

    public int getTreeMenuSize() {
        return normalizeInventorySize(config.getInt("gui.tree-size", 54));
    }

    public int getMilestoneMenuSize() {
        return normalizeInventorySize(config.getInt("gui.milestone-size", 54));
    }

    public int getMilestoneButtonSlot() {
        return config.getInt("gui.milestone-button-slot", 13);
    }

    public int getGuideButtonSlot() {
        return config.getInt("gui.guide-button-slot", 22);
    }

    public int getProgressInfoSlot() {
        return config.getInt("gui.progress-info-slot", 4);
    }

    public int getBackButtonSlot() {
        return config.getInt("gui.back-button-slot", 49);
    }

    public Material getFillerMaterial() {
        return MaterialUtil.matchOrDefault(config.getString("gui.filler-material", "STAINED_GLASS_PANE"), Material.STAINED_GLASS_PANE);
    }

    public short getFillerData() {
        return (short) Math.max(0, Math.min(15, config.getInt("gui.filler-data", 15)));
    }

    public String getLockedLoreLine() {
        return config.getString("gui.locked-lore-line", "&cLocked");
    }

    public String getUnlockableLoreLine() {
        return config.getString("gui.unlockable-lore-line", "&eClick to unlock");
    }

    public String getUnlockedLoreLine() {
        return config.getString("gui.unlocked-lore-line", "&aUnlocked");
    }

    public String getClaimableLoreLine() {
        return config.getString("gui.claimable-lore-line", "&eClick to claim");
    }

    public String getClaimedLoreLine() {
        return config.getString("gui.claimed-lore-line", "&aClaimed");
    }

    public String getIpLockedLoreLine() {
        return config.getString("gui.ip-locked-lore-line", "&cAlready claimed on this IP");
    }

    public String getLevelBarFilled() {
        return config.getString("gui.level-bar-filled", "&a|");
    }

    public String getLevelBarEmpty() {
        return config.getString("gui.level-bar-empty", "&7|");
    }

    public int getLevelBarLength() {
        return Math.max(5, config.getInt("gui.level-bar-length", 20));
    }

    public String getLevelXpGainMessage() {
        return config.getString("messages.xp-gain", "&a+%xp% XP &7(%reason%)");
    }

    public String getLevelUpMessage() {
        return config.getString("messages.level-up", "&6Level Up! &7You reached &fLevel %level%&7 and earned &f%points% Skill Point&7.");
    }

    public String getMilestoneClaimMessage() {
        return config.getString("messages.milestone-claim", "&aClaimed milestone reward: &f%milestone%");
    }

    public String getMilestoneLockedMessage() {
        return config.getString("messages.milestone-locked", "&cYou have not reached that milestone yet.");
    }

    public String getMilestoneAlreadyClaimedMessage() {
        return config.getString("messages.milestone-claimed", "&eYou already claimed that milestone.");
    }

    public String getMilestoneIpLockedMessage() {
        return config.getString("messages.milestone-ip-locked", "&cThat milestone has already been claimed on your IP for this map.");
    }

    public String getMilestoneNoRewardsMessage() {
        return config.getString("messages.milestone-no-rewards", "&cThis milestone has no configured rewards.");
    }

    public int getBaseXpPerLevel() {
        return Math.max(1, config.getInt("level-system.base-xp-per-level", 50));
    }

    public int getSkillPointsPerLevel() {
        return Math.max(1, config.getInt("level-system.skill-points-per-level", 1));
    }

    public String getCurrentMapId() {
        return config.getString("level-system.map-id", "default");
    }

    public double getRealmLevelXp() {
        return Math.max(0D, config.getDouble("level-system.sources.realm-levels.xp", 15D));
    }

    public int getRealmLevelStep() {
        return Math.max(1, config.getInt("level-system.sources.realm-levels.every-levels", 15));
    }

    public double getOutpostCaptureXp() {
        return Math.max(0D, config.getDouble("level-system.sources.outpost-capture.xp", 10D));
    }

    public double getKothCaptureXp() {
        return Math.max(0D, config.getDouble("level-system.sources.koth-capture.xp", 50D));
    }

    public double getMobKillBatchXp() {
        return Math.max(0D, config.getDouble("level-system.sources.mob-kills.xp", 0.5D));
    }

    public int getMobKillBatchSize() {
        return Math.max(1, config.getInt("level-system.sources.mob-kills.per-kills", 1000));
    }

    public double getCoinFlipWinXp() {
        return Math.max(0D, config.getDouble("level-system.sources.coinflip-win.xp", 1D));
    }

    public double getJackpotWinXp() {
        return Math.max(0D, config.getDouble("level-system.sources.jackpot-win.xp", 1D));
    }

    public boolean isTreeDefaultAutoSyncEnabled() {
        return config.getBoolean("tree-defaults.auto-sync-on-startup", true);
    }

    public boolean isSkillPointItemEnabled() {
        return config.getBoolean("skill-point-item.enabled", true);
    }

    public Material getSkillPointItemMaterial() {
        return MaterialUtil.matchOrDefault(config.getString("skill-point-item.material", "NETHER_STAR"), Material.NETHER_STAR);
    }

    public String getSkillPointItemDisplayName() {
        return config.getString("skill-point-item.display-name", "&bSkill Point Token");
    }

    public List<String> getSkillPointItemLore(int points) {
        List<String> output = new ArrayList<String>();
        for (String line : config.getStringList("skill-point-item.lore")) {
            output.add(line.replace("%points%", String.valueOf(points)));
        }
        return output;
    }

    public String getSkillPointRedeemMessage() {
        return config.getString("skill-point-item.redeem-message", "&aYou redeemed &f%points%&a skill point(s).");
    }

    public String getSkillPointInventoryFullMessage() {
        return config.getString("skill-point-item.inventory-full-message", "&cYour inventory is full.");
    }

    public String getSkillPointHiddenLorePrefix() {
        return config.getString("skill-point-item.hidden-lore-prefix", "FST_SKILL_POINT:");
    }

    public int getDefaultSkillPointItemValue() {
        return Math.max(1, config.getInt("skill-point-item.default-points", 1));
    }

    public boolean isMobXpPlaceholderEnabled() {
        return config.getBoolean("placeholders.mob-xp-listener-enabled", true);
    }

    public boolean isBossDamagePlaceholderEnabled() {
        return config.getBoolean("placeholders.boss-damage-listener-enabled", true);
    }

    public List<String> getBossNameContains() {
        return config.getStringList("placeholders.boss-name-contains");
    }

    public boolean isIntegrationEnabled(String key) {
        return config.getBoolean("integrations." + key + ".enabled", true);
    }

    public String getIntegrationPluginName(String key, String fallback) {
        return config.getString("integrations." + key + ".plugin-name", fallback);
    }

    private int normalizeInventorySize(int size) {
        int normalized = Math.max(9, size);
        if (normalized % 9 != 0) {
            normalized = ((normalized / 9) + 1) * 9;
        }
        return Math.min(54, normalized);
    }
}
