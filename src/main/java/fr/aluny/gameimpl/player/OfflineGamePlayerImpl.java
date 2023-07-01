package fr.aluny.gameimpl.player;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameimpl.message.DummyMessageHandler;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OfflineGamePlayerImpl implements OfflineGamePlayer {

    private static final MessageHandler DUMMY_MESSAGE_HANDLER = new DummyMessageHandler();

    private final UUID    uuid;
    private final String  name;
    private final Instant lastConnected;

    private Location                 location;
    private Collection<PotionEffect> activePotionsEffects;
    private GameMode                 gameMode;
    private PlayerInventory          inventory;
    private int                      level;
    private float                    experience;
    private double                   health;
    private int                      foodLevel;
    private float                    walkSpeed;
    private boolean                  allowFlight;
    private AttributeInstance        healthAttribute;

    public OfflineGamePlayerImpl(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.lastConnected = Instant.now();

        this.location = player.getLocation();
        this.activePotionsEffects = player.getActivePotionEffects();
        this.gameMode = player.getGameMode();
        this.inventory = player.getInventory();
        this.level = player.getLevel();
        this.experience = player.getExp();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.walkSpeed = player.getWalkSpeed();
        this.allowFlight = player.getAllowFlight();

        this.healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    }

    public void applyDataToPlayer(Player player) {
        player.teleport(this.location);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.addPotionEffects(this.activePotionsEffects);
        player.setGameMode(this.gameMode);

        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();
        playerInventory.setContents(this.inventory.getContents());
        //TODO armor ?

        player.setLevel(this.level);
        player.setExp(this.experience);
        player.setFoodLevel(this.foodLevel);
        player.setWalkSpeed(this.walkSpeed);
        player.setAllowFlight(this.allowFlight);

        AttributeInstance playerHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        this.healthAttribute.getModifiers().forEach(playerHealth::addModifier);

        player.setHealth(this.health);
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getPlayerName() {
        return this.name;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public Instant getLastConnectedInstant() {
        return this.lastConnected;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public void teleport(Location location) {
        this.location = location;
    }

    @Override
    public Collection<PotionEffect> getPotionEffects() {
        return this.activePotionsEffects;
    }

    @Override
    public void setPotionEffects(Collection<PotionEffect> potionEffects) {
        this.activePotionsEffects = potionEffects;
    }

    @Override
    public void addPotionEffect(PotionEffect potionEffect) {
        this.activePotionsEffects.add(potionEffect);
    }

    @Override
    public void removePotionEffect(PotionEffectType potionEffectType) {
        this.activePotionsEffects.removeIf(potionEffect -> potionEffect.getType() == potionEffectType);
    }

    @Override
    public GameMode getGameMode() {
        return this.gameMode;
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public PlayerInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void clearInventory() {
        this.inventory.clear();
    }

    @Override
    public int getLevels() {
        return this.level;
    }

    @Override
    public float getExperience() {
        return this.experience;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void setExperience(float experience) {
        this.experience = experience;
    }

    @Override
    public double getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(double health) {
        this.health = health;
    }

    @Override
    public double getMaxHealth() {
        return getMaxHealthAttribute().getValue();
    }

    @Override
    public AttributeInstance getMaxHealthAttribute() {
        return this.healthAttribute;
    }

    @Override
    public int getFoodLevel() {
        return this.foodLevel;
    }

    @Override
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    @Override
    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    @Override
    public void setWalkSpeed(float speed) {
        this.walkSpeed = speed;
    }

    @Override
    public boolean isAllowFlight() {
        return this.allowFlight;
    }

    @Override
    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

    @Override
    public MessageHandler getMessageHandler() {
        return DUMMY_MESSAGE_HANDLER;
    }
}
