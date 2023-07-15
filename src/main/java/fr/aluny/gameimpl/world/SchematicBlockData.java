package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import org.bukkit.Location;
import org.bukkit.block.structure.StructureRotation;

public interface SchematicBlockData {

    void paste(Location location);

    default SchematicBlockData flip(Axis axis) {
        return this; // when flip doesn't affect the block
    }

    default SchematicBlockData rotate(StructureRotation rotation) {
        return this; // when rotate doesn't affect the block
    }

}
