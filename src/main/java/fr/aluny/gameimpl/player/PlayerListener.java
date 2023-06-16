package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.event.GamePlayerJoinEvent;
import fr.aluny.gameapi.player.event.GamePlayerQuitEvent;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.moderation.VanishServiceImpl;
import fr.aluny.gameimpl.player.rank.RankServiceImpl;
import fr.aluny.gameimpl.scoreboard.ScoreboardServiceImpl;
import fr.aluny.gameimpl.scoreboard.team.ScoreboardTeamServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
            event.disallow(Result.KICK_OTHER, Component.text("Cannot load player data", NamedTextColor.DARK_RED));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        scoreboardTeamService.onPlayerJoin(player);

        playerAccountService.getDetailedPlayerAccount(player.getUniqueId()).ifPresentOrElse(playerAccount -> {

            GamePlayer gamePlayer = gamePlayerService.onPlayerJoin(player, playerAccount);

            event.joinMessage(null);

            player.sendPlayerListHeader(Component.text(" \nALUNY\n ", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));

            vanishService.onPlayerJoin(gamePlayer, playerAccount.shouldVanish());

            rankService.onPlayerJoin(gamePlayer, playerAccount);

            plugin.getServer().getPluginManager().callEvent(new GamePlayerJoinEvent(player, gamePlayer, playerAccount));

        }, () -> player.kick(Component.text("Cannot load player data", NamedTextColor.RED)));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {

        event.quitMessage(null);

        GamePlayer gamePlayer = gamePlayerService.getPlayer(event.getPlayer());

        GamePlayerQuitEvent gamePlayerQuitEvent = new GamePlayerQuitEvent(event.getPlayer(), gamePlayer);
        plugin.getServer().getPluginManager().callEvent(gamePlayerQuitEvent);

        vanishService.onPlayerQuit(gamePlayer);

        gamePlayerService.onPlayerQuit(event.getPlayer(), gamePlayerQuitEvent.isSaved());

        scoreboardService.deleteScoreboard(gamePlayer);
    }

}
