package fr.aluny.gameimpl.scoreboard;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.scoreboard.PlayerScoreboard;
import fr.aluny.gameapi.scoreboard.Scoreboard;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardLine;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardStructure;
import fr.aluny.gameapi.translation.Locale;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerScoreboardImpl implements PlayerScoreboard {

    private final GamePlayer          gamePlayer;
    private final Locale              locale;
    private final ScoreboardStructure structure;
    private final Scoreboard          scoreboard;

    private final Map<ScoreboardLine, List<Integer>> linesMap;

    public PlayerScoreboardImpl(GamePlayer gamePlayer, Locale locale, ScoreboardStructure structure, Scoreboard scoreboard) {
        this.gamePlayer = gamePlayer;
        this.locale = locale;
        this.structure = structure;
        this.scoreboard = scoreboard;

        AtomicInteger index = new AtomicInteger();
        linesMap = structure.getLines().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.mapping(i -> index.getAndIncrement(), Collectors.toList())));
    }

    @Override
    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public ScoreboardStructure getStructure() {
        return this.structure;
    }

    @Override
    public void updateLine(ScoreboardLine scoreboardLine) {
        for (Integer line : this.linesMap.get(scoreboardLine)) {
            this.scoreboard.updateLine(line, scoreboardLine.getLineProvider().getForPlayer(this));
        }
    }

    public Scoreboard getBoard() {
        return this.scoreboard;
    }
}
