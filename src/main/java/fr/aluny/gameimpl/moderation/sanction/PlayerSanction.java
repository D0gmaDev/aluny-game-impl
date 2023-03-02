package fr.aluny.gameimpl.moderation.sanction;

import fr.aluny.gameapi.utils.TimeUtils;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PlayerSanction {

    private final int  id;
    private final UUID player;

    private final SanctionType sanctionType;
    private final boolean      canceled;

    private final OffsetDateTime startAt;
    private final OffsetDateTime endAt;

    public PlayerSanction(int id, UUID player, SanctionType sanctionType, boolean canceled, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.id = id;
        this.player = player;
        this.sanctionType = sanctionType;
        this.canceled = canceled;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public int getId() {
        return id;
    }

    public UUID getPlayer() {
        return player;
    }

    public SanctionType getSanctionType() {
        return sanctionType;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public boolean isType(SanctionType sanctionType) {
        return this.sanctionType == sanctionType;
    }

    public boolean isActive() {
        return !isCanceled() && TimeUtils.isBetween(OffsetDateTime.now(), getStartAt(), getEndAt());
    }

    @Override
    public String toString() {
        return "PlayerSanction{" +
                "id=" + id +
                ", player=" + player +
                ", sanctionType=" + sanctionType +
                ", canceled=" + canceled +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }
}
