package com.fantasycloud.fantasyskilltrees.effect;

import java.util.Locale;

public enum EffectOperation {
    ADDITIVE,
    MULTIPLIER,
    PERCENT_REDUCTION,
    PROC_CHANCE;

    public static EffectOperation fromString(String raw) {
        if (raw == null) {
            return null;
        }

        try {
            return EffectOperation.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
