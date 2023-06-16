package fr.aluny.gameimpl.scoreboard.team;

import fr.aluny.gameapi.scoreboard.team.ScoreboardTeam;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeamService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardTeamServiceImpl implements ScoreboardTeamService {

    private final Scoreboard scoreboard;

    private final Map<String, ScoreboardTeam> scoreboardTeams = new HashMap<>();

    public ScoreboardTeamServiceImpl() {
        this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    }

    @Override
    public Optional<ScoreboardTeam> getScoreboardTeam(String name) {
        return Optional.ofNullable(this.scoreboardTeams.get(name));
    }

    @Override
    public ScoreboardTeam registerScoreboardTeam(String name, Component prefix) {
        ScoreboardTeamImpl scoreboardTeam = new ScoreboardTeamImpl(this.scoreboard.registerNewTeam(name));
        scoreboardTeam.setPrefix(prefix);
        this.scoreboardTeams.put(name, scoreboardTeam);
        return scoreboardTeam;
    }

    @Override
    public void deleteScoreboardTeam(ScoreboardTeam scoreboardTeam) {
        scoreboardTeam.unregister();
        this.scoreboardTeams.remove(scoreboardTeam.getName());
    }

    public void onPlayerJoin(Player player) {
        player.setScoreboard(this.scoreboard);
    }
}
