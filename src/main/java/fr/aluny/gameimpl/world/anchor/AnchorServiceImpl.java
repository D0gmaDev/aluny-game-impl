package fr.aluny.gameimpl.world.anchor;

import fr.aluny.gameapi.world.anchor.Anchor;
import fr.aluny.gameapi.world.anchor.AnchorService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class AnchorServiceImpl implements AnchorService {

    private static final Logger logger = Bukkit.getLogger();

    private final List<AnchorSource> sources = new ArrayList<>();

    @Override
    public void initializeWorld(World world) {
        logger.info("[GAME-Anchor] Initializing world " + world.getName());
        addSource(new AnchorWorldSource(world));
        logger.info("[GAME-Anchor] Initialized world " + world.getName());
    }

    @Override
    public void unregisterWorld(World world) {
        logger.info("[GAME-Anchor] Unregistering world " + world.getName());
        new ArrayList<>(sources).stream()
                .filter(source -> source instanceof AnchorWorldSource anchorWorldSource && world.equals(anchorWorldSource.getWorld()))
                .forEach(this::removeSource);
        logger.info("[GAME-Anchor] Unregistered world " + world.getName());
    }

    @Override
    public Optional<Anchor> findOne(String key) {
        return sources.stream().map(anchorSource -> anchorSource.findOne(key)).findAny().orElse(Optional.empty());
    }

    @Override
    public List<Anchor> findMany(String key) {
        return sources.stream().map(anchorSource -> anchorSource.findMany(key)).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public List<Anchor> findAll() {
        return sources.stream().map(AnchorSource::findAll).flatMap(List::stream).collect(Collectors.toList());
    }

    public void addSource(AnchorSource source) {
        sources.add(source);
    }

    public void removeSource(AnchorSource source) {
        sources.remove(source);
    }
}
