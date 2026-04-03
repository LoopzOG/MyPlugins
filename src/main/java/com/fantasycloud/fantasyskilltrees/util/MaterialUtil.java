package com.fantasycloud.fantasyskilltrees.util;

import org.bukkit.Material;

public final class MaterialUtil {
    private MaterialUtil() {
    }

    public static Material matchOrDefault(String raw, Material fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }

        Material material = Material.matchMaterial(raw.trim());
        return material != null ? material : fallback;
    }
}
