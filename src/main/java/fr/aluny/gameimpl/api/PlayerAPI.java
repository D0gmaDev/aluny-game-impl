package fr.aluny.gameimpl.api;

import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.ApiException;
import fr.aluny.alunyapi.generated.api.PlayerControllerApi;
import fr.aluny.alunyapi.generated.model.PlayerDTO;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameimpl.player.PlayerAccountImpl;
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

            Optional<Locale> locale = serviceManager.getTranslationService().getLocale(player.getLocale());

            if (locale.isEmpty())
                throw new IllegalStateException("locale: " + player.getLocale());

            if (player.getRankIds() == null)
                throw new IllegalStateException("rankIds is null");

            Set<Rank> ranks = player.getRankIds().stream().map(id -> serviceManager.getRankService().getRankById(id).orElseThrow(() -> new IllegalStateException("rank: " + id))).collect(Collectors.toUnmodifiableSet());

            PlayerAccountImpl playerAccount = new PlayerAccountImpl(player.getUuid(), player.getUsername(), locale.get(), ranks, player.getCreatedAt());
            return Optional.of(playerAccount);

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
}
