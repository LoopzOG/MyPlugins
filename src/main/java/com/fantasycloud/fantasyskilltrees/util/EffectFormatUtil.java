package com.fantasycloud.fantasyskilltrees.util;

import com.fantasycloud.fantasyskilltrees.effect.EffectOperation;
import com.fantasycloud.fantasyskilltrees.effect.EffectType;
import com.fantasycloud.fantasyskilltrees.effect.SkillEffect;

public final class EffectFormatUtil {
    private EffectFormatUtil() {
    }

    public static String toLoreLine(SkillEffect effect) {
        String label = toLabel(effect.getType());
        String formattedValue = formatValue(effect.getValue(), effect.getOperation());
        return "&7- &b" + label + "&7: &f" + formattedValue;
    }

    private static String toLabel(EffectType type) {
        switch (type) {
            case LOOT_REVEAL_SPEED:
                return "Loot Reveal Speed";
            case JACKPOT_TAX_REDUCTION:
                return "Jackpot Tax Reduction";
            case CF_TAX_REDUCTION:
                return "CoinFlip Tax Reduction";
            case GEM_SAVE_CHANCE:
                return "Gem Save Chance";
            case OUTPOST_CAPTURE_SPEED:
                return "Outpost Capture Speed";
            case REALM_HIGHER_LOOT_CHANCE:
                return "Realm Loot Upgrade Chance";
            case LMS_MULTIPLIER:
                return "LMS Multiplier";
            case KEY_DROP_CHANCE:
                return "Key Drop Chance";
            case ROYAL_CACHE_CHANCE:
                return "Royal Cache Chance";
            case BOSS_DAMAGE_MULTIPLIER:
                return "Boss Damage";
            case MOB_XP_BOOST:
                return "Mob XP Boost";
            case REBIRTH_VAULT_SLOTS:
                return "Vault Slots";
            default:
                return type.name();
        }
    }

    private static String formatValue(double value, EffectOperation operation) {
        double percent = value * 100D;
        String sign = value >= 0D ? "+" : "";
        if (operation == EffectOperation.ADDITIVE && value == Math.rint(value)) {
            return sign + trim(value);
        }
        if (operation == EffectOperation.MULTIPLIER || operation == EffectOperation.ADDITIVE) {
            return sign + trim(percent) + "%";
        }
        if (operation == EffectOperation.PERCENT_REDUCTION) {
            return sign + trim(percent) + "% reduction";
        }
        if (operation == EffectOperation.PROC_CHANCE) {
            return sign + trim(percent) + "% chance";
        }
        return sign + trim(value);
    }

    private static String trim(double value) {
        return String.format(java.util.Locale.US, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
