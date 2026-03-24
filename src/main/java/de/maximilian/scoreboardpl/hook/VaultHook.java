package de.maximilian.scoreboardpl.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private static Economy economy;
    private static Permission permission;
    private static boolean enabled = false;

    public static void setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> ecoProvider =
                Bukkit.getServicesManager().getRegistration(Economy.class);
        RegisteredServiceProvider<Permission> permProvider =
                Bukkit.getServicesManager().getRegistration(Permission.class);
        if (ecoProvider != null) economy = ecoProvider.getProvider();
        if (permProvider != null) permission = permProvider.getProvider();
        enabled = (economy != null);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static double getBalance(Player player) {
        if (economy == null) return 0;
        return economy.getBalance(player);
    }

    public static String getGroup(Player player) {
        if (permission == null) return "default";
        return permission.getPrimaryGroup(player);
    }
}
