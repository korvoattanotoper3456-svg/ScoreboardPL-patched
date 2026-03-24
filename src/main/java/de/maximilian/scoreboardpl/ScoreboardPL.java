package de.maximilian.scoreboardpl;

import de.maximilian.scoreboardpl.commands.ScoreboardCommand;
import de.maximilian.scoreboardpl.hook.LuckPermsHook;
import de.maximilian.scoreboardpl.hook.PapiHook;
import de.maximilian.scoreboardpl.hook.VaultHook;
import de.maximilian.scoreboardpl.listener.PlayerListener;
import de.maximilian.scoreboardpl.manager.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardPL extends JavaPlugin {

    private static ScoreboardPL instance;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        VaultHook.setup();
        LuckPermsHook.setup();
        PapiHook.setup(this);

        scoreboardManager = new ScoreboardManager(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        ScoreboardCommand cmd = new ScoreboardCommand(this);
        getCommand("scoreboard").setExecutor(cmd);
        getCommand("scoreboard").setTabCompleter(cmd);

        for (Player player : getServer().getOnlinePlayers()) {
            scoreboardManager.showScoreboard(player);
        }
        scoreboardManager.startUpdateTask();

        getLogger().info("ScoreboardPL enabled!");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.removeAll();
        }
        getLogger().info("ScoreboardPL disabled!");
    }

    public static ScoreboardPL getInstance() {
        return instance;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
