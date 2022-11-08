package fr.aluny.gameimpl.player;

import fr.aluny.gameimpl.scoreboard.team.ScoreboardTeamServiceImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final GamePlayerServiceImpl     gamePlayerService;
    private final ScoreboardTeamServiceImpl scoreboardTeamService;

    public PlayerListener(GamePlayerServiceImpl gamePlayerService, ScoreboardTeamServiceImpl scoreboardTeamService) {
        this.gamePlayerService = gamePlayerService;
        this.scoreboardTeamService = scoreboardTeamService;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        scoreboardTeamService.onPlayerJoin(event.getPlayer());
        gamePlayerService.onPlayerJoin(event.getPlayer());

        gamePlayerService.getPlayer(event.getPlayer()).getMessageHandler().sendMessage("game-join-message");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        gamePlayerService.onPlayerQuit(event.getPlayer(), true);
    }

}
