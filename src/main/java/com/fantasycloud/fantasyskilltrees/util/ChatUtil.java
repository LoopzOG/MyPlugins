package com.fantasycloud.fantasyskilltrees.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class ChatUtil {
    private ChatUtil() {
    }

    public static String color(String value) {
        if (value == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public static List<String> color(List<String> lines) {
        List<String> output = new ArrayList<String>();
        for (String line : lines) {
            output.add(color(line));
        }
        return output;
    }
}
