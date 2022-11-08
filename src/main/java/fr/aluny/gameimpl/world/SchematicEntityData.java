package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchematicEntityData {

    Entity summon(Location location);

    void flip(Axis axis);

    void rotate(int quart);

}
