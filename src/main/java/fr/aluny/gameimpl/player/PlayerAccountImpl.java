package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.translation.Locale;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

public class PlayerAccountImpl implements PlayerAccount {

    private final UUID      uuid;
    private final String    name;
    private final Locale    locale;
    private final Set<Rank> ranks;

    private final OffsetDateTime creationDate;

    private final Rank highestRank;

    public PlayerAccountImpl(UUID uuid, String name, Locale locale, Set<Rank> ranks, OffsetDateTime creationDate) {
        this.uuid = uuid;
        this.name = name;
        this.locale = locale;
        this.ranks = ranks;
        this.creationDate = creationDate;

        this.highestRank = ranks.stream().max(Comparator.comparingInt(Rank::getImportanceIndex)).orElseThrow();
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public Set<Rank> getRanks() {
        return this.ranks;
    }

    @Override
    public OffsetDateTime getCreationDate() {
        return this.creationDate;
    }

    @Override
    public Rank getHighestRank() {
        return this.highestRank;
    }

    @Override
    public String toString() {
        return "PlayerAccountImpl{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", locale=" + locale +
                ", ranks=" + ranks +
                ", creationDate=" + creationDate +
                ", highestRank=" + highestRank +
                '}';
    }
}
