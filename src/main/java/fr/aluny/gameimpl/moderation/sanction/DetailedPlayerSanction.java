package fr.aluny.gameimpl.moderation.sanction;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DetailedPlayerSanction extends PlayerSanction {

    private final UUID   author;
    private final String description;

    public DetailedPlayerSanction(int id, UUID player, UUID author, SanctionType sanctionType, String description, boolean canceled, OffsetDateTime startAt, OffsetDateTime endAt) {
        super(id, player, sanctionType, canceled, startAt, endAt);
        this.author = author;
        this.description = description;
    }

    public UUID getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DetailedPlayerSanction{" +
                "id=" + getId() +
                ", player=" + getPlayer() +
                ", author=" + author +
                ", sanctionType=" + getSanctionType() +
                ", description='" + description + '\'' +
                ", canceled=" + isCanceled() +
                ", startAt=" + getStartAt() +
                ", endAt=" + getEndAt() +
                '}';
    }
}
