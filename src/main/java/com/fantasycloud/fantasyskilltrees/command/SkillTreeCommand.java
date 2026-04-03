package com.fantasycloud.fantasyskilltrees.command;

import com.fantasycloud.fantasyskilltrees.config.ConfigManager;
import com.fantasycloud.fantasyskilltrees.config.TreeConfigLoader;
import com.fantasycloud.fantasyskilltrees.gui.GuiManager;
import com.fantasycloud.fantasyskilltrees.integration.IntegrationManager;
import com.fantasycloud.fantasyskilltrees.item.SkillPointItemFactory;
import com.fantasycloud.fantasyskilltrees.model.SkillTree;
import com.fantasycloud.fantasyskilltrees.service.SkillTreeServiceImpl;
import com.fantasycloud.fantasyskilltrees.service.UnlockResult;
import com.fantasycloud.fantasyskilltrees.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SkillTreeCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;
    private final SkillTreeServiceImpl skillTreeService;
    private final GuiManager guiManager;
    private final IntegrationManager integrationManager;
    private final SkillPointItemFactory skillPointItemFactory;
    private final TreeConfigLoader treeConfigLoader;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

    public SkillTreeCommand(ConfigManager configManager,
                            SkillTreeServiceImpl skillTreeService,
                            GuiManager guiManager,
                            IntegrationManager integrationManager,
                            SkillPointItemFactory skillPointItemFactory,
                            TreeConfigLoader treeConfigLoader) {
        this.configManager = configManager;
        this.skillTreeService = skillTreeService;
        this.guiManager = guiManager;
        this.integrationManager = integrationManager;
        this.skillPointItemFactory = skillPointItemFactory;
        this.treeConfigLoader = treeConfigLoader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("openallvaults")
                || command.getName().equalsIgnoreCase("unlockallvaults")) {
            return handleUnlockAllVaults(sender);
        }
        if (command.getName().equalsIgnoreCase("milestones")) {
            return handleStandaloneMilestones(sender, args);
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            return handleOpen(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("milestones")) {
            return handleMilestones(sender, args);
        }
        if (sub.equals("points")) {
            return handlePoints(sender, args);
        }
        if (sub.equals("removepoints")) {
            return handleRemovePoints(sender, args);
        }
        if (sub.equals("levels")) {
            return handleLevels(sender, args);
        }
        if (sub.equals("xp")) {
            return handleXp(sender, args);
        }
        if (sub.equals("sourcexp")) {
            return handleSourceXp(sender, args);
        }
        if (sub.equals("resetmilestones")) {
            return handleResetMilestones(sender, args);
        }
        if (sub.equals("autocompletemilestones")) {
            return handleAutocompleteMilestones(sender, args);
        }
        if (sub.equals("item")) {
            return handleItem(sender, args);
        }
        if (sub.equals("unlockvault")) {
            return handleUnlockVault(sender, args);
        }
        if (sub.equals("unlockallvaults") || sub.equals("openallvaults")) {
            return handleUnlockAllVaults(sender);
        }
        if (sub.equals("unlock")) {
            return handleUnlock(sender, args);
        }
        if (sub.equals("reset")) {
            return handleReset(sender, args);
        }
        if (sub.equals("reload")) {
            return handleReload(sender);
        }
        if (sub.equals("syncdefaults")) {
            return handleSyncDefaults(sender);
        }

        sendHelp(sender);
        return true;
    }

    private boolean handleStandaloneMilestones(CommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("edit")) {
            if (!sender.hasPermission("fantasyskilltrees.command.milestones.edit")) {
                sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatUtil.color("&cOnly players can edit milestone rewards in-game."));
                return true;
            }
            guiManager.openMilestoneAdminMenu((Player) sender);
            return true;
        }

        if (!sender.hasPermission("fantasyskilltrees.command.milestones")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can open the milestone menu."));
            return true;
        }
        guiManager.openMilestoneMenu((Player) sender);
        return true;
    }

    private boolean handleOpen(CommandSender sender) {
        if (!sender.hasPermission("fantasyskilltrees.command.open")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can open the skill tree menu."));
            return true;
        }
        guiManager.openMainMenu((Player) sender);
        return true;
    }

    private boolean handleMilestones(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("edit")) {
            if (!sender.hasPermission("fantasyskilltrees.command.milestones.edit")) {
                sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatUtil.color("&cOnly players can edit milestone rewards in-game."));
                return true;
            }
            guiManager.openMilestoneAdminMenu((Player) sender);
            return true;
        }

        if (!sender.hasPermission("fantasyskilltrees.command.milestones")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can open the milestone menu."));
            return true;
        }
        guiManager.openMilestoneMenu((Player) sender);
        return true;
    }

    private boolean handlePoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.points")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree points <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.color("&cAmount must be a number."));
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatUtil.color("&cAmount must be greater than 0."));
            return true;
        }

        skillTreeService.grantSkillPoints(target.getUniqueId(), amount);
        sender.sendMessage(ChatUtil.color("&aGranted &f" + amount + "&a points to &f" + displayName(target) + "&a."));
        return true;
    }

    private boolean handleRemovePoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.points")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree removepoints <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.color("&cAmount must be a number."));
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatUtil.color("&cAmount must be greater than 0."));
            return true;
        }

        int removed = skillTreeService.removeSkillPoints(target.getUniqueId(), amount);
        if (removed <= 0) {
            sender.sendMessage(ChatUtil.color("&e" + displayName(target) + " has no available skill points to remove."));
            return true;
        }

        sender.sendMessage(ChatUtil.color("&aRemoved &f" + removed + "&a points from &f" + displayName(target) + "&a."));
        return true;
    }

    private boolean handleLevels(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.levels")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree levels <add|remove|set> <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.color("&cAmount must be a number."));
            return true;
        }
        if (amount < 0) {
            sender.sendMessage(ChatUtil.color("&cAmount must be 0 or greater."));
            return true;
        }

        int previous;
        if (args[1].equalsIgnoreCase("add")) {
            previous = skillTreeService.addLevels(target.getUniqueId(), amount);
        } else if (args[1].equalsIgnoreCase("remove")) {
            previous = skillTreeService.removeLevels(target.getUniqueId(), amount);
        } else if (args[1].equalsIgnoreCase("set")) {
            previous = skillTreeService.setLevel(target.getUniqueId(), amount);
        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree levels <add|remove|set> <player> <amount>"));
            return true;
        }

        sender.sendMessage(ChatUtil.color("&aUpdated &f" + displayName(target) + "&a from level &f" + previous + "&a to &f" + skillTreeService.getLevel(target.getUniqueId()) + "&a."));
        return true;
    }

    private boolean handleXp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.xp")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("add")) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree xp add <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.color("&cAmount must be a number."));
            return true;
        }
        if (amount <= 0D) {
            sender.sendMessage(ChatUtil.color("&cAmount must be greater than 0."));
            return true;
        }

        skillTreeService.addExperience(target.getUniqueId(), amount, "Admin Grant");
        sender.sendMessage(ChatUtil.color("&aGranted &f" + decimalFormat.format(amount) + "&a XP to &f" + displayName(target) + "&a."));
        return true;
    }

    private boolean handleSourceXp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.xp")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree sourcexp <player> <realm|outpost|koth|mobs|cf|jackpot> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String source = args[2].toLowerCase(Locale.ROOT);
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.color("&cAmount must be a whole number."));
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatUtil.color("&cAmount must be greater than 0."));
            return true;
        }

        double awarded;
        if (source.equals("realm")) {
            awarded = skillTreeService.awardRealmLevels(target.getUniqueId(), amount);
        } else if (source.equals("outpost")) {
            awarded = skillTreeService.awardOutpostCaptures(target.getUniqueId(), amount);
        } else if (source.equals("koth")) {
            awarded = skillTreeService.awardKothCaptures(target.getUniqueId(), amount);
        } else if (source.equals("mobs")) {
            awarded = skillTreeService.recordMobKills(target.getUniqueId(), amount);
        } else if (source.equals("cf")) {
            awarded = skillTreeService.awardCoinFlipWins(target.getUniqueId(), amount);
        } else if (source.equals("jackpot")) {
            awarded = skillTreeService.awardJackpotWins(target.getUniqueId(), amount);
        } else {
            sender.sendMessage(ChatUtil.color("&cUnknown source. Use realm, outpost, koth, mobs, cf, or jackpot."));
            return true;
        }

        sender.sendMessage(ChatUtil.color("&aApplied &f" + source + "&a progression to &f" + displayName(target) + "&a for &f" + amount + "&a unit(s), awarding &f" + decimalFormat.format(awarded) + "&a XP."));
        return true;
    }

    private boolean handleResetMilestones(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.milestones.admin")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree resetmilestones <player> [map]"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String mapId = args.length >= 3 ? args[2] : configManager.getCurrentMapId();
        skillTreeService.resetMilestones(target.getUniqueId(), mapId);
        sender.sendMessage(ChatUtil.color("&aReset milestone claims for &f" + displayName(target) + "&a on map &f" + mapId + "&a."));
        return true;
    }

    private boolean handleAutocompleteMilestones(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.milestones.admin")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree autocompletemilestones <player> [map]"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String mapId = args.length >= 3 ? args[2] : configManager.getCurrentMapId();
        skillTreeService.autocompleteMilestones(target.getUniqueId(), mapId);
        sender.sendMessage(ChatUtil.color("&aMarked all milestones as claimed for &f" + displayName(target) + "&a on map &f" + mapId + "&a."));
        return true;
    }

    private boolean handleUnlock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.unlock")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree unlock <player> <tree> <node>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UnlockResult result = skillTreeService.forceUnlockNode(target.getUniqueId(), args[2], args[3]);
        if (result == UnlockResult.SUCCESS) {
            sender.sendMessage(ChatUtil.color("&aForce-unlocked node for &f" + displayName(target) + "&a."));
            return true;
        }
        sender.sendMessage(ChatUtil.color("&cCould not unlock node: &f" + result.name()));
        return true;
    }

    private boolean handleUnlockVault(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.unlock")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree unlockvault <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!guiManager.unlockLegacyVault(target.getUniqueId())) {
            sender.sendMessage(ChatUtil.color("&eNo locked legacy vault items were found for &f" + displayName(target) + "&e."));
            return true;
        }

        sender.sendMessage(ChatUtil.color("&aUnlocked legacy vault items for &f" + displayName(target) + "&a."));
        return true;
    }

    private boolean handleUnlockAllVaults(CommandSender sender) {
        if (!sender.hasPermission("fantasyskilltrees.command.unlock")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }

        int unlockedVaults = guiManager.unlockAllLegacyVaults();
        int preparedClaims = guiManager.queueLegacyLoginClaims();
        int resetPlayers = skillTreeService.resetAllPlayerProgression();
        guiManager.refreshOpenSkillTreeMenus();
        guiManager.refreshOpenLegacyVaults();
        guiManager.refreshOpenMilestoneMenus();
        for (Player player : Bukkit.getOnlinePlayers()) {
            guiManager.enforcePendingLegacyClaim(player);
        }

        if (unlockedVaults <= 0 && preparedClaims <= 0 && resetPlayers <= 0) {
            sender.sendMessage(ChatUtil.color("&eNo locked legacy vaults or saved player progression were found."));
            return true;
        }

        sender.sendMessage(ChatUtil.color("&aUnlocked legacy vault items for &f" + unlockedVaults + "&a player vault(s), reset skills and levels for &f" + resetPlayers + "&a player(s), and queued one-time login claim popups for &f" + preparedClaims + "&a player(s)."));
        return true;
    }

    private boolean handleItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.item")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (!configManager.isSkillPointItemEnabled()) {
            sender.sendMessage(ChatUtil.color("&cSkill point items are disabled in config."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree item <player> <points> [amount]"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatUtil.color("&cThat player must be online to receive the item."));
            return true;
        }

        int points;
        try {
            points = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.color("&cPoints must be a number."));
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatUtil.color("&cAmount must be a number."));
                return true;
            }
        }

        if (points <= 0 || amount <= 0) {
            sender.sendMessage(ChatUtil.color("&cPoints and amount must be greater than 0."));
            return true;
        }

        ItemStack item = skillPointItemFactory.createItem(points, amount);
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(item);
        int inserted = amount;
        for (ItemStack leftover : leftovers.values()) {
            inserted -= leftover.getAmount();
        }

        if (inserted <= 0) {
            sender.sendMessage(ChatUtil.color("&c" + target.getName() + "'s inventory is full."));
            target.sendMessage(ChatUtil.color(configManager.getSkillPointInventoryFullMessage()));
            return true;
        }

        sender.sendMessage(ChatUtil.color("&aGave &f" + inserted + "&a skill point item(s) worth &f" + points + "&a point(s) to &f" + target.getName() + "&a."));
        target.sendMessage(ChatUtil.color("&aYou received &f" + inserted + "&a skill point item(s) worth &f" + points + "&a point(s) each."));
        if (!leftovers.isEmpty()) {
            sender.sendMessage(ChatUtil.color("&e" + leftovers.size() + " stack(s) could not be added because inventory space ran out."));
            target.sendMessage(ChatUtil.color(configManager.getSkillPointInventoryFullMessage()));
        }
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fantasyskilltrees.command.reset")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.color("&cUsage: /skilltree reset <player> [tree]"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (args.length == 2) {
            skillTreeService.resetAll(target.getUniqueId());
            sender.sendMessage(ChatUtil.color("&aReset all trees for &f" + displayName(target) + "&a."));
            return true;
        }

        String treeId = args[2];
        if (!skillTreeService.resetTree(target.getUniqueId(), treeId)) {
            sender.sendMessage(ChatUtil.color("&cUnknown tree: &f" + treeId));
            return true;
        }
        sender.sendMessage(ChatUtil.color("&aReset tree &f" + treeId + "&a for &f" + displayName(target) + "&a."));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("fantasyskilltrees.command.reload")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }

        configManager.reload();
        treeConfigLoader.syncBundledTreeDefaults(configManager.isTreeDefaultAutoSyncEnabled());
        skillTreeService.reloadTrees();
        skillTreeService.reloadMilestones();
        integrationManager.shutdown();
        integrationManager.initialize();
        guiManager.refreshOpenSkillTreeMenus();
        guiManager.refreshOpenMilestoneMenus();
        sender.sendMessage(ChatUtil.color("&aFantasySkillTrees config reloaded."));
        return true;
    }

    private boolean handleSyncDefaults(CommandSender sender) {
        if (!sender.hasPermission("fantasyskilltrees.command.syncdefaults")) {
            sender.sendMessage(ChatUtil.color("&cYou do not have permission."));
            return true;
        }

        treeConfigLoader.syncBundledTreeDefaults(true);
        skillTreeService.reloadTrees();
        sender.sendMessage(ChatUtil.color("&aBundled tree defaults synced from the jar and reloaded."));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtil.color("&e/skilltree open"));
        sender.sendMessage(ChatUtil.color("&e/skilltree milestones"));
        sender.sendMessage(ChatUtil.color("&e/skilltree milestones edit"));
        sender.sendMessage(ChatUtil.color("&e/skilltree points <player> <amount>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree removepoints <player> <amount>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree levels <add|remove|set> <player> <amount>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree xp add <player> <amount>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree sourcexp <player> <realm|outpost|koth|mobs|cf|jackpot> <amount>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree resetmilestones <player> [map]"));
        sender.sendMessage(ChatUtil.color("&e/skilltree autocompletemilestones <player> [map]"));
        sender.sendMessage(ChatUtil.color("&e/skilltree item <player> <points> [amount]"));
        sender.sendMessage(ChatUtil.color("&e/skilltree unlockvault <player>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree unlockallvaults"));
        sender.sendMessage(ChatUtil.color("&e/skilltree openallvaults"));
        sender.sendMessage(ChatUtil.color("&e/skilltree unlock <player> <tree> <node>"));
        sender.sendMessage(ChatUtil.color("&e/skilltree reset <player> [tree]"));
        sender.sendMessage(ChatUtil.color("&e/skilltree reload"));
        sender.sendMessage(ChatUtil.color("&e/skilltree syncdefaults"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("openallvaults")
                || command.getName().equalsIgnoreCase("unlockallvaults")) {
            return Collections.emptyList();
        }
        if (command.getName().equalsIgnoreCase("milestones")) {
            if (args.length == 1) {
                return partial(args[0], Collections.singletonList("edit"));
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partial(args[0], Arrays.asList(
                    "open", "milestones", "points", "removepoints", "levels", "xp", "sourcexp",
                    "resetmilestones", "autocompletemilestones", "item", "unlockvault",
                    "unlockallvaults", "openallvaults", "unlock", "reset", "reload", "syncdefaults"
            ));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("milestones")) {
            return partial(args[1], Collections.singletonList("edit"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("levels")) {
            return partial(args[1], Arrays.asList("add", "remove", "set"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("xp")) {
            return partial(args[1], Collections.singletonList("add"));
        }

        if (args.length == 2 && isOneOf(args[0], "points", "removepoints", "item", "unlockvault", "unlock", "reset", "resetmilestones", "autocompletemilestones", "sourcexp")) {
            return partial(args[1], onlinePlayerNames());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("levels")) {
            return partial(args[2], onlinePlayerNames());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("xp")) {
            return partial(args[2], onlinePlayerNames());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("sourcexp")) {
            return partial(args[2], Arrays.asList("realm", "outpost", "koth", "mobs", "cf", "jackpot"));
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("reset"))) {
            List<String> treeIds = new ArrayList<String>();
            for (SkillTree tree : skillTreeService.getTrees()) {
                treeIds.add(tree.getId());
            }
            return partial(args[2], treeIds);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("unlock")) {
            SkillTree tree = skillTreeService.getTree(args[2]);
            if (tree == null) {
                return Collections.emptyList();
            }
            return partial(args[3], new ArrayList<String>(tree.getNodes().keySet()));
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("levels")) {
            return partial(args[3], Arrays.asList("1", "5", "10", "25", "50"));
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("xp")) {
            return partial(args[3], Arrays.asList("1", "10", "25", "50", "100"));
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("sourcexp")) {
            return partial(args[3], Arrays.asList("1", "5", "10", "15", "1000"));
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("item")) {
            return partial(args[2], Arrays.asList(String.valueOf(configManager.getDefaultSkillPointItemValue()), "5", "10"));
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("item")) {
            return partial(args[3], Arrays.asList("1", "5", "10", "16", "32", "64"));
        }

        return Collections.emptyList();
    }

    private boolean isOneOf(String input, String... values) {
        for (String value : values) {
            if (value.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    private List<String> partial(String token, List<String> values) {
        if (token == null) {
            return values;
        }

        String lower = token.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<String>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(value);
            }
        }
        return matches;
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private String displayName(OfflinePlayer player) {
        return player.getName() != null ? player.getName() : player.getUniqueId().toString();
    }
}
