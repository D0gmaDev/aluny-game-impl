package fr.aluny.gameimpl.scoreboard.team;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeam;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
    public Component getPrefix() {
        return this.team.prefix();
    }

    @Override
    public void setPrefix(Component prefix) {
        this.team.prefix(prefix);
    }

    @Override
    public Component getSuffix() {
        return this.team.suffix();
    }

    @Override
    public void setSuffix(Component suffix) {
        this.team.suffix(suffix);
    }

    @Override
    public Optional<TextColor> getColor() {
        return this.team.hasColor() ? Optional.of(this.team.color()) : Optional.empty();
    }

    @Override
    public void setColor(NamedTextColor color) {
        this.team.color(color);
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
        this.team.addEntity(player.getPlayer());

        player.getPlayer().playerListName(getPrefix().append(Component.text(player.getPlayerName())).append(getSuffix()));

        player.setScoreboardTeam(this);
    }

    @Override
    public boolean removePlayer(GamePlayer player) {
        this.players.remove(player);
        boolean removed = this.team.removeEntity(player.getPlayer());

        player.getPlayer().playerListName(null);

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
