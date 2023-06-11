package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.message.SetMessageHandler.BroadcastMessageHandler;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.xenondevs.inventoryaccess.component.i18n.AdventureComponentLocalizer;

public class MessageServiceImpl implements MessageService {

    private BroadcastMessageHandler broadcastMessageHandler;

    private final ServiceManager serviceManager;

    public MessageServiceImpl(ServiceManager serviceManager) {
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
        AdventureComponentLocalizer.getInstance().setComponentCreator(COMPONENT_PARSER::deserialize);
    }

    static MiniMessage getComponentParser() {
        return COMPONENT_PARSER;
    }
}
