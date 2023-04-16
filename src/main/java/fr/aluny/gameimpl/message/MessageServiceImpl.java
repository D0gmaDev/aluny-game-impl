package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.message.SetMessageHandler.BroadcastMessageHandler;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.inventoryaccess.component.i18n.AdventureComponentLocalizer;

public class MessageServiceImpl implements MessageService {

    private static BukkitAudiences audiences;

    private BroadcastMessageHandler broadcastMessageHandler;

    private final JavaPlugin     plugin;
    private final ServiceManager serviceManager;

    public MessageServiceImpl(JavaPlugin plugin, ServiceManager serviceManager) {
        this.plugin = plugin;
        this.serviceManager = serviceManager;
    }

    @Override
    public MessageHandler getPlayerMessageHandler(OfflineGamePlayer gamePlayer) {
        return gamePlayer.getMessageHandler();
    }

    @Override
    public MessageHandler getSetMessageHandler(Supplier<Collection<UUID>> receiversSupplier) {
        return new SetMessageHandler(serviceManager.getGamePlayerService(), receiversSupplier);
    }

    @Override
    public MessageHandler getBroadcastHandler() {
        if (broadcastMessageHandler != null)
            return broadcastMessageHandler;

        return broadcastMessageHandler = new BroadcastMessageHandler(serviceManager.getGamePlayerService());
    }

    @Override
    public void initialize() {
        audiences = BukkitAudiences.create(plugin);
        AdventureComponentLocalizer.getInstance().setComponentCreator(COMPONENT_PARSER::deserialize);
    }

    @Override
    public void shutdown() {
        if (audiences != null)
            audiences.close();
    }

    static BukkitAudiences getAudiences() {
        return audiences;
    }

    static MiniMessage getComponentParser() {
        return COMPONENT_PARSER;
    }
}
