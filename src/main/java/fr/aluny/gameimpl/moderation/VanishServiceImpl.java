package fr.aluny.gameimpl.moderation;

import fr.aluny.gameapi.moderation.VanishService;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.GamePlayerService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VanishServiceImpl implements VanishService, Listener {

    private final List<GamePlayer>  vanishPlayers = new ArrayList<>();
    private final JavaPlugin        plugin;
    private final GamePlayerService gamePlayerService;

    public VanishServiceImpl(JavaPlugin plugin, GamePlayerService gamePlayerService) {
        this.plugin = plugin;
        this.gamePlayerService = gamePlayerService;
    }

    @Override
    public List<GamePlayer> getVanishedPlayers() {
        return Collections.unmodifiableList(this.vanishPlayers);
    }

    @Override
    public boolean isVanished(GamePlayer gamePlayer) {
        return gamePlayer.isVanished();
    }

    private boolean isVanished(Player player) {
        return isVanished(gamePlayerService.getPlayer(player));
    }

    @Override
    public void vanishPlayer(GamePlayer gamePlayer) {
        this.vanishPlayers.add(gamePlayer);
        gamePlayer.setVanished(true);

        Bukkit.getOnlinePlayers().stream().filter(other -> !isVanished(other)).forEach(other -> other.hidePlayer(plugin, gamePlayer.getPlayer()));
        this.vanishPlayers.forEach(vanished -> gamePlayer.getPlayer().showPlayer(plugin, vanished.getPlayer()));

        setupVanishedPlayer(gamePlayer);
    }

    @Override
    public void unVanishPlayer(GamePlayer gamePlayer) {
        this.vanishPlayers.remove(gamePlayer);
        gamePlayer.setVanished(false);

        Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(plugin, gamePlayer.getPlayer()));
        this.vanishPlayers.forEach(vanished -> gamePlayer.getPlayer().hidePlayer(plugin, vanished.getPlayer()));
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onPlayerJoin(GamePlayer gamePlayer, boolean vanish) {
        if (vanish)
            vanishPlayer(gamePlayer);
        else
            this.vanishPlayers.forEach(vanished -> gamePlayer.getPlayer().hidePlayer(plugin, vanished.getPlayer()));
    }

    public void onPlayerQuit(GamePlayer gamePlayer) {
        this.vanishPlayers.remove(gamePlayer);
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

    private void setupVanishedPlayer(GamePlayer gamePlayer) {
        gamePlayer.setGameMode(GameMode.SPECTATOR);
        gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1, false, false));
        gamePlayer.clearInventory();
    }

}
