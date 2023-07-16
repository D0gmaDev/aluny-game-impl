package fr.aluny.gameimpl.world.anchor;

import fr.aluny.gameapi.world.anchor.Anchor;
import java.util.*;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class AnchorWorldSource implements AnchorSource {

    private static final String     INVALID_NAME = "#tobedeleted";
    private static final EntityType MARKER_TYPE  = EntityType.MARKER;

    private static final Logger logger = Bukkit.getLogger();

    private final String                    worldName;
    private final Map<String, List<Anchor>> anchors = new HashMap<>();

    public AnchorWorldSource(World world) {
        this.worldName = world.getName();

        triggerLoads(world);
        loadAllInMemory(world);
    }

    @Override
    public Optional<Anchor> findOne(String key) {
        return Optional.ofNullable(anchors.get(key)).filter(list -> !list.isEmpty()).map(list -> list.get(0));
    }

    @Override
    public List<Anchor> findMany(String key) {
        return Optional.ofNullable(anchors.get(key)).map(Collections::unmodifiableList).orElse(List.of());
    }

    @Override
    public List<Anchor> findAll() {
        return anchors.values().stream().flatMap(List::stream).toList();
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    private void triggerLoads(World world) {
        List<Entity> entityList = worldFindLoad(world);
        if (entityList.size() == 0)
            return;

        boolean newlyLoaded = false;

        for (Entity entity : entityList) {
            if (entity.customName() != null && name(entity).equalsIgnoreCase(INVALID_NAME))
                continue;

            newlyLoaded = true;

            String[] parameters = entity.getName().split(" ");
            int chunkX = Integer.parseInt(parameters[1]) / 16;
            int chunkY = Integer.parseInt(parameters[2]) / 16;
            int radius = parameters.length >= 4 ? Integer.parseInt(parameters[3]) : 3;

            logger.info("[GAME-Anchor-AS] #load armor stand (" + chunkX + "," + chunkY + "," + radius + ")");

            for (int cx = chunkX - radius / 2; cx < chunkX + radius / 2; cx++)
                for (int cy = chunkY - radius / 2; cy < chunkY + radius / 2; cy++)
                    world.loadChunk(cx, cy);

            entity.customName(Component.text(INVALID_NAME));
            entity.remove();
        }

        if (newlyLoaded)
            triggerLoads(world);
    }

    private void loadAllInMemory(World world) {
        worldFindAll(world).stream()
                .filter(entity -> !entity.getName().equals(INVALID_NAME))
                .forEach(entity -> {
                    Anchor anchor = createAnchorFromEntity(entity);
                    logger.info("[GAME-Anchor-AS] Stored " + anchor);

                    if (!anchors.containsKey(anchor.key()))
                        anchors.put(anchor.key(), new ArrayList<>());

                    anchors.get(anchor.key()).add(anchor);

                    entity.remove();
                });
    }

    private Anchor createAnchorFromEntity(Entity entity) {
        return createAnchorFromString(name(entity), entity.getLocation());
    }

    private Anchor createAnchorFromString(String string, Location location) {

        // Only allow # format
        if (string.length() < 2 || !string.startsWith("#"))
            return null;

        // Remove #, split by spaces
        String[] split = string.substring(1).split(" ");

        String key = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        return new Anchor(key, args, location);
    }

    private List<Entity> worldFindAll(World world) {
        return world.getEntities().stream()
                .filter(entity -> entity.getType() == MARKER_TYPE && name(entity).startsWith("#"))
                .toList();
    }

    private List<Entity> worldFindLoad(World world) {
        return world.getEntities().stream()
                .filter(entity -> entity.getType() == MARKER_TYPE && name(entity).startsWith("#load"))
                .toList();
    }

    private String name(Entity entity) {
        Component customName = Optional.ofNullable(entity.customName()).orElse(Component.empty());
        return PlainTextComponentSerializer.plainText().serialize(customName);
    }
}
