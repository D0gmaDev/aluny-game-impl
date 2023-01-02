package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.api.PlayerAPI;
import java.util.Optional;
import java.util.UUID;

public class PlayerAccountServiceImpl implements PlayerAccountService {

    private final PlayerAPI      playerAPI;
    private final ServiceManager serviceManager;

    public PlayerAccountServiceImpl(PlayerAPI playerAPI, ServiceManager serviceManager) {
        this.playerAPI = playerAPI;
        this.serviceManager = serviceManager;
    }

    @Override
    public PlayerAccount getPlayerAccount(OfflineGamePlayer gamePlayer) {
        return playerAPI.getPlayer(gamePlayer.getUuid()).orElseThrow();
    }

    @Override
    public Optional<PlayerAccount> getPlayerAccount(UUID uuid) {
        return playerAPI.getPlayer(uuid);
    }
}
