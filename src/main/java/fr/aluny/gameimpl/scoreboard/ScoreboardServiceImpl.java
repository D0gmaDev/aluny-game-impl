package fr.aluny.gameimpl.scoreboard;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.scoreboard.ScoreboardService;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardLine;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardLineProvider;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardStructure;
import fr.aluny.gameapi.scoreboard.structure.impl.BaseScoreboardLine;
import fr.aluny.gameapi.scoreboard.structure.impl.SimpleScoreboardLine;
import fr.aluny.gameapi.scoreboard.structure.impl.SuppliedLineProvider;
import fr.aluny.gameapi.scoreboard.structure.impl.TextLineWrapper;
import fr.aluny.gameapi.scoreboard.structure.impl.TranslatedLineWrapper;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.timer.Timer;
import fr.aluny.gameimpl.scoreboard.structure.DecreasingTimerLine;
import fr.aluny.gameimpl.scoreboard.structure.IncreasingTimerLine;
import fr.aluny.gameimpl.scoreboard.structure.ScoreboardStructureImpl;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

public class ScoreboardServiceImpl implements ScoreboardService {

    private final ServiceManager serviceManager;

    public ScoreboardServiceImpl(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    private final ScoreboardLine emptyLine = new SimpleScoreboardLine(new TextLineWrapper(""));
    private final IpLine         ipLine    = new IpLine();

    @Override
    public void initialize() {
        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                ipLine.ipState = (ipLine.ipState + 1) % 38;
                ipLine.notifyScoreboards();
            }
        }, 5000L, 75L);
    }

    @Override
    public ScoreboardStructure createStructure(String title, ScoreboardLine... lines) {
        return new ScoreboardStructureImpl(title, Arrays.asList(lines));
    }

    @Override
    public void displayScoreboard(GamePlayer gamePlayer, ScoreboardStructure structure) {
        gamePlayer.getScoreboard().ifPresent(playerScoreboard -> deleteScoreboard(gamePlayer));

        ScoreboardImpl scoreboard = new ScoreboardImpl(gamePlayer.getPlayer());

        PlayerScoreboardImpl playerScoreboard = new PlayerScoreboardImpl(gamePlayer, serviceManager.getTranslationService().getDefaultLocale(), structure, scoreboard);

        gamePlayer.setScoreboard(playerScoreboard);

        scoreboard.updateTitle(structure.getTitle());

        List<ScoreboardLine> lines = structure.getLines();

        scoreboard.updateLines(lines.stream().map(line -> "").toArray(String[]::new)); // scoreboard lines creation

        lines.forEach(line -> {
            line.addScoreboard(playerScoreboard);
            line.notifyScoreboard(gamePlayer);
        });
    }

    @Override
    public void deleteScoreboard(GamePlayer gamePlayer) {
        gamePlayer.getScoreboard().ifPresent(playerScoreboard -> {
            gamePlayer.setScoreboard(null);

            List<ScoreboardLine> lines = playerScoreboard.getStructure().getLines();

            lines.forEach(line -> line.removeScoreboard(playerScoreboard));

            ((PlayerScoreboardImpl) playerScoreboard).getBoard().delete();
        });
    }

    @Override
    public ScoreboardLine getEmptyLine() {
        return this.emptyLine;
    }

    @Override
    public ScoreboardLine getIpLine() {
        return this.ipLine;
    }

    @Override
    public ScoreboardLine getLine(String key, String... args) {
        return new SimpleScoreboardLine(new TranslatedLineWrapper(key, args));
    }

    @Override
    public ScoreboardLine getIncreasingTimerLine(String key, Timer timer) {
        return new IncreasingTimerLine(key, timer);
    }

    @Override
    public ScoreboardLine getDecreasingTimerLine(String key, Timer timer, String endedKey) {
        return new DecreasingTimerLine(key, timer, endedKey);
    }

    private String getIpLine(int count) {
        return switch (count) {
            default -> "         §6mc.aluny.fr";
            case 6, 37 -> "         §em§6c.aluny.fr";
            case 7, 36 -> "         §fm§ec§6.aluny.fr";
            case 8, 35 -> "         §em§fc§e.§6aluny.fr";
            case 9, 34 -> "         §6m§ec§f.§ea§6luny.fr";
            case 10, 33 -> "         §6mc§e.§fa§el§6uny.fr";
            case 11, 32 -> "         §6mc.§ea§fl§eu§6ny.fr";
            case 12, 31 -> "         §6mc.a§el§fu§en§6y.fr";
            case 13, 30 -> "         §6mc.al§eu§fn§ey§6.fr";
            case 14, 29 -> "         §6mc.alu§en§fy§e.§6fr";
            case 15, 28 -> "         §6mc.alun§ey§f.§ef§6r";
            case 16, 27 -> "         §6mc.aluny§e.§ff§er";
            case 17, 26 -> "         §6mc.aluny.§ef§fr";
            case 18, 25 -> "         §6mc.aluny.f§er";
        };
    }

    private class IpLine extends BaseScoreboardLine {

        private int ipState = 0;

        @Override
        public ScoreboardLineProvider getLineProvider() {
            return new SuppliedLineProvider(() -> getIpLine(ipState));
        }
    }
}
