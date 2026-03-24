package de.maximilian.scoreboardpl.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PapiHook {

    private static boolean enabled = false;

    public static void setup(Plugin plugin) {
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            enabled = true;
            plugin.getLogger().info("[ScoreboardPL] PlaceholderAPI found! External placeholders enabled.");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String setPlaceholders(Player player, String text) {
        if (!enabled) return text;
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }
}
