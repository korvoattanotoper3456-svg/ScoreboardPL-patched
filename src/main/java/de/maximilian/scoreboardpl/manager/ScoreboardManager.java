package de.maximilian.scoreboardpl.manager;

import de.maximilian.scoreboardpl.ScoreboardPL;
import de.maximilian.scoreboardpl.hook.LuckPermsHook;
import de.maximilian.scoreboardpl.hook.PapiHook;
import de.maximilian.scoreboardpl.hook.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScoreboardManager {

    private final ScoreboardPL plugin;
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<String, Integer> usedEntries = new HashMap<>();
    private int taskId = -1;

    public ScoreboardManager(ScoreboardPL plugin) {
        this.plugin = plugin;
    }

    public void showScoreboard(Player player) {
        if (hiddenPlayers.contains(player.getUniqueId())) return;
        updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        player.setScoreboard(manager.getNewScoreboard());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.setScoreboard(online.getScoreboard());
            }
        }
    }

    public void removeAll() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
        }
        hiddenPlayers.clear();
    }

    public boolean toggleScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        if (hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
            showScoreboard(player);
            return true;
        } else {
            hiddenPlayers.add(uuid);
            removeScoreboard(player);
            return false;
        }
    }

    public boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUniqueId());
    }

    public void startUpdateTask() {
        int interval = plugin.getConfig().getInt("settings.update-interval-ticks", 20);
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hiddenPlayers.contains(player.getUniqueId())) {
                    updateScoreboard(player);
                }
            }
        }, interval, interval);
    }

    private void updateScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        String titleRaw = plugin.getConfig().getString("scoreboard.title", "&6&lServer");
        String title = color(parsePlaceholders(player, titleRaw));
        if (title.length() > 128) title = title.substring(0, 128);

        Objective obj = board.registerNewObjective("scoreboardpl", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        usedEntries.clear();

        int score = lines.size();
        for (String line : lines) {
            String parsed = color(parsePlaceholders(player, line));
            String unique = makeUnique(parsed, score);
            Score entry = obj.getScore(unique);
            entry.setScore(score);
            score--;
        }

        player.setScoreboard(board);
    }

    private String parsePlaceholders(Player player, String line) {
        // ── SERVER ──
        line = line.replace("%server_name%",
                plugin.getConfig().getString("settings.server-name", "MyServer"));
        line = line.replace("%server_online%",
                String.valueOf(Bukkit.getOnlinePlayers().size()));
        line = line.replace("%server_max_players%",
                String.valueOf(Bukkit.getMaxPlayers()));
        line = line.replace("%server_tps%",
                String.format("%.1f", getServerTPS()));
        line = line.replace("%server_version%",
                Bukkit.getVersion());

        // ── PLAYER ──
        line = line.replace("%player_name%", player.getName());
        line = line.replace("%player_displayname%", player.getDisplayName());
        line = line.replace("%player_health%",
                String.format("%.1f", player.getHealth()));
        line = line.replace("%player_max_health%",
                String.format("%.1f", player.getMaxHealth()));
        line = line.replace("%player_food%",
                String.valueOf(player.getFoodLevel()));
        line = line.replace("%player_level%",
                String.valueOf(player.getLevel()));
        line = line.replace("%player_exp%",
                String.valueOf(Math.round(player.getExp() * 100)) + "%");
        line = line.replace("%player_ping%",
                String.valueOf(player.getPing()));
        line = line.replace("%player_world%",
                player.getWorld().getName());
        line = line.replace("%player_gamemode%",
                player.getGameMode().name());
        line = line.replace("%player_x%",
                String.valueOf((int) player.getLocation().getX()));
        line = line.replace("%player_y%",
                String.valueOf((int) player.getLocation().getY()));
        line = line.replace("%player_z%",
                String.valueOf((int) player.getLocation().getZ()));
        line = line.replace("%player_deaths%",
                String.valueOf(player.getStatistic(org.bukkit.Statistic.DEATHS)));
        line = line.replace("%player_kills%",
                String.valueOf(player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS)));
        line = line.replace("%player_blocks_walked%",
                String.valueOf(player.getStatistic(org.bukkit.Statistic.WALK_ONE_CM) / 100));
        line = line.replace("%player_playtime_hours%",
                String.valueOf(player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 1200));

        // ── DATE / TIME ──
        String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
        TimeZone tz = TimeZone.getTimeZone(timezone);
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
        timeFmt.setTimeZone(tz);
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.yyyy");
        dateFmt.setTimeZone(tz);
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        dayFmt.setTimeZone(tz);
        Date now = new Date();
        line = line.replace("%time%", timeFmt.format(now));
        line = line.replace("%date%", dateFmt.format(now));
        line = line.replace("%day%", dayFmt.format(now));

        // ── VAULT ──
        if (VaultHook.isEnabled()) {
            double balance = VaultHook.getBalance(player);
            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat dfi = new DecimalFormat("#,##0");
            line = line.replace("%vault_balance%", df.format(balance));
            line = line.replace("%vault_balance_int%", dfi.format(balance));
            line = line.replace("%vault_rank%", VaultHook.getGroup(player));
        } else {
            line = line.replace("%vault_balance%", "0");
            line = line.replace("%vault_balance_int%", "0");
            line = line.replace("%vault_rank%", "default");
        }

        // ── LUCKPERMS ──
        if (LuckPermsHook.isEnabled()) {
            line = line.replace("%luckperms_group%", LuckPermsHook.getGroup(player));
            line = line.replace("%luckperms_prefix%", LuckPermsHook.getPrefix(player));
            line = line.replace("%luckperms_suffix%", LuckPermsHook.getSuffix(player));
        } else {
            line = line.replace("%luckperms_group%", "default");
            line = line.replace("%luckperms_prefix%", "");
            line = line.replace("%luckperms_suffix%", "");
        }

        // ── PLACEHOLDERAPI (все остальные плейсхолдеры — EcoJobs, CMI, и т.д.) ──
        if (PapiHook.isEnabled()) {
            line = PapiHook.setPlaceholders(player, line);
        }

        return line;
    }

    private String makeUnique(String entry, int score) {
        if (entry.length() > 40) entry = entry.substring(0, 40);
        String base = entry;
        int count = usedEntries.getOrDefault(base, 0);
        usedEntries.put(base, count + 1);
        if (count == 0) return entry;
        StringBuilder sb = new StringBuilder(entry);
        sb.append(ChatColor.RESET);
        for (int i = 0; i < count; i++) sb.append(ChatColor.RESET);
        String result = sb.toString();
        if (result.length() > 40) result = result.substring(0, 40);
        return result;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private double getServerTPS() {
        try {
            Object server = Bukkit.getServer();
            Object tpsField = server.getClass().getMethod("getServer").invoke(server);
            double[] tps = (double[]) tpsField.getClass().getField("recentTps").get(tpsField);
            return Math.min(20.0, tps[0]);
        } catch (Exception e) {
            return 20.0;
        }
    }
}
