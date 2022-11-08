package fr.aluny.gameimpl.world;

import fr.aluny.gameapi.world.LootModifierService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class LootModifierListener implements Listener {

    private final JavaPlugin          plugin;
    private final LootModifierService lootModifierService;

    public LootModifierListener(JavaPlugin plugin, LootModifierService lootModifierService) {
        this.plugin = plugin;
        this.lootModifierService = lootModifierService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        event.getBlockPlaced().setMetadata("dropped", new FixedMetadataValue(plugin, "true"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        lootModifierService.applyLootModifiers(event);
    }

}
