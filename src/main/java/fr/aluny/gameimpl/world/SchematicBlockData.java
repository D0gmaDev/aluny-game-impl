package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import org.bukkit.Location;

public interface SchematicBlockData {

    void paste(Location location);

    void flip(Axis axis);

    void rotate(int quart);

}
