package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.event.GamePlayerJoinEvent;
import fr.aluny.gameapi.player.event.GamePlayerQuitEvent;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.moderation.VanishServiceImpl;
import fr.aluny.gameimpl.player.rank.RankServiceImpl;
import fr.aluny.gameimpl.scoreboard.ScoreboardServiceImpl;
import fr.aluny.gameimpl.scoreboard.team.ScoreboardTeamServiceImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private final JavaPlugin                plugin;
    private final GamePlayerServiceImpl     gamePlayerService;
    private final ScoreboardTeamServiceImpl scoreboardTeamService;
    private final ScoreboardServiceImpl     scoreboardService;
    private final VanishServiceImpl         vanishService;
    private final PlayerAccountServiceImpl  playerAccountService;
    private final RankServiceImpl           rankService;

    public PlayerListener(JavaPlugin plugin, ServiceManager serviceManager) {
        this.plugin = plugin;
        this.gamePlayerService = (GamePlayerServiceImpl) serviceManager.getGamePlayerService();
        this.scoreboardTeamService = (ScoreboardTeamServiceImpl) serviceManager.getScoreboardTeamService();
        this.scoreboardService = (ScoreboardServiceImpl) serviceManager.getScoreboardService();
        this.vanishService = (VanishServiceImpl) serviceManager.getVanishService();
        this.playerAccountService = (PlayerAccountServiceImpl) serviceManager.getPlayerAccountService();
        this.rankService = (RankServiceImpl) serviceManager.getRankService();
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (playerAccountService.getPlayerAccount(event.getUniqueId()).isEmpty()) {
            event.disallow(Result.KICK_OTHER, "§4Cannot load player data");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        scoreboardTeamService.onPlayerJoin(player);

        playerAccountService.getPlayerAccount(player.getUniqueId()).ifPresentOrElse(playerAccount -> {

            GamePlayer gamePlayer = gamePlayerService.onPlayerJoin(player, playerAccount);

            event.setJoinMessage(null);

            player.setPlayerListHeader(" \n§3§lALUNY\n ");

            vanishService.onPlayerJoin(player);

            rankService.onPlayerJoin(gamePlayer, playerAccount);

            plugin.getServer().getPluginManager().callEvent(new GamePlayerJoinEvent(player, gamePlayer, playerAccount));

        }, () -> player.kickPlayer("§cCannot load player data"));
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
