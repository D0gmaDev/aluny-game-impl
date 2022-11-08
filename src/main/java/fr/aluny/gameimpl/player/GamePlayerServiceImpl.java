package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

public class GamePlayerServiceImpl implements GamePlayerService {

    private final Map<UUID, OfflineGamePlayer> gamePlayerMap = new HashMap<>();

    private final ServiceManager serviceManager;

    public GamePlayerServiceImpl(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public Optional<OfflineGamePlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(this.gamePlayerMap.get(uuid));
    }

    @Override
    public GamePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId())
                .filter(GamePlayer.class::isInstance).map(GamePlayer.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("player '" + player.getName() + "' is not online"));
    }

    public void onPlayerJoin(Player player) {
        if (gamePlayerMap.containsKey(player.getUniqueId())) {
            OfflineGamePlayerImpl offlineGamePlayer = ((OfflineGamePlayerImpl) gamePlayerMap.get(player.getUniqueId()));
            offlineGamePlayer.applyDataToPlayer(player);
        }

        //todo playerBean
        GamePlayer gamePlayer = new GamePlayerImpl(player, () -> serviceManager.getTranslationService().getDefaultLocale());
        gamePlayerMap.put(player.getUniqueId(), gamePlayer);

        serviceManager.getScoreboardTeamService().getScoreboardTeam("godTeam").ifPresent(scoreboardTeam -> scoreboardTeam.addPlayer(gamePlayer));
    }

    public void onPlayerQuit(Player player, boolean save) {

        if (gamePlayerMap.get(player.getUniqueId()) instanceof GamePlayer gamePlayer)
            gamePlayer.getScoreboardTeam().ifPresent(scoreboardTeam -> scoreboardTeam.removePlayer(gamePlayer));

        if (!save) {
            gamePlayerMap.remove(player.getUniqueId());
            return;
        }

        gamePlayerMap.put(player.getUniqueId(), new OfflineGamePlayerImpl(player));
    }
}
