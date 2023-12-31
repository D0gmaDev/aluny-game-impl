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
    private final boolean              allowsPrivateMessages;
    private final boolean              vanished;

    public DetailedPlayerAccount(UUID uuid, String name, Integer currentServerId, Locale locale, Set<Rank> ranks, OffsetDateTime creationDate, List<PlayerSanction> currentSanctions, boolean allowsPrivateMessages, boolean vanished) {
        super(uuid, name, currentServerId, locale, ranks, creationDate);
        this.currentSanctions = currentSanctions;
        this.allowsPrivateMessages = allowsPrivateMessages;
        this.vanished = vanished;
    }

    public List<PlayerSanction> getCurrentSanctions() {
        return this.currentSanctions;
    }

    public boolean doesAllowPrivateMessages() {
        return this.allowsPrivateMessages;
    }

    public boolean shouldVanish() {
        return this.vanished && hasPermission("fr.aluny.vanish");
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
                ", allowsPrivateMessages=" + allowsPrivateMessages +
                ", vanished=" + vanished +
                '}';
    }
}
