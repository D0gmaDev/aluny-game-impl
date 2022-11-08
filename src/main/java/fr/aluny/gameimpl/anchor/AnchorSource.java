package fr.aluny.gameimpl.anchor;

import fr.aluny.gameapi.anchor.Anchor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class AnchorSource {

    public abstract Optional<Anchor> findOne(String key);

    public abstract List<Anchor> findMany(String key);

    public abstract List<Anchor> findAll();

    public abstract Optional<World> getWorld();

    protected static Anchor createAnchorFromString(String string, Location location) {
        // Only allow # format
        if (!string.startsWith("#"))
            return null;

        // Remove #, split by spaces
        String[] split = string.substring(1).split(" ");

        String key = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        return new Anchor(key, args, location);
    }
}
