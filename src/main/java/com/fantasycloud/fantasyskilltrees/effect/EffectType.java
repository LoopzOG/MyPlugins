package com.fantasycloud.fantasyskilltrees.effect;

import java.util.Locale;

public enum EffectType {
    LOOT_REVEAL_SPEED,
    JACKPOT_TAX_REDUCTION,
    CF_TAX_REDUCTION,
    GEM_SAVE_CHANCE,
    OUTPOST_CAPTURE_SPEED,
    REALM_HIGHER_LOOT_CHANCE,
    LMS_MULTIPLIER,
    KEY_DROP_CHANCE,
    ROYAL_CACHE_CHANCE,
    BOSS_DAMAGE_MULTIPLIER,
    MOB_XP_BOOST,
    REBIRTH_VAULT_SLOTS;

    public static EffectType fromString(String raw) {
        if (raw == null) {
            return null;
        }

        try {
            return EffectType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
