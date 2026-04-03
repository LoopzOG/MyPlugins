package com.fantasycloud.fantasyskilltrees.model;

import java.util.Locale;

public enum RequirementType {
    NODE_UNLOCKED,
    TREE_FULLY_UNLOCKED,
    ALL_OTHER_TREES_FULLY_UNLOCKED;

    public static RequirementType fromString(String raw) {
        if (raw == null) {
            return null;
        }

        try {
            return RequirementType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
