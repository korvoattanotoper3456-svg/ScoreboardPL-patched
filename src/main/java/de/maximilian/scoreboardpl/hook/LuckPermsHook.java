package de.maximilian.scoreboardpl.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsHook {

    private static LuckPerms luckPerms;
    private static boolean enabled = false;

    public static void setup() {
        try {
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) return;
            luckPerms = LuckPermsProvider.get();
            enabled = true;
        } catch (Exception e) {
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getGroup(Player player) {
        if (!enabled) return "default";
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "default";
            return user.getPrimaryGroup();
        } catch (Exception e) {
            return "default";
        }
    }

    public static String getPrefix(Player player) {
        if (!enabled) return "";
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            CachedMetaData meta = user.getCachedData().getMetaData();
            String prefix = meta.getPrefix();
            return prefix != null ? prefix : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String getSuffix(Player player) {
        if (!enabled) return "";
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            CachedMetaData meta = user.getCachedData().getMetaData();
            String suffix = meta.getSuffix();
            return suffix != null ? suffix : "";
        } catch (Exception e) {
            return "";
        }
    }
}
