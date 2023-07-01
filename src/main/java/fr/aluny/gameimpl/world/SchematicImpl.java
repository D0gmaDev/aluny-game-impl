package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import fr.aluny.gameapi.world.Schematic;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class SchematicImpl implements Schematic {

    private final int version;

    private final short height;
    private       short width;
    private       short length;

    private final SchematicEntityData[] entitiesData;
    private final SchematicBlockData[]  blocksData;

    private       short[] blocks;
    private final int     airData;

    private boolean pasteAir = false;

    public SchematicImpl(int version, short height, short width, short length, SchematicEntityData[] entitiesData, SchematicBlockData[] blocksData, short[] blocks, int airData) {
        this.version = version;
        this.height = height;
        this.width = width;
        this.length = length;
        this.entitiesData = entitiesData;
        this.blocksData = blocksData;
        this.blocks = blocks;
        this.airData = airData;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public void paste(Location location) {
        paste(location, null, true, null);
    }

    @Override
    public void paste(Location location, Consumer<Block> blockConsumer) {
        paste(location, blockConsumer, true, null);
    }

    @Override
    public void paste(Location location, Consumer<Block> blockConsumer, boolean withEntities) {
        paste(location, blockConsumer, withEntities, null);
    }

    @Override
    public void paste(Location location, Consumer<Block> blockConsumer, boolean withEntities, Consumer<Entity> entityConsumer) {
        World world = location.getWorld();

        int widthTimesLength = this.width * this.length;

        int x = 0;
        while (x < this.width) {
            int y = 0;
            while (y < this.height) {
                int z = 0;
                while (z < this.length) {
                    int index = y * widthTimesLength + z * this.width + x;
                    short blockId = this.blocks[index];

                    if ((pasteAir || blockId != this.airData)) {
                        Location loc = new Location(world, (double) x + location.getX(), (double) y + location.getY(), (double) z + location.getZ());
                        this.blocksData[blockId].paste(loc);
                        if (blockConsumer != null)
                            blockConsumer.accept(loc.getBlock());
                    }
                    ++z;
                }
                ++y;
            }
            ++x;
        }

        if (withEntities) {
            for (SchematicEntityData entityData : this.entitiesData) {
                Entity entity = entityData.summon(location);

                if (entityConsumer != null)
                    entityConsumer.accept(entity);
            }
        }
    }

    @Override
    public Schematic rotateBlocks(int quart) {
        if (quart > 4)
            return rotateBlocks(4 - quart);

        if (quart > 0)
            quart = 4 - quart;

        if (quart < 0)
            quart = -quart;

        short[] result = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * this.length);
            int z = (i - y * width * this.length) / width;
            int x = i - y * width * this.length - z * width;
            int[] newCoords = getRotationNewCoords(this.width, this.length, x, z, quart);
            int newId = y * width * length + newCoords[1] * (quart % 2 == 0 ? this.width : this.length) + newCoords[0];
            result[newId] = this.blocks[i];
        }

        if (quart % 2 == 1) {
            short tempWidth = this.width;
            this.width = this.length;
            this.length = tempWidth;
        }

        this.blocks = result;

        for (SchematicBlockData blockData : this.blocksData) {
            blockData.rotate(quart);
        }

        for (SchematicEntityData entityData : this.entitiesData) {
            entityData.rotate(quart);
        }

        return this;
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
        short[] result = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int newId = y * width * length + (length - 1 - z) * width + x;
            result[newId] = blocks[i];
        }

        this.blocks = result;

        for (SchematicBlockData blockData : this.blocksData) {
            blockData.flip(Axis.X);
        }

        for (SchematicEntityData entityData : this.entitiesData) {
            entityData.flip(Axis.X);
        }

        return this;
    }

    @Override
    public Schematic flipY() {
        short[] result = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int height = blocks.length / (width * length);
            int newId = (height - 1 - y) * width * length + z * width + x;
            result[newId] = blocks[i];
        }

        this.blocks = result;

        for (SchematicBlockData blockData : this.blocksData) {
            blockData.flip(Axis.Y);
        }

        for (SchematicEntityData entityData : this.entitiesData) {
            entityData.flip(Axis.Y);
        }

        return this;
    }

    @Override
    public Schematic flipZ() {
        short[] result = new short[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int y = i / (width * length);
            int z = (i - y * width * length) / width;
            int x = i - y * width * length - z * width;
            int newId = y * width * length + z * width + (width - 1 - x);
            result[newId] = blocks[i];
        }

        this.blocks = result;

        for (SchematicBlockData blockData : this.blocksData) {
            blockData.flip(Axis.Z);
        }

        for (SchematicEntityData entityData : this.entitiesData) {
            entityData.flip(Axis.Z);
        }

        return this;
    }

    @Override
    public short getWidth() {
        return this.width;
    }

    @Override
    public short getLength() {
        return this.length;
    }

    @Override
    public short getHeight() {
        return this.height;
    }

    private int[] getRotationNewCoords(int width, int length, int x, int z, int quart) {
        int[] coords = new int[]{x, z, width - 1 - x, length - 1 - z};
        return new int[]{coords[quart % 4], coords[(quart + 1) % 4]};
    }

    @Override
    public void setPasteAir(boolean pasteAir) {
        this.pasteAir = pasteAir;
    }

    @Override
    public boolean isPasteAir() {
        return this.pasteAir;
    }
}
