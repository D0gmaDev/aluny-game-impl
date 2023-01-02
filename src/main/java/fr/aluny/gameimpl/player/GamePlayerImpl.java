package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.scoreboard.PlayerScoreboard;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeam;
import fr.aluny.gameimpl.message.PlayerMessageHandler;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GamePlayerImpl implements GamePlayer {

    private final Player           player;
    private final MessageHandler   messageHandler;
    private       ScoreboardTeam   scoreboardTeam;
    private       PlayerScoreboard scoreboard;
    private       boolean          vanished;

    public GamePlayerImpl(Player player, PlayerAccount playerBean) {
        this.player = player;
        this.messageHandler = new PlayerMessageHandler(player, playerBean.getLocale());
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public int getPing() {
        return this.player.getPing();
    }

    @Override
    public Optional<ScoreboardTeam> getScoreboardTeam() {
        return Optional.ofNullable(this.scoreboardTeam);
    }

    @Override
    public void setScoreboardTeam(ScoreboardTeam scoreboardTeam) {
        this.scoreboardTeam = scoreboardTeam;
    }

    @Override
    public Optional<PlayerScoreboard> getScoreboard() {
        return Optional.ofNullable(this.scoreboard);
    }

    @Override
    public void setScoreboard(PlayerScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public boolean isVanished() {
        return this.vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
    }

    @Override
    public UUID getUuid() {
        return this.player.getUniqueId();
    }

    @Override
    public String getPlayerName() {
        return this.player.getName();
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public Instant getLastConnectedInstant() {
        return Instant.now();
    }

    @Override
    public Location getLocation() {
        return this.player.getLocation();
    }

    @Override
    public void teleport(Location location) {
        this.player.teleport(location);
    }

    @Override
    public Collection<PotionEffect> getPotionEffects() {
        return this.player.getActivePotionEffects();
    }

    @Override
    public void setPotionEffects(Collection<PotionEffect> potionEffects) {
        this.player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(this::removePotionEffect);
        this.player.addPotionEffects(potionEffects);
    }

    @Override
    public void addPotionEffect(PotionEffect potionEffect) {
        this.player.addPotionEffect(potionEffect);
    }

    @Override
    public void removePotionEffect(PotionEffectType potionEffectType) {
        this.player.removePotionEffect(potionEffectType);
    }

    @Override
    public PlayerInventory getInventory() {
        return this.player.getInventory();
    }

    @Override
    public void clearInventory() {
        this.player.getInventory().clear();
    }

    @Override
    public int getLevels() {
        return this.player.getLevel();
    }

    @Override
    public float getExperience() {
        return this.player.getExp();
    }

    @Override
    public void setLevel(int level) {
        this.player.setLevel(level);
    }

    @Override
    public void setExperience(float experience) {
        this.player.setExp(experience);
    }

    @Override
    public double getHealth() {
        return this.player.getHealth();
    }

    @Override
    public void setHealth(double health) {
        this.player.setHealth(health);
    }

    @Override
    public double getMaxHealth() {
        return Optional.ofNullable(getMaxHealthAttribute()).map(AttributeInstance::getValue).orElse(20d);
    }

    @Override
    public AttributeInstance getMaxHealthAttribute() {
        return this.player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    }

    @Override
    public int getFoodLevel() {
        return this.player.getFoodLevel();
    }

    @Override
    public void setFoodLevel(int foodLevel) {
        this.player.setFoodLevel(foodLevel);
    }

    @Override
    public float getWalkSpeed() {
        return this.player.getWalkSpeed();
    }

    @Override
    public void setWalkSpeed(float speed) {
        this.player.setWalkSpeed(speed);
    }

    @Override
    public boolean isAllowFlight() {
        return this.player.getAllowFlight();
    }

    @Override
    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }
}
