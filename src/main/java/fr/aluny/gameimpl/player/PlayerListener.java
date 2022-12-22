package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.event.GamePlayerJoinEvent;
import fr.aluny.gameapi.player.event.GamePlayerQuitEvent;
import fr.aluny.gameimpl.scoreboard.ScoreboardServiceImpl;
import fr.aluny.gameimpl.scoreboard.team.ScoreboardTeamServiceImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private final JavaPlugin                plugin;
    private final GamePlayerServiceImpl     gamePlayerService;
    private final ScoreboardTeamServiceImpl scoreboardTeamService;
    private final ScoreboardServiceImpl     scoreboardService;

    public PlayerListener(JavaPlugin plugin, GamePlayerServiceImpl gamePlayerService, ScoreboardTeamServiceImpl scoreboardTeamService, ScoreboardServiceImpl scoreboardService) {
        this.plugin = plugin;
        this.gamePlayerService = gamePlayerService;
        this.scoreboardTeamService = scoreboardTeamService;
        this.scoreboardService = scoreboardService;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        scoreboardTeamService.onPlayerJoin(event.getPlayer());
        gamePlayerService.onPlayerJoin(event.getPlayer());

        event.setJoinMessage(null);

        plugin.getServer().getPluginManager().callEvent(new GamePlayerJoinEvent(event.getPlayer(), gamePlayerService.getPlayer(event.getPlayer())));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        GamePlayer gamePlayer = gamePlayerService.getPlayer(event.getPlayer());

        GamePlayerQuitEvent gamePlayerQuitEvent = new GamePlayerQuitEvent(event.getPlayer(), gamePlayer);
        plugin.getServer().getPluginManager().callEvent(gamePlayerQuitEvent);

        gamePlayerService.onPlayerQuit(event.getPlayer(), gamePlayerQuitEvent.isSaved());

        scoreboardService.deleteScoreboard(gamePlayer);
    }

}
