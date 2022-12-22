package fr.aluny.gameimpl.scoreboard.structure;

import fr.aluny.gameapi.scoreboard.structure.ScoreboardLine;
import fr.aluny.gameapi.scoreboard.structure.ScoreboardStructure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScoreboardStructureImpl implements ScoreboardStructure {

    private final String               title;
    private final List<ScoreboardLine> lines = new ArrayList<>();

    public ScoreboardStructureImpl(String title, Collection<ScoreboardLine> lines) {
        this.title = title;
        this.lines.addAll(lines);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public List<ScoreboardLine> getLines() {
        return this.lines;
    }
}
