package fr.aluny.gameimpl.moderation;

import fr.aluny.gameapi.moderation.VanishService;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.GamePlayerService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishServiceImpl implements VanishService, Listener {

    private final Set<GamePlayer>   vanishPlayers = new HashSet<>();
    private final JavaPlugin        plugin;
    private final GamePlayerService gamePlayerService;

    public VanishServiceImpl(JavaPlugin plugin, GamePlayerService gamePlayerService) {
        this.plugin = plugin;
        this.gamePlayerService = gamePlayerService;
    }

    @Override
    public Set<GamePlayer> getVanishedPlayers() {
        return Collections.unmodifiableSet(this.vanishPlayers);
    }

    @Override
    public boolean isVanished(GamePlayer gamePlayer) {
        return this.vanishPlayers.contains(gamePlayer);
    }

    private boolean isVanished(Player player) {
        return isVanished(gamePlayerService.getPlayer(player));
    }

    @Override
    public void vanishPlayer(GamePlayer gamePlayer) {
        this.vanishPlayers.add(gamePlayer);

        Bukkit.getOnlinePlayers().stream().filter(other -> !isVanished(other)).forEach(other -> other.hidePlayer(plugin, gamePlayer.getPlayer()));
        getVanishedPlayers().forEach(vanished -> gamePlayer.getPlayer().showPlayer(plugin, vanished.getPlayer()));
    }

    @Override
    public void unVanishPlayer(GamePlayer gamePlayer) {
        this.vanishPlayers.remove(gamePlayer);

        Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(plugin, gamePlayer.getPlayer()));
        getVanishedPlayers().forEach(vanished -> gamePlayer.getPlayer().hidePlayer(plugin, vanished.getPlayer()));
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onPlayerJoin(Player player) {
        getVanishedPlayers().forEach(vanished -> player.hidePlayer(plugin, vanished.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (isVanished(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrop(PlayerDropItemEvent event) {
        if (isVanished(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && isVanished(player))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isVanished(player))
            event.setCancelled(true);
    }

}
