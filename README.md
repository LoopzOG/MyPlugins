# FantasySkillTrees

FantasySkillTrees is a production-ready baseline Spigot 1.8.9 skill tree plugin for FantasyCloud.
It provides config-driven trees, unlockable nodes with prerequisites and costs, YAML player progression, GUI menus, command/admin controls, and an integration layer for wiring into existing FantasyCloud plugins.

## Features
- 4 baseline branches: Looting, Luck, Grinding, Rebirth
- Config-driven tree/node definitions from `plugins/FantasySkillTrees/trees/*.yml`
- Generic effect system (`EffectType`, `EffectOperation`, `SkillEffect`)
- YAML player data storage by UUID in `plugins/FantasySkillTrees/playerdata/`
- GUI menus (branch list + per-branch node menu with locked/unlockable/unlocked states)
- Admin command suite for points, skill point items, force unlock, reset, and reload
- Optional placeholder listeners (mob XP and boss damage) for baseline behavior
- Safe optional integration architecture with stubs and TODO hook points
- Custom events:
  - `SkillNodeUnlockEvent`
  - `SkillTreeResetEvent`

## Build
Requires Java 8+ and Maven.

```bash
mvn clean package
```

Output jar will be in `target/FantasySkillTrees.jar`.

## Commands
- `/skilltree open`
- `/skilltree points <player> <amount>`
- `/skilltree item <player> <points> [amount]`
- `/skilltree unlock <player> <tree> <node>`
- `/skilltree reset <player> [tree]`
- `/skilltree reload`

Aliases: `/skills`, `/stree`

## Permissions
- `fantasyskilltrees.command.open` (default: true)
- `fantasyskilltrees.command.points` (default: op)
- `fantasyskilltrees.command.item` (default: op)
- `fantasyskilltrees.command.unlock` (default: op)
- `fantasyskilltrees.command.reset` (default: op)
- `fantasyskilltrees.command.reload` (default: op)

## Skill Point Item
The plugin now includes a consumable skill point item.

- Give item: `/skilltree item <player> <points> [amount]`
- Redeem item: right-click while holding it
- Default config section: `skill-point-item` in `config.yml`

The item is identified by an internal hidden lore marker so it remains stable on Spigot 1.8.9 without relying on modern item metadata APIs.

## How Trees Are Loaded
1. Default files are copied from jar resources:
   - `trees/looting.yml`
   - `trees/luck.yml`
   - `trees/pve.yml`
2. `TreeConfigLoader` reads all `.yml` files in `plugins/FantasySkillTrees/trees/`.
3. Each file is parsed into:
   - `SkillTree`
   - `SkillNode`
   - `NodeRequirement`
   - `SkillEffect`
4. Invalid entries are skipped with warnings in console, so bad nodes do not crash startup.

## Node Format (Example)
```yml
nodes:
  reveal_speed_1:
    display-name: "&eQuick Hands I"
    description:
      - "&7Reveal loot faster in Conquest/Envoy."
    icon: FEATHER
    cost: 1
    slot: 20
    prerequisites: []
    effects:
      - type: LOOT_REVEAL_SPEED
        value: 0.10
        operation: ADDITIVE
```

## Add A New Node
1. Open the target tree file in `plugins/FantasySkillTrees/trees/`.
2. Add a new node key under `nodes:`.
3. Set `display-name`, `description`, `icon`, `cost`, `slot`, `prerequisites`, and `effects`.
4. Use either:
   - Same-tree prerequisite: `prerequisites: [existing_node_id]`
   - Cross-tree prerequisite: `prerequisites: [other_tree:other_node]`
5. Run `/skilltree reload`.

## Add A New Tree
1. Create a new file in `plugins/FantasySkillTrees/trees/`, for example `alchemy.yml`.
2. Define top-level keys:
   - `id`
   - `display-name`
   - `description`
   - `icon`
   - `menu-slot`
   - `nodes`
3. Add nodes using the same node format.
4. Run `/skilltree reload`.

## Add A New Effect Type
1. Add enum value in `EffectType`.
2. Update effect formatting label in `EffectFormatUtil`.
3. If operation handling needs special behavior, update aggregation in `SkillTreeServiceImpl#getTotalEffectValue`.
4. Expose/use the effect in your integration class or listeners.
5. Use the new effect in tree config node `effects`.

## Rebirth Tree
`Rebirth` is an endgame branch.

- It is loaded from `trees/rebirth.yml`
- `Legacy Vault I` requires every other tree to be fully unlocked
- `Legacy Vault II` requires `Legacy Vault I`
- The `REBIRTH_VAULT_SLOTS` effect can be read through `SkillTreeService#getEffectTotal(...)`
- A value of `1` means 1 saved item slot, and `2` means 2 saved item slots

## Integration Layer
Integration entry points are in `com.fantasycloud.fantasyskilltrees.integration`.

Edit these first when wiring to existing FantasyCloud plugins:
- `ConquestIntegration`
- `EnvoyIntegration`
- `JackpotIntegration`
- `CoinFlipIntegration`
- `RevealerIntegration`
- `OutpostIntegration`
- `KeyHarvesterIntegration`
- `BossIntegration`
- `MobXPIntegration`

All integrations are optional and safe:
- they only hook if enabled in `config.yml`
- they only hook if the target plugin is present and enabled
- they fail gracefully otherwise

Each integration class includes TODO comments where actual API/event hooks should be added.

## Internal Service API
`SkillTreeService` provides:
- `boolean hasUnlocked(UUID uuid, String treeId, String nodeId)`
- `double getEffectTotal(UUID uuid, EffectType type)`
- `int getAvailablePoints(UUID uuid)`
- `Set<String> getUnlockedNodes(UUID uuid, String treeId)`

Main plugin class also exposes `getSkillTreeService()` for access by other plugins.
It is also registered in Bukkit `ServicesManager` under `SkillTreeService`.

## Placeholder Baseline Behavior
Optional placeholders (controlled in `config.yml`):
- `PlaceholderMobXpListener`: applies `MOB_XP_BOOST` to mob XP drops when no MobXP integration is hooked
- `PlaceholderBossDamageListener`: applies `BOSS_DAMAGE_MULTIPLIER` against entities that look like bosses when no Boss integration is hooked

These are isolated and can be disabled at any time.

## Implementation Guide
### 1) Which files to edit first for real plugin wiring
1. `integration/ConquestIntegration.java`
2. `integration/EnvoyIntegration.java`
3. `integration/JackpotIntegration.java`
4. `integration/CoinFlipIntegration.java`
5. `integration/RevealerIntegration.java`
6. `integration/OutpostIntegration.java`
7. `integration/KeyHarvesterIntegration.java`
8. `integration/BossIntegration.java`
9. `integration/MobXPIntegration.java`

### 2) How to add more nodes to Looting, Luck, and PVE
1. Edit `plugins/FantasySkillTrees/trees/looting.yml`, `luck.yml`, or `pve.yml`.
2. Add node blocks under `nodes:`.
3. Set prerequisites and effects.
4. Run `/skilltree reload`.

### 3) How to create a new branch later
1. Add a new `trees/<branch>.yml`.
2. Set branch metadata and icon/menu slot.
3. Define nodes and effects.
4. Reload and verify it appears in main GUI.

### 4) How to test on a Spigot 1.8.9 server
1. Build jar and place it in server `plugins/`.
2. Start server and verify startup logs for tree loading and integration hook status.
3. Use `/skilltree open` and confirm GUI branch/node displays.
4. Grant points with `/skilltree points <player> <amount>`.
5. Unlock nodes and verify costs/prerequisites.
6. Test reset commands and confirm point refunds.
7. If no real integrations are attached, test placeholder mob XP and boss damage behavior.
