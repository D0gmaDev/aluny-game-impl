package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.SchematicService;
import java.io.InputStream;
import org.bukkit.plugin.Plugin;

public class SchematicServiceImpl implements SchematicService {

    @Override
    public SchematicImpl loadSchematic(String name, Plugin plugin) {
        return loadSchematic(getSchematicFromRessources(name, plugin));
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
    public InputStream getSchematicFromRessources(String name, Plugin plugin) {
        return plugin.getClass().getClassLoader().getResourceAsStream(name);
    }
}
