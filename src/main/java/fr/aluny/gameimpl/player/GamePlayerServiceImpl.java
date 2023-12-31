package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
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

    public GamePlayer onPlayerJoin(Player player, PlayerAccount account) {
        if (gamePlayerMap.containsKey(player.getUniqueId()) && gamePlayerMap.get(player.getUniqueId()) instanceof OfflineGamePlayerImpl offlineGamePlayer) {
            offlineGamePlayer.applyDataToPlayer(player);
        }

        GamePlayer gamePlayer = new GamePlayerImpl(player, account);
        gamePlayerMap.put(player.getUniqueId(), gamePlayer);
        return gamePlayer;
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

    public String getCachedLocaleCode(Player player) {
        GamePlayer gamePlayer = getPlayer(player);
        Locale locale = gamePlayer instanceof GamePlayerImpl gamePlayerImpl ? gamePlayerImpl.getCachedLocale()
                : serviceManager.getPlayerAccountService().getPlayerAccount(gamePlayer).getLocale(); // accurate backup, but should not happen
        return locale.getCode();
    }

    public int getPlayersDataSize() {
        return this.gamePlayerMap.size();
    }

    public void clearOfflinePlayersData() {
        this.gamePlayerMap.values().removeIf(Predicate.not(OfflineGamePlayer::isOnline));
    }
}
