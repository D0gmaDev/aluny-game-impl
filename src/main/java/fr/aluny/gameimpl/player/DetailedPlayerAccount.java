package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameimpl.moderation.sanction.PlayerSanction;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DetailedPlayerAccount extends PlayerAccountImpl {

    private final List<PlayerSanction> currentSanctions;

    public DetailedPlayerAccount(UUID uuid, String name, Integer currentServerId, Locale locale, Set<Rank> ranks, OffsetDateTime creationDate, List<PlayerSanction> currentSanctions) {
        super(uuid, name, currentServerId, locale, ranks, creationDate);
        this.currentSanctions = currentSanctions;
    }

    public List<PlayerSanction> getCurrentSanctions() {
        return this.currentSanctions;
    }

    @Override
    public String toString() {
        return "DetailedPlayerAccount{" +
                "uuid=" + getUuid() +
                ", name='" + getName() + '\'' +
                ", currentServerId=" + getCurrentServerId().map(Object::toString).orElse("null") +
                ", locale=" + getLocale() +
                ", ranks=" + getRanks() +
                ", creationDate=" + getCreationDate() +
                ", highestRank=" + getHighestRank() +
                ", currentSanctions=" + currentSanctions.size() +
                '}';
    }
}
