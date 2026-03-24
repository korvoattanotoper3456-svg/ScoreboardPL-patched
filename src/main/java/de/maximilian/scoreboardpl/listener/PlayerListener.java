package de.maximilian.scoreboardpl.listener;

import de.maximilian.scoreboardpl.ScoreboardPL;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ScoreboardPL plugin;

    public PlayerListener(ScoreboardPL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getScoreboardManager().showScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
    }
}
