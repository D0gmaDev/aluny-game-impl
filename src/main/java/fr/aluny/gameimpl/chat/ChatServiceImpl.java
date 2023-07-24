package fr.aluny.gameimpl.chat;

import fr.aluny.gameapi.chat.ChatCallback;
import fr.aluny.gameapi.chat.ChatPreProcessor;
import fr.aluny.gameapi.chat.ChatProcessor;
import fr.aluny.gameapi.chat.ChatService;
import fr.aluny.gameapi.chat.ProcessedChat;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.chat.processor.ChatColorPreProcessor;
import fr.aluny.gameimpl.chat.processor.DefaultChatProcessor;
import fr.aluny.gameimpl.chat.processor.MutePreProcessor;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class ChatServiceImpl implements ChatService {

    private static final char DEFAULT_PREFIX = '§'; //This char is impossible to write in minecraft, so it is used as default

    private final Map<Character, List<ChatPreProcessor>> chatPreProcessors = new HashMap<>();
    private final Map<Character, ChatProcessor>          chatProcessors    = new HashMap<>();
    private final Map<UUID, IdentifiedCallback>          chatCallbacks     = new HashMap<>();

    private final ServiceManager serviceManager;

    public ChatServiceImpl(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void registerDefaultChatPreProcessor(ChatPreProcessor chatPreProcessor) {
        registerChatPreProcessor(DEFAULT_PREFIX, chatPreProcessor);
    }

    @Override
    public void registerChatPreProcessor(char prefix, ChatPreProcessor chatPreProcessor) {
        List<ChatPreProcessor> list = chatPreProcessors.getOrDefault(prefix, new ArrayList<>());
        list.add(chatPreProcessor);
        chatPreProcessors.put(prefix, list);
    }

    @Override
    public void setDefaultChatProcessor(ChatProcessor chatProcessor) {
        chatProcessors.put(DEFAULT_PREFIX, chatProcessor);
    }

    @Override
    public void setChatProcessor(char prefix, ChatProcessor chatProcessor) {
        chatProcessors.put(prefix, chatProcessor);
    }

    @Override
    public void unregisterAllChatPreProcessor(char prefix) {
        chatPreProcessors.remove(prefix);
    }

    @Override
    public void unregisterChatPreProcessor(char prefix, ChatPreProcessor chatPreProcessor) {
        List<ChatPreProcessor> list = chatPreProcessors.getOrDefault(prefix, new ArrayList<>());
        list.remove(chatPreProcessor);
        chatPreProcessors.put(prefix, list);
    }

    @Override
    public void unregisterDefaultChatProcessor() {
        chatProcessors.remove(DEFAULT_PREFIX);
    }

    @Override
    public void unregisterChatProcessor(char prefix) {
        chatProcessors.remove(prefix);
    }

    @Override
    public void addChatListener(UUID uuid, ChatCallback callback) {

        if (chatCallbacks.containsKey(uuid))
            chatCallbacks.get(uuid).callback().onError();

        UUID callbackId = UUID.randomUUID();
        chatCallbacks.put(uuid, new IdentifiedCallback(callbackId, callback));

        serviceManager.getRunnableHelper().runLaterAsynchronously(() -> {
            if (chatCallbacks.containsKey(uuid) && chatCallbacks.get(uuid).uuid().equals(callbackId))
                chatCallbacks.remove(uuid).callback().onError();
        }, 600);
    }

    @Override
    public void cancelChatListener(UUID uuid) {
        if (chatCallbacks.containsKey(uuid))
            chatCallbacks.remove(uuid).callback().onError();
    }

    public boolean executeChatListener(UUID uuid, String text) {
        if (!chatCallbacks.containsKey(uuid))
            return false;

        chatCallbacks.remove(uuid).callback().callback(text);
        return true;
    }

    public void acceptPreProcess(char prefix, ProcessedChat processedChat) {
        if (this.chatPreProcessors.containsKey(prefix)) {
            for (ChatPreProcessor chatPreProcessor : this.chatPreProcessors.get(prefix)) {
                chatPreProcessor.accept(processedChat);

                if (processedChat.isCancelled())
                    return;
            }
        } else if (this.chatPreProcessors.containsKey(DEFAULT_PREFIX)) {
            for (ChatPreProcessor chatPreProcessor : this.chatPreProcessors.get(DEFAULT_PREFIX)) {
                chatPreProcessor.accept(processedChat);

                if (processedChat.isCancelled())
                    return;
            }
        }
    }

    public void acceptProcess(GamePlayer sender, AsyncChatEvent event, String messageContent) {
        char prefix = messageContent.charAt(0);

        if (this.chatPreProcessors.containsKey(prefix) || this.chatProcessors.containsKey(prefix)) {
            event.message(Component.text(messageContent.substring(1)));

            if (messageContent.length() < 2) {
                event.setCancelled(true);
                return;
            }
        } else {
            prefix = DEFAULT_PREFIX;
        }

        ProcessedChat processedChat = new ProcessedChat(sender, event.viewers(), event.message());

        acceptPreProcess(prefix, processedChat);

        if (processedChat.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        if (chatProcessors.containsKey(prefix))
            chatProcessors.get(prefix).accept(processedChat);

        event.setCancelled(processedChat.isCancelled());
        event.renderer(processedChat.getRenderer());
    }

    @Override
    public void initialize() {
        registerDefaultChatPreProcessor(new MutePreProcessor(serviceManager.getModerationService()));
        registerDefaultChatPreProcessor(new ChatColorPreProcessor(serviceManager.getPlayerAccountService()));

        setDefaultChatProcessor(new DefaultChatProcessor(serviceManager.getTranslationService(), serviceManager.getPlayerAccountService()));
    }

    private record IdentifiedCallback(UUID uuid, ChatCallback callback) {

    }
}
