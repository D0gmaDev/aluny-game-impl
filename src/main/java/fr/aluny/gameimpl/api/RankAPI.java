package fr.aluny.gameimpl.api;

import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.ApiException;
import fr.aluny.alunyapi.generated.api.RankControllerApi;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameimpl.player.rank.RankImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RankAPI {

    private final RankControllerApi apiInstance;

    public RankAPI(ApiClient client) {
        this.apiInstance = new RankControllerApi(client);
    }

    public List<Rank> loadAllRanks() {
        try {

            return apiInstance.getAll().stream()
                    .map(rankDTO -> new RankImpl(rankDTO.getId(), rankDTO.getName(), rankDTO.getImportanceIndex(), rankDTO.getPrefix(), rankDTO.getColor(), rankDTO.getPermissions()))
                    .collect(Collectors.toList());

        } catch (ApiException e) {
            System.err.println("Exception when calling RankControllerApi#getAll");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
