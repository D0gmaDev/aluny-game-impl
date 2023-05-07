package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.translation.Locale;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerAccountImpl implements PlayerAccount {

    private final UUID      uuid;
    private final String    name;
    private final Integer   currentServerId;
    private final Locale    locale;
    private final Set<Rank> ranks;

    private final OffsetDateTime creationDate;

    private final Rank highestRank;

    public PlayerAccountImpl(UUID uuid, String name, Integer currentServerId, Locale locale, Set<Rank> ranks, OffsetDateTime creationDate) {
        this.uuid = uuid;
        this.name = name;
        this.currentServerId = currentServerId;
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
    public Optional<Integer> getCurrentServerId() {
        return Optional.ofNullable(this.currentServerId);
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public Set<Rank> getRanks() {
        return Collections.unmodifiableSet(this.ranks);
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
    public boolean hasPermission(String permission) {
        return this.ranks.stream().anyMatch(rank -> rank.hasPermission(permission));
    }

    @Override
    public String toString() {
        return "PlayerAccountImpl{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", currentServerId=" + currentServerId +
                ", locale=" + locale +
                ", ranks=" + ranks +
                ", creationDate=" + creationDate +
                ", highestRank=" + highestRank +
                '}';
    }
}
