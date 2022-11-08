package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.Axis;
import fr.aluny.gameimpl.nbt.NBTReader;
import fr.aluny.gameimpl.nbt.type.NBTCompound;
import fr.aluny.gameimpl.nbt.type.NBTList;
import fr.aluny.gameimpl.nbt.type.TagType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class SchematicLoader {

    public SchematicImpl load(InputStream inputStream) throws IOException {
        NBTCompound schematicData = NBTReader.read(inputStream);

        short width = schematicData.getShort("Width", (short) 0);
        short height = schematicData.getShort("Height", (short) 0);
        short length = schematicData.getShort("Length", (short) 0);
        byte[] blocks = schematicData.getByteArray("BlockData");

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

        SchematicEntityData[] entities = new SchematicEntityData[entitiesList.size()];
        for (int i = 0; i < entitiesList.size(); i++)
            entities[i] = parseEntity(length, height, width, entitiesList.getCompound(i));

        inputStream.close();

        return new SchematicImpl(height, width, length, entities, blocksData, sblocks, airData);
    }

    private SchematicBlockData parsePaletteData(String key) {
        int paramsIndex = key.indexOf('[');

        if (paramsIndex == -1)
            return new SimpleBlock(getMaterialOrThrow(key));

        Material material = getMaterialOrThrow(key.substring(0, paramsIndex));

        String[] parameters = key.substring(paramsIndex + 1, key.length() - 1).split(",");


        Map<String, String> params = Arrays.stream(parameters).map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s -> s[1]));

        return new ParameterizedBlock(material, params);
    }

    @SuppressWarnings("deprecation")
    private SchematicEntityData parseEntity(short length, short height, short width, NBTCompound entityData) {
        String type = entityData.getString("Id");
        EntityType entityType = EntityType.fromName(type.replaceFirst("minecraft:", ""));

        NBTList posList = entityData.getList("Pos");

        double posX = posList.getDouble(0);
        double posY = posList.getDouble(1);
        double posZ = posList.getDouble(2);
        return new EntityData(length, height, width, posX, posY, posZ, entityType);
    }

    private Material getMaterialOrThrow(String materialName) {
        return Optional.ofNullable(Material.matchMaterial(materialName)).orElseThrow(() -> new SchematicParseException("match failed: " + materialName));
    }

    public static class SimpleBlock implements SchematicBlockData {

        private final Material material;

        public SimpleBlock(Material material) {
            this.material = material;
        }

        @Override
        public void paste(Location location) {
            location.getBlock().setType(material);
        }

        @Override
        public void flip(Axis axis) {
            // flip doesn't affect the block
        }

        @Override
        public void rotate(int quart) {
            // rotation doesn't affect the block
        }
    }

    public static class ParameterizedBlock implements SchematicBlockData {

        private final Material material;

        private BlockFace face;
        private Half      half;
        private Shape     stairsShape;
        private boolean   waterLogged;

        public ParameterizedBlock(Material material, Map<String, String> params) {
            this.material = material;
            this.face = getParam(params, "facing", BlockFace::valueOf);
            this.half = getParam(params, "half", Half::valueOf);
            this.stairsShape = getParam(params, "shape", Shape::valueOf);
            this.waterLogged = Boolean.TRUE.equals(getParam(params, "waterlogged", Boolean::parseBoolean));
        }

        private static <T> T getParam(Map<String, String> params, String key, Function<String, T> function) {
            String value = params.get(key);
            return value == null ? null : function.apply(value.toUpperCase(Locale.ROOT));
        }

        @Override
        public void paste(Location location) {
            Block block = location.getBlock();
            block.setType(this.material);

            BlockData blockData = block.getBlockData();

            if (this.face != null && blockData instanceof Directional directional)
                directional.setFacing(this.face);

            if (this.half != null && blockData instanceof Bisected bisected)
                bisected.setHalf(this.half);

            if (this.stairsShape != null && blockData instanceof Stairs stairs)
                stairs.setShape(this.stairsShape);

            if (this.waterLogged && blockData instanceof Waterlogged waterlogged)
                waterlogged.setWaterlogged(true);

            block.setBlockData(blockData);
        }

        @Override
        public void flip(Axis axis) {
            switch (axis) {

                case X -> {
                    if (this.face != null)
                        this.face = switch (this.face) {
                            case NORTH, SOUTH -> this.face.getOppositeFace();
                            default -> this.face;
                        };
                    if (this.stairsShape != null)
                        this.stairsShape = switch (this.stairsShape) {
                            case INNER_LEFT -> Shape.INNER_RIGHT;
                            case INNER_RIGHT -> Shape.INNER_LEFT;
                            case OUTER_LEFT -> Shape.OUTER_RIGHT;
                            case OUTER_RIGHT -> Shape.OUTER_LEFT;
                            default -> this.stairsShape;
                        };
                }
                case Y -> {
                    if (this.half != null)
                        this.half = switch (this.half) {
                            case TOP -> Half.BOTTOM;
                            case BOTTOM -> Half.TOP;
                        };
                }
                case Z -> {
                    if (this.face != null)
                        this.face = switch (this.face) {
                            case EAST, WEST -> this.face.getOppositeFace();
                            default -> this.face;
                        };
                    if (this.stairsShape != null)
                        this.stairsShape = switch (this.stairsShape) {
                            case INNER_LEFT -> Shape.INNER_RIGHT;
                            case INNER_RIGHT -> Shape.INNER_LEFT;
                            case OUTER_LEFT -> Shape.OUTER_RIGHT;
                            case OUTER_RIGHT -> Shape.OUTER_LEFT;
                            default -> this.stairsShape;
                        };
                }
            }
        }

        @Override
        public void rotate(int quart) {
            if (this.face == null)
                return;

            if (quart == 2) {
                this.face = switch (this.face) {
                    case NORTH, EAST, SOUTH, WEST -> this.face.getOppositeFace();
                    default -> this.face;
                };
                return;
            }

            if (quart == 1) {
                this.face = switch (this.face) {
                    case NORTH -> BlockFace.WEST;
                    case EAST -> BlockFace.NORTH;
                    case SOUTH -> BlockFace.EAST;
                    case WEST -> BlockFace.SOUTH;
                    default -> this.face;
                };
                return;
            }

            rotate(2);
            rotate(1);
        }

    }

    public static class EntityData implements SchematicEntityData {

        private short length, height, width;

        private double posX, posY, posZ;

        private final EntityType entityType;

        public EntityData(short length, short height, short width, double posX, double posY, double posZ, EntityType entityType) {
            this.length = length;
            this.height = height;
            this.width = width;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.entityType = entityType;
        }

        @Override
        public Entity summon(Location location) {
            return location.getWorld().spawnEntity(location.getBlock().getLocation().clone().add(this.posX, this.posY, this.posZ).getBlock().getLocation().clone().add(0.5, 0, 0.5), this.entityType);
        }

        @Override
        public void flip(Axis axis) {
            switch (axis) {
                case X -> this.posZ = length - this.posZ;
                case Y -> this.posY = height - this.posY;
                case Z -> this.posX = width - this.posX;
            }
        }

        @Override
        public void rotate(int quart) {
            if (quart > 0)
                quart = 4 - quart;

            if (quart < 0)
                quart = -quart;

            double[] coords = new double[]{this.posX, this.posZ, width - this.posX, length - this.posZ};

            this.posX = coords[quart % 4];
            this.posZ = coords[(quart + 1) % 4];
        }
    }

    public static class SchematicParseException extends IllegalArgumentException {

        public SchematicParseException(String reason) {
            super(reason);
        }
    }

}
