package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import org.bukkit.Location;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;

public interface SchematicEntityData {

    Entity summon(Location location);

    SchematicEntityData flip(Axis axis);

    SchematicEntityData rotate(StructureRotation rotation);

}
