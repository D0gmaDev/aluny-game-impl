package fr.aluny.gameimpl.item;

import fr.aluny.gameapi.item.InteractAction;
import fr.aluny.gameapi.item.ItemService;
import fr.aluny.gameapi.service.ServiceManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemServiceImpl implements ItemService, Listener {

    private final ServiceManager serviceManager;

    private final JavaPlugin    plugin;
    private final NamespacedKey key;

    private final Map<String, InteractAction> actions = new HashMap<>();

    public ItemServiceImpl(ServiceManager serviceManager, JavaPlugin plugin) {
        this.serviceManager = serviceManager;
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "click_action");
    }

    @Override
    public void initialize() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void registerInteractAction(String key, InteractAction action) {
        this.actions.put(key, action);
    }

    @Override
    public void unregisterInteractAction(String key) {
        this.actions.remove(key);
    }

    @Override
    public ItemStack actionItem(ItemStack itemStack, String key) {
        itemStack.editMeta(itemMeta -> itemMeta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, key));
        return itemStack;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Optional.ofNullable(event.getItem())
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getPersistentDataContainer)
                .map(pdc -> pdc.get(this.key, PersistentDataType.STRING))
                .map(this.actions::get)
                .ifPresent(interactAction -> interactAction.accept(this.serviceManager.getGamePlayerService().getPlayer(event.getPlayer()), event.getAction(), event.getItem()));
    }
}
