package fr.aluny.gameimpl.world.anchor;

import fr.aluny.gameapi.world.anchor.Anchor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class AnchorWorldSource extends AnchorSource {

    private static final String     INVALID_NAME = "#tobedeleted";
    private static final EntityType MARKER_TYPE  = EntityType.MARKER;

    private static final Logger logger = Bukkit.getLogger();

    private final String       worldName;
    private final List<Anchor> anchors = new ArrayList<>();

    public AnchorWorldSource(World world) {
        this.worldName = world.getName();
        triggerLoads(world);
        loadAllInMemory(world);
    }

    @Override
    public Optional<Anchor> findOne(String key) {
        return anchors.stream().filter(anchor -> anchor.key().equals(key)).findAny();
    }

    @Override
    public List<Anchor> findMany(String key) {
        return anchors.stream().filter(anchor -> anchor.key().equals(key)).collect(Collectors.toList());
    }

    @Override
    public List<Anchor> findAll() {
        return new ArrayList<>(anchors);
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(worldName));
    }

    private void triggerLoads(World world) {
        List<Entity> entityList = worldFindLoad(world);
        if (entityList.size() == 0)
            return;

        int newlyLoaded = 0;
        for (Entity e : entityList) {
            if (e.getCustomName() != null && e.getCustomName().equalsIgnoreCase(INVALID_NAME))
                continue;
            newlyLoaded++;

            String[] parameters = e.getName().split(" ");
            int chunkX = Integer.parseInt(parameters[1]) / 16;
            int chunkY = Integer.parseInt(parameters[2]) / 16;
            int radius = 3;
            if (parameters.length >= 4)
                radius = Integer.parseInt(parameters[3]);

            logger.info("[GAME-Anchor-AS] #load armor stand (" + chunkX + "," + chunkY + "," + radius + ")");
            for (int cx = chunkX - radius / 2; cx < chunkX + radius / 2; cx++)
                for (int cy = chunkY - radius / 2; cy < chunkY + radius / 2; cy++)
                    world.loadChunk(cx, cy);

            e.setCustomName(INVALID_NAME);
            e.remove();
        }

        if (newlyLoaded > 0)
            triggerLoads(world);
    }

    private void loadAllInMemory(World world) {
        List<Entity> armorStands = worldFindAll(world);
        for (Entity entity : armorStands) {
            if (entity.getCustomName() != null && entity.getCustomName().equals(INVALID_NAME))
                continue;

            Anchor anchor = createAnchorFromEntity(entity);
            logger.info("[GAME-Anchor-AS] Stored " + anchor);
            anchors.add(anchor);
            entity.remove();
        }
    }

    private Anchor createAnchorFromEntity(Entity entity) {
        return createAnchorFromString(entity.getName(), entity.getLocation());
    }

    private List<Entity> worldFindAll(World world) {
        ArrayList<Entity> entities = new ArrayList<>();

        for (Entity entity : world.getEntities())
            if (entity.getType() == MARKER_TYPE && entity.getName().startsWith("#"))
                entities.add(entity);

        return entities;
    }

    private List<Entity> worldFindLoad(World world) {
        ArrayList<Entity> entities = new ArrayList<>();

        for (Entity entity : world.getEntities())
            if (entity.getType() == MARKER_TYPE && entity.getName().startsWith("#load"))
                entities.add(entity);

        return entities;
    }
}
