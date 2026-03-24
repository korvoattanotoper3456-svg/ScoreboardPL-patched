package de.maximilian.scoreboardpl.commands;

import de.maximilian.scoreboardpl.ScoreboardPL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private final ScoreboardPL plugin;

    public ScoreboardCommand(ScoreboardPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&8[&6ScoreboardPL&8] &r"));

        if (args.length == 0) {
            sendHelp(sender, prefix);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this!");
                    return true;
                }
                boolean visible = plugin.getScoreboardManager().toggleScoreboard(player);
                String msg = visible
                        ? plugin.getConfig().getString("messages.toggle-on", "&aScoreboard &2enabled&a!")
                        : plugin.getConfig().getString("messages.toggle-off", "&cScoreboard &4disabled&c!");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            }
            case "reload" -> {
                if (!sender.hasPermission("scoreboardpl.reload")) {
                    sender.sendMessage(prefix + ChatColor.RED + "No permission!");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getScoreboardManager().removeAll();
                for (var player : Bukkit.getOnlinePlayers()) {
                    plugin.getScoreboardManager().showScoreboard(player);
                }
                plugin.getScoreboardManager().startUpdateTask();
                sender.sendMessage(prefix + ChatColor.GREEN + "Config reloaded!");
            }
            default -> sendHelp(sender, prefix);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String prefix) {
        sender.sendMessage(prefix + ChatColor.YELLOW + "Commands:");
        sender.sendMessage(prefix + ChatColor.WHITE + "  /scoreboard toggle " + ChatColor.GRAY + "- Toggle your scoreboard on/off");
        sender.sendMessage(prefix + ChatColor.WHITE + "  /scoreboard reload " + ChatColor.GRAY + "- Reload the config (Admin)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("toggle", "reload");
        return List.of();
    }
}
