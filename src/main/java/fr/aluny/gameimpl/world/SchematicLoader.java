package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import fr.aluny.gameimpl.nbt.NBTReader;
import fr.aluny.gameimpl.nbt.type.NBTCompound;
import fr.aluny.gameimpl.nbt.type.NBTList;
import fr.aluny.gameimpl.nbt.type.TagType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class SchematicLoader {

    public SchematicImpl load(InputStream inputStream) throws IOException {
        NBTCompound schematicData = NBTReader.read(inputStream);

        int version = schematicData.getInt("Version", -1);

        short width = schematicData.getShort("Width", (short) 0);
        short height = schematicData.getShort("Height", (short) 0);
        short length = schematicData.getShort("Length", (short) 0);
        byte[] blocks = schematicData.getByteArray("BlockData");
        int[] offset = schematicData.getIntArray("Offset");

        byte[] addId = new byte[]{ };
        if (schematicData.containsTag("AddBlocks", TagType.BYTE_ARRAY)) {
            addId = schematicData.getByteArray("AddBlocks");
        }
        short[] sblocks = new short[blocks.length];
        int index = 0;
        while (index < blocks.length) {
            sblocks[index] = index >> 1 >= addId.length ? (short) (blocks[index] & 255) : ((index & 1) == 0 ? (short) (((addId[index >> 1] & 15) << 8) + (blocks[index] & 255)) : (short) (((addId[index >> 1] & 240) << 4) + (blocks[index] & 255)));
            ++index;
        }

        NBTCompound palette = (NBTCompound) schematicData.get("Palette");
        int paletteSize = schematicData.getInt("PaletteMax", -1);

        int airData = palette.getInt("minecraft:air", -1);

        SchematicBlockData[] blocksData = new SchematicBlockData[paletteSize];

        for (String key : palette.getKeySet()) {
            blocksData[palette.getInt(key, -1)] = parsePaletteData(key);
        }

        NBTList entitiesList = schematicData.getList("Entities");
        int entitiesSize = entitiesList != null ? entitiesList.size() : 0;

        SchematicEntityData[] entities = new SchematicEntityData[entitiesSize];
        for (int i = 0; i < entitiesSize; i++)
            entities[i] = parseEntity(length, height, width, offset, entitiesList.getCompound(i));

        inputStream.close();

        return new SchematicImpl(version, height, width, length, blocksData, entities, sblocks, airData);
    }

    private SchematicBlockData parsePaletteData(String data) {
        return data.contains("[") ? new ParameterizedBlock(data) : new SimpleBlock(getMaterialOrThrow(data));
    }

    private SchematicEntityData parseEntity(short length, short height, short width, int[] offset, NBTCompound entityData) {
        String type = entityData.getString("Id");
        EntityType entityType = getEntityTypeOrThrow(type);

        NBTList posList = entityData.getList("Pos");
        double posX = posList.getDouble(0) - offset[0];
        double posY = posList.getDouble(1) - offset[1];
        double posZ = posList.getDouble(2) - offset[2];

        NBTList rotationList = entityData.getList("Rotation");
        float yaw = rotationList.getFloat(0);
        float pitch = rotationList.getFloat(1);

        Component customName = Optional.ofNullable(entityData.getString("CustomName")).map(GsonComponentSerializer.gson()::deserialize).orElse(null);

        return new EntityData(length, height, width, posX, posY, posZ, pitch, yaw, entityType, customName);
    }

    private Material getMaterialOrThrow(String materialName) {
        return Optional.ofNullable(Material.matchMaterial(materialName)).orElseThrow(() -> new SchematicParseException("match failed: " + materialName));
    }

    @SuppressWarnings("deprecation")
    private EntityType getEntityTypeOrThrow(String entityTypeName) {
        return Optional.ofNullable(EntityType.fromName(entityTypeName.replaceFirst("minecraft:", ""))).orElseThrow(() -> new SchematicParseException("entitytype fromName failed: " + entityTypeName));
    }

    public record SimpleBlock(Material material) implements SchematicBlockData {

        @Override
        public void paste(Location location) {
            location.getBlock().setType(material);
        }

    }

    public record ParameterizedBlock(BlockData blockData) implements SchematicBlockData {

        public ParameterizedBlock(String data) {
            this(Bukkit.createBlockData(data));
        }

        @Override
        public void paste(Location location) {
            Block block = location.getBlock();
            block.setType(blockData.getMaterial());
            block.setBlockData(blockData);
        }

        @Override
        public SchematicBlockData flip(Axis axis) {

            Mirror mirror = switch (axis) {
                case X -> Mirror.LEFT_RIGHT;
                case Y -> Mirror.NONE;
                case Z -> Mirror.FRONT_BACK;
            };

            BlockData newData = blockData.clone();
            newData.mirror(mirror);

            return new ParameterizedBlock(newData);
        }

        @Override
        public SchematicBlockData rotate(StructureRotation rotation) {

            BlockData newData = blockData.clone();
            newData.rotate(rotation);
            return new ParameterizedBlock(newData);
        }

    }

    public record EntityData(short length, short height, short width, double posX, double posY, double posZ, float pitch, float yaw, EntityType entityType, Component customName) implements SchematicEntityData {

        @Override
        public Entity summon(Location location) {
            Location summonLocation = location.getBlock().getLocation().clone().add(this.posX, this.posY, this.posZ);
            summonLocation.setPitch(this.pitch);
            summonLocation.setYaw(this.yaw);

            Entity entity = Objects.requireNonNull(location.getWorld()).spawnEntity(summonLocation, this.entityType);
            if (this.customName != null) {
                entity.setCustomNameVisible(true);
                entity.customName(this.customName);
            }

            return entity;
        }

        @Override
        public SchematicEntityData flip(Axis axis) {
            return switch (axis) {
                case X -> new EntityData(this.length, this.height, this.width, this.posX, this.posY, length - this.posZ, this.pitch, this.yaw, this.entityType, this.customName);
                case Y -> new EntityData(this.length, this.height, this.width, this.posX, height - this.posY, this.posZ, this.pitch, this.yaw, this.entityType, this.customName);
                case Z -> new EntityData(this.length, this.height, this.width, width - this.posX, this.posY, this.posZ, this.pitch, this.yaw, this.entityType, this.customName);
            };
        }

        @Override
        public SchematicEntityData rotate(StructureRotation rotation) {

            if (rotation == StructureRotation.NONE)
                return this;

            int quart = switch (rotation) {
                case CLOCKWISE_90 -> 3;
                case CLOCKWISE_180 -> 2;
                case COUNTERCLOCKWISE_90 -> 1;
                default -> throw new IllegalStateException("Unexpected value: " + rotation);
            };

            double[] coords = new double[]{this.posX, this.posZ, width - this.posX, length - this.posZ};

            double newPosX = coords[quart % 4];
            double newPosZ = coords[(quart + 1) % 4];

            return new EntityData(this.length, this.height, this.width, newPosX, this.posY, newPosZ, this.pitch, this.yaw, this.entityType, this.customName);
        }
    }

    public static class SchematicParseException extends IllegalArgumentException {

        public SchematicParseException(String reason) {
            super(reason);
        }
    }

}
