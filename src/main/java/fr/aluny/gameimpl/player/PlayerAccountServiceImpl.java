package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.api.PlayerAPI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;

public class PlayerAccountServiceImpl implements PlayerAccountService {

    private final PlayerAPI      playerAPI;
    private final ServiceManager serviceManager;

    private final Map<UUID, CachedAccount> cacheMap = new HashMap<>();

    private BukkitTask cacheRemoveTask;

    public PlayerAccountServiceImpl(PlayerAPI playerAPI, ServiceManager serviceManager) {
        this.playerAPI = playerAPI;
        this.serviceManager = serviceManager;
    }

    @Override
    public void initialize() {
        this.cacheRemoveTask = serviceManager.getRunnableHelper().runTimerAsynchronously(() -> {
            Instant now = Instant.now();
            this.cacheMap.values().removeIf(cachedAccount -> cachedAccount.expirationTime().isBefore(now));
        }, 20 * 5, 20 * 30);
    }

    @Override
    public void shutdown() {
        if (this.cacheRemoveTask != null)
            this.cacheRemoveTask.cancel();
    }

    @Override
    public PlayerAccount getPlayerAccount(OfflineGamePlayer gamePlayer) {

        CachedAccount cache = this.cacheMap.get(gamePlayer.getUuid());

        if (cache != null && cache.expirationTime().isAfter(Instant.now()))
            return cache.account();

        PlayerAccount account = playerAPI.getPlayer(gamePlayer.getUuid()).orElseThrow();
        return saveToCache(account);
    }

    @Override
    public PlayerAccount getPlayerAccount(OfflineGamePlayer gamePlayer, boolean forceRefresh) {
        return forceRefresh ? playerAPI.getPlayer(gamePlayer.getUuid()).map(this::saveToCache).orElseThrow() : getPlayerAccount(gamePlayer);
    }

    @Override
    public Optional<PlayerAccount> getPlayerAccount(UUID uuid) {
        return playerAPI.getPlayer(uuid).map(this::saveToCache);
    }

    @Override
    public Optional<PlayerAccount> getPlayerAccountByName(String name) {
        return playerAPI.getPlayerByName(name).map(this::saveToCache);
    }

    public Optional<DetailedPlayerAccount> getDetailedPlayerAccount(UUID uuid) {
        return playerAPI.getDetailedPlayer(uuid);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return getPlayerAccount(uuid).map(PlayerAccount::isOnline).orElse(false);
    }

    public int getCacheSize() {
        return this.cacheMap.size();
    }

    public void clearCache() {
        this.cacheMap.clear();
    }

    private PlayerAccount saveToCache(PlayerAccount playerAccount) {
        this.cacheMap.put(playerAccount.getUuid(), new CachedAccount(playerAccount, Instant.now().plusSeconds(30)));
        return playerAccount;
    }

    private record CachedAccount(PlayerAccount account, Instant expirationTime) {

    }
}
