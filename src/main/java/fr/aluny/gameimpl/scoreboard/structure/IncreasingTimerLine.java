package fr.aluny.gameimpl.scoreboard.structure;

import fr.aluny.gameapi.scoreboard.PlayerScoreboard;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardLineProvider;
import fr.aluny.gameapi.scoreboard.structure.impl.BaseScoreboardLine;
import fr.aluny.gameapi.timer.Timer;
import reactor.core.Disposable;

public class IncreasingTimerLine extends BaseScoreboardLine {

    private final String key;
    private final Timer  timer;

    private Disposable disposable;

    public IncreasingTimerLine(String key, Timer timer) {
        this.key = key;
        this.timer = timer;
    }

    @Override
    public ScoreboardLineProvider getLineProvider() {
        return playerScoreboard -> playerScoreboard.getLocale().translate(key, timer.getIncreasingFormattedValue());
    }

    @Override
    public void addScoreboard(PlayerScoreboard playerScoreboard) {
        super.addScoreboard(playerScoreboard);

        if (disposable == null)
            disposable = timer.onTick().subscribe(timerTick -> notifyScoreboards());
    }

    @Override
    public void removeScoreboard(PlayerScoreboard playerScoreboard) {
        super.removeScoreboard(playerScoreboard);

        if (disposable != null && getScoreboards().isEmpty()) {
            disposable.dispose();
            disposable = null;
        }
    }
}
