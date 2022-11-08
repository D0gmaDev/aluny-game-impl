package fr.aluny.gameimpl.scoreboard.team;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeam;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

public class ScoreboardTeamImpl implements ScoreboardTeam {

    private final Team team;

    private final Set<GamePlayer> players = new HashSet<>(); // should always be synchronized with team#getEntries

    public ScoreboardTeamImpl(Team team) {
        this.team = team;
    }

    @Override
    public String getName() {
        return this.team.getName();
    }

    @Override
    public String getPrefix() {
        return this.team.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) {
        this.team.setPrefix(prefix);
    }

    @Override
    public String getSuffix() {
        return this.team.getSuffix();
    }

    @Override
    public void setSuffix(String suffix) {
        this.team.setSuffix(suffix);
    }

    @Override
    public ChatColor getColor() {
        return this.team.getColor();
    }

    @Override
    public void setColor(ChatColor color) {
        this.team.setColor(color);
    }

    @Override
    public Set<GamePlayer> getPlayers() {
        return this.players;
    }

    @Override
    public int getSize() {
        return this.team.getSize();
    }

    @Override
    public void addPlayer(GamePlayer player) {
        this.players.add(player);
        this.team.addEntry(player.getPlayerName());

        player.getPlayer().setPlayerListName(getPrefix() + player.getPlayerName() + getSuffix());

        player.setScoreboardTeam(this);
    }

    @Override
    public boolean removePlayer(GamePlayer player) {
        this.players.remove(player);
        boolean removed = this.team.removeEntry(player.getPlayerName());

        player.getPlayer().setPlayerListName(null);

        player.setScoreboardTeam(null);
        return removed;
    }

    @Override
    public void unregister() {
        this.players.clear();
        this.team.unregister();
    }

    @Override
    public boolean hasPlayer(GamePlayer player) {
        return this.team.hasEntry(player.getPlayerName());
    }

    @Override
    public Team.OptionStatus getOption(Team.Option option) {
        return this.team.getOption(option);
    }

    @Override
    public void setOption(Team.Option option, Team.OptionStatus status) {
        this.team.setOption(option, status);
    }
}
