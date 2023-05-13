package fr.aluny.gameimpl.scoreboard;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.scoreboard.Scoreboard;
import fr.aluny.gameapi.scoreboard.ScoreboardService;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardServiceImpl implements ScoreboardService {

    private final JavaPlugin     plugin;
    private final ServiceManager serviceManager;

    public ScoreboardServiceImpl(JavaPlugin plugin, ServiceManager serviceManager) {
        this.plugin = plugin;
        this.serviceManager = serviceManager;
    }

    @Override
    public Scoreboard createScoreboard(Locale locale, Component title) {
        return new ScoreboardImpl(this.plugin, this.serviceManager.getGamePlayerService(), locale, title);
    }

    @Override
    public Scoreboard displayScoreboard(GamePlayer gamePlayer, Locale locale, Component title, Consumer<Scoreboard> scoreboardBuilder) {
        gamePlayer.getScoreboard().ifPresent(scoreboard -> deleteScoreboard(gamePlayer));

        Scoreboard scoreboard = createScoreboard(locale, title);
        scoreboardBuilder.accept(scoreboard);

        scoreboard.addViewer(gamePlayer);
        return scoreboard;
    }

    @Override
    public Scoreboard displayScoreboard(GamePlayer gamePlayer, Locale locale, Component title, long updatePeriod, Consumer<Scoreboard> scoreboardBuilder) {
        Scoreboard scoreboard = displayScoreboard(gamePlayer, locale, title, scoreboardBuilder);
        scoreboard.updateLinesPeriodically(0, updatePeriod);
        return scoreboard;
    }

    @Override
    public void deleteScoreboard(GamePlayer gamePlayer) {
        gamePlayer.getScoreboard().ifPresent(playerScoreboard -> {
            gamePlayer.setScoreboard(null);
            playerScoreboard.destroy();
        });
    }
}
