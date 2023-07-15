package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import fr.aluny.gameapi.world.Schematic;
import fr.aluny.gameapi.world.SchematicPasteSpec;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;

public record SchematicImpl(int version, short height, short width, short length, SchematicBlockData[] blocksData, SchematicEntityData[] entitiesData, short[] blocks, int airData) implements Schematic {

    @Override
    public void paste(Location location) {
        paste(location, SchematicPasteSpec.create());
    }

    @Override
    public void paste(Location location, Consumer<Block> blockConsumer) {
        paste(location, SchematicPasteSpec.create().onBlockPlace(blockConsumer));
    }

    @Override
    public void paste(Location location, Consumer<Block> blockConsumer, boolean withEntities) {
        paste(location, SchematicPasteSpec.create().onBlockPlace(blockConsumer).withEntities(withEntities));
    }

    @Override
    public void paste(Location location, Consumer<Block> blockConsumer, boolean withEntities, Consumer<Entity> entityConsumer) {
        paste(location, SchematicPasteSpec.create().onBlockPlace(blockConsumer).withEntities(withEntities).onEntitySpawn(entityConsumer));
    }

    @Override
    public void paste(Location location, SchematicPasteSpec schematicPasteSpec) {
        World world = location.getWorld();

        int widthTimesLength = width * length;

        int x = 0;
        while (x < width) {
            int y = 0;
            while (y < height) {
                int z = 0;
                while (z < length) {
                    int index = y * widthTimesLength + z * width + x;
                    short blockId = blocks[index];

                    if ((schematicPasteSpec.doesPasteAir() || blockId != airData)) {
                        Location blockLocation = new Location(world, (double) x + location.getX(), (double) y + location.getY(), (double) z + location.getZ());
                        blocksData[blockId].paste(blockLocation);
                        schematicPasteSpec.getBlockPlaceConsumer().ifPresent(blockConsumer -> blockConsumer.accept(blockLocation.getBlock()));
                    }
                    ++z;
                }
                ++y;
            }
            ++x;
        }

        /* Summon entities */
        if (schematicPasteSpec.doesSpawnEntities()) {
            schematicPasteSpec.getEntitySpawnConsumer().ifPresentOrElse(
                    entityConsumer -> Arrays.stream(entitiesData).map(entityData -> entityData.summon(location)).forEach(entityConsumer),
                    () -> Arrays.stream(entitiesData).forEach(entityData -> entityData.summon(location))
            );
        }
    }

    @Override
    public Schematic rotate(StructureRotation rotation) {

        if (rotation == StructureRotation.NONE)
            return this;

        int quart = switch (rotation) {
            case CLOCKWISE_90 -> 3;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 1;
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };

        short[] newBlocks = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int[] newCoords = getRotationNewCoords(width, length, x, z, quart);
            int newId = y * width * length + newCoords[1] * (quart % 2 == 0 ? width : length) + newCoords[0];
            newBlocks[newId] = blocks[i];
        }

        short newWidth = quart % 2 == 1 ? length : width;
        short newLength = quart % 2 == 1 ? width : length;

        SchematicBlockData[] newBlocksData = Arrays.stream(blocksData).map(blockData -> blockData.rotate(rotation)).toArray(SchematicBlockData[]::new);
        SchematicEntityData[] newEntitiesData = Arrays.stream(entitiesData).map(entityData -> entityData.rotate(rotation)).toArray(SchematicEntityData[]::new);

        return new SchematicImpl(version, height, newWidth, newLength, newBlocksData, newEntitiesData, newBlocks, airData);
    }

    @Override
    public Schematic flip(Axis axis) {
        return switch (axis) {
            case X -> flipX();
            case Y -> flipY();
            case Z -> flipZ();
        };
    }

    @Override
    public Schematic flipX() {
        short[] newBlocks = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int newId = y * width * length + (length - 1 - z) * width + x;
            newBlocks[newId] = blocks[i];
        }

        SchematicBlockData[] newBlocksData = Arrays.stream(blocksData).map(blockData -> blockData.flip(Axis.X)).toArray(SchematicBlockData[]::new);
        SchematicEntityData[] newEntitiesData = Arrays.stream(entitiesData).map(entityData -> entityData.flip(Axis.X)).toArray(SchematicEntityData[]::new);

        return new SchematicImpl(version, height, width, length, newBlocksData, newEntitiesData, newBlocks, airData);
    }

    @Override
    public Schematic flipY() {
        short[] newBlocks = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int height = blocks.length / (width * length);
            int newId = (height - 1 - y) * width * length + z * width + x;
            newBlocks[newId] = blocks[i];
        }

        SchematicBlockData[] newBlocksData = Arrays.stream(blocksData).map(blockData -> blockData.flip(Axis.Y)).toArray(SchematicBlockData[]::new);
        SchematicEntityData[] newEntitiesData = Arrays.stream(entitiesData).map(entityData -> entityData.flip(Axis.Y)).toArray(SchematicEntityData[]::new);

        return new SchematicImpl(version, height, width, length, newBlocksData, newEntitiesData, newBlocks, airData);
    }

    @Override
    public Schematic flipZ() {
        short[] newBlocks = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int newId = y * width * length + z * width + (width - 1 - x);
            newBlocks[newId] = blocks[i];
        }

        SchematicBlockData[] newBlocksData = Arrays.stream(blocksData).map(blockData -> blockData.flip(Axis.Z)).toArray(SchematicBlockData[]::new);
        SchematicEntityData[] newEntitiesData = Arrays.stream(entitiesData).map(entityData -> entityData.flip(Axis.Z)).toArray(SchematicEntityData[]::new);

        return new SchematicImpl(version, height, width, length, newBlocksData, newEntitiesData, newBlocks, airData);
    }

    private int[] getRotationNewCoords(int width, int length, int x, int z, int quart) {
        int[] coords = new int[]{x, z, width - 1 - x, length - 1 - z};
        return new int[]{coords[quart % 4], coords[(quart + 1) % 4]};
    }
}
