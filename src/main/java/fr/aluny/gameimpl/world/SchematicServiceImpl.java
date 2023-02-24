package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.SchematicService;
import java.io.InputStream;
import org.bukkit.plugin.Plugin;

public class SchematicServiceImpl implements SchematicService {

    @Override
    public SchematicImpl loadSchematic(String name, Plugin plugin) {
        return loadSchematic(getSchematicStreamFromResources(name, plugin));
    }

    @Override
    public SchematicImpl loadSchematic(InputStream inputStream) {
        try {
            SchematicLoader schematicLoader = new SchematicLoader();
            return schematicLoader.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputStream getSchematicStreamFromResources(String name, Plugin plugin) {
        return plugin.getClass().getClassLoader().getResourceAsStream(name);
    }
}
