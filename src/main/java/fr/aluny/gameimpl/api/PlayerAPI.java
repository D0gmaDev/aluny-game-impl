package fr.aluny.gameimpl.api;

import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.ApiException;
import fr.aluny.alunyapi.generated.api.PlayerControllerApi;
import fr.aluny.alunyapi.generated.model.PlayerDTO;
import fr.aluny.alunyapi.generated.model.PlayerDetailsDTO;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameimpl.moderation.sanction.PlayerSanction;
import fr.aluny.gameimpl.player.DetailedPlayerAccount;
import fr.aluny.gameimpl.player.PlayerAccountImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerAPI {

    private final PlayerControllerApi apiInstance;
    private final ServiceManager      serviceManager;

    public PlayerAPI(ApiClient client, ServiceManager serviceManager) {
        this.apiInstance = new PlayerControllerApi(client);
        this.serviceManager = serviceManager;
    }

    public Optional<PlayerAccount> getPlayer(UUID uuid) {
        try {

            PlayerDTO player = apiInstance.getPlayer(uuid);
            return Optional.of(new PlayerAccountImpl(player.getUuid(), player.getUsername(), player.getCurrentServerId(), parseLocale(player.getLocale()), parseRanks(player.getRankIds()), player.getCreatedAt()));

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerControllerApi#getPlayer");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return Optional.empty();
        } catch (IllegalStateException e) {
            System.err.println("Exception when parsing '" + uuid + "' PlayerDTO: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<PlayerAccount> searchPlayersByName(String name) {
        try {

            return apiInstance.searchPlayers(name).stream()
                    .<PlayerAccount>map(player -> new PlayerAccountImpl(player.getUuid(), player.getUsername(), player.getCurrentServerId(), parseLocale(player.getLocale()), parseRanks(player.getRankIds()), player.getCreatedAt()))
                    .toList();

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerControllerApi#getPlayer");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return List.of();
        } catch (IllegalStateException e) {
            System.err.println("Exception when parsing '" + name + "' PlayerDTO: " + e.getMessage());
            return List.of();
        }
    }

    public Optional<PlayerAccount> getPlayerByName(String name) {
        return searchPlayersByName(name).stream().filter(playerAccount -> playerAccount.getName().equalsIgnoreCase(name)).findAny();
    }

    public Optional<DetailedPlayerAccount> getDetailedPlayer(UUID uuid) {
        try {

            PlayerDetailsDTO player = apiInstance.getPlayerDetails(uuid);

            List<PlayerSanction> sanctions = player.getCurrentSanctions() != null ? player.getCurrentSanctions().stream().map(PlayerSanctionAPI::buildSanction).toList() : List.of();
            boolean allowsPrivateMessages = player.getSettings() != null && player.getSettings().getCanReceivePrivateMessages() != null ? player.getSettings().getCanReceivePrivateMessages() : true;
            boolean vanished = player.getSettings() != null && player.getSettings().getIsVanishEnabled() != null ? player.getSettings().getIsVanishEnabled() : false;

            return Optional.of(new DetailedPlayerAccount(player.getUuid(), player.getUsername(), player.getCurrentServerId(), parseLocale(player.getLocale()), parseRanks(player.getRankIds()), player.getCreatedAt(), sanctions, allowsPrivateMessages, vanished));

        } catch (ApiException e) {
            System.err.println("Exception when calling PlayerControllerApi#getPlayerDetails");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return Optional.empty();
        } catch (IllegalStateException e) {
            System.err.println("Exception when parsing '" + uuid + "' PlayerDetailsDTO: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Locale parseLocale(String code) {
        return serviceManager.getTranslationService().getLocale(code).orElseThrow(() -> new IllegalStateException("locale: " + code));
    }

    private Set<Rank> parseRanks(Set<Integer> ids) {
        if (ids == null)
            throw new IllegalStateException("rankIds is null");

        return ids.stream().map(id -> serviceManager.getRankService().getRankById(id).orElseThrow(() -> new IllegalStateException("rank: " + id))).collect(Collectors.toUnmodifiableSet());
    }
}
