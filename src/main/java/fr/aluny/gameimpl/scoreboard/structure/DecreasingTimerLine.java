package fr.aluny.gameimpl.scoreboard.structure;

import fr.aluny.gameapi.scoreboard.PlayerScoreboard;
import fr.aluny.gameapi.scoreboard.structure.impl.BaseScoreboardLine;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardLineProvider;
import fr.aluny.gameapi.timer.Timer;
import reactor.core.Disposable;

public class DecreasingTimerLine extends BaseScoreboardLine {

    private final String key;
    private final Timer  timer;
    private final String endedKey;

    private Disposable disposable;

    public DecreasingTimerLine(String key, Timer timer, String endedKey) {
        this.key = key;
        this.timer = timer;
        this.endedKey = endedKey;

        timer.addEndTask(() -> {
            if (!disposable.isDisposed())
                notifyScoreboards();
        });
    }

    @Override
    public ScoreboardLineProvider getLineProvider() {
        return playerScoreboard -> {
            if (timer.isEnded())
                return playerScoreboard.getLocale().translate(endedKey);

            return playerScoreboard.getLocale().translate(key, timer.getDecreasingFormattedValue());
        };
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
