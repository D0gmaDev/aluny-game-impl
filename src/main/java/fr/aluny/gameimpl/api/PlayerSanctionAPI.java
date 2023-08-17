package fr.aluny.gameimpl.api;

import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.ApiException;
import fr.aluny.alunyapi.generated.api.PlayerSanctionControllerApi;
import fr.aluny.alunyapi.generated.model.PlayerSanctionDTO;
import fr.aluny.alunyapi.generated.model.PlayerSanctionDetailsDTO;
import fr.aluny.alunyapi.generated.model.PlayerSanctionEditDTO;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameimpl.moderation.sanction.DetailedPlayerSanction;
import fr.aluny.gameimpl.moderation.sanction.PlayerSanction;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public class PlayerSanctionAPI {

    private final PlayerSanctionControllerApi apiInstance;

    public PlayerSanctionAPI(ApiClient client) {
        this.apiInstance = new PlayerSanctionControllerApi(client);
    }

    public Optional<PlayerSanction> applySanction(PlayerAccount player, OfflineGamePlayer author, SanctionType sanctionType, TemporalAmount duration, String reason) {

        OffsetDateTime now = OffsetDateTime.now();

        PlayerSanctionDetailsDTO playerSanctionDetailsDTO = new PlayerSanctionDetailsDTO();
        playerSanctionDetailsDTO.playerUuid(player.getUuid()).fromPlayerUuid(author.getUuid()).type(getDetailsTypeEnum(sanctionType)).startAt(now).endAt(now.plus(duration)).cancelled(false).description(reason);

        try {
            PlayerSanctionDetailsDTO result = apiInstance.create(playerSanctionDetailsDTO);
            return Optional.of(buildSanction(result));

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerSanctionControllerApi#create");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return Optional.empty();
        }

    }

    public Optional<DetailedPlayerSanction> cancelSanction(int sanctionId) {
        PlayerSanctionEditDTO playerSanctionEditDTO = new PlayerSanctionEditDTO().cancelled(true);
        try {
            PlayerSanctionDetailsDTO result = apiInstance.update(sanctionId, playerSanctionEditDTO);
            return Optional.of(buildSanction(result));

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerSanctionControllerApi#update");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<PlayerSanction> getPlayerSanctions(PlayerAccount player, int count, int page) {
        try {
            List<PlayerSanctionDTO> resultList = apiInstance.getAll1(player.getUuid(), count, page);

            return resultList.stream().map(result -> new PlayerSanction(result.getId().intValue(), result.getPlayerUuid(), getSanctionType(result.getType()), result.getCancelled().booleanValue(), result.getStartAt(), result.getEndAt())).toList();

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerSanctionControllerApi#getAll1");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return List.of();
        }
    }

    public List<DetailedPlayerSanction> getPlayerDetailedSanctions(PlayerAccount player, int count, int page) {
        try {
            List<PlayerSanctionDetailsDTO> resultList = apiInstance.getDetails(player.getUuid(), count, page);
            return resultList.stream().map(PlayerSanctionAPI::buildSanction).toList();

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerSanctionControllerApi#getDetails");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return List.of();
        }
    }

    public Optional<DetailedPlayerSanction> getPlayerSanctionById(int id) {
        try {
            PlayerSanctionDetailsDTO result = apiInstance.getById1(id);
            return Optional.of(buildSanction(result));

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerSanctionControllerApi#getById1");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    static PlayerSanction buildSanction(PlayerSanctionDTO sanction) {
        return new PlayerSanction(sanction.getId().intValue(), sanction.getPlayerUuid(), getSanctionType(sanction.getType()), sanction.getCancelled().booleanValue(), sanction.getStartAt(), sanction.getEndAt());
    }

    static DetailedPlayerSanction buildSanction(PlayerSanctionDetailsDTO sanction) {
        return new DetailedPlayerSanction(sanction.getId().intValue(), sanction.getPlayerUuid(), sanction.getFromPlayerUuid(), getSanctionType(sanction.getType()), sanction.getDescription(), sanction.getCancelled().booleanValue(), sanction.getStartAt(), sanction.getEndAt());
    }

    private static SanctionType getSanctionType(PlayerSanctionDTO.TypeEnum typeEnum) {
        return switch (typeEnum) {
            case UNKNOWN -> SanctionType.UNKNOWN;
            case BAN -> SanctionType.BAN;
            case MUTE -> SanctionType.MUTE;
            case KICK -> SanctionType.KICK;
        };
    }

    private static SanctionType getSanctionType(PlayerSanctionDetailsDTO.TypeEnum typeEnum) {
        return switch (typeEnum) {
            case UNKNOWN -> SanctionType.UNKNOWN;
            case BAN -> SanctionType.BAN;
            case MUTE -> SanctionType.MUTE;
            case KICK -> SanctionType.KICK;
        };
    }

    private static PlayerSanctionDTO.TypeEnum getTypeEnum(SanctionType sanctionType) {
        return switch (sanctionType) {
            case UNKNOWN -> PlayerSanctionDTO.TypeEnum.UNKNOWN;
            case BAN -> PlayerSanctionDTO.TypeEnum.BAN;
            case MUTE -> PlayerSanctionDTO.TypeEnum.MUTE;
            case KICK -> PlayerSanctionDTO.TypeEnum.KICK;
        };
    }

    private static PlayerSanctionDetailsDTO.TypeEnum getDetailsTypeEnum(SanctionType sanctionType) {
        return switch (sanctionType) {
            case UNKNOWN -> PlayerSanctionDetailsDTO.TypeEnum.UNKNOWN;
            case BAN -> PlayerSanctionDetailsDTO.TypeEnum.BAN;
            case MUTE -> PlayerSanctionDetailsDTO.TypeEnum.MUTE;
            case KICK -> PlayerSanctionDetailsDTO.TypeEnum.KICK;
        };
    }

}
