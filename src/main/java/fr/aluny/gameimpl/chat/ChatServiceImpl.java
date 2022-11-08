package fr.aluny.gameimpl.chat;

import fr.aluny.gameapi.chat.ChatPreProcessor;
import fr.aluny.gameapi.chat.ChatProcessor;
import fr.aluny.gameapi.chat.ChatService;
import fr.aluny.gameapi.chat.ProcessedChat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import reactor.core.publisher.MonoSink;

public class ChatServiceImpl implements ChatService {

    private static final String DEFAULT_STRING = "§"; //This String is impossible to write in minecraft, so it is used as default

    private final Map<String, List<ChatPreProcessor>> chatPreProcessors = new HashMap<>();
    private final Map<String, ChatProcessor>          chatProcessors    = new HashMap<>();
    private final Map<UUID, MonoSink<String>>         chatCallbacks     = new HashMap<>();

    @Override
    public void registerDefaultChatPreProcessor(ChatPreProcessor chatPreProcessor) {
        registerChatPreProcessor(DEFAULT_STRING, chatPreProcessor);
    }

    @Override
    public void registerChatPreProcessor(String prefix, ChatPreProcessor chatPreProcessor) {
        List<ChatPreProcessor> list = chatPreProcessors.getOrDefault(prefix, new ArrayList<>());
        list.add(chatPreProcessor);
        chatPreProcessors.put(prefix, list);
    }

    @Override
    public void setDefaultChatProcessor(ChatProcessor chatProcessor) {
        chatProcessors.put(DEFAULT_STRING, chatProcessor);
    }

    @Override
    public void setChatProcessor(String prefix, ChatProcessor chatProcessor) {
        chatProcessors.put(prefix, chatProcessor);
    }

    @Override
    public void unregisterAllChatPreProcessor(String prefix) {
        chatPreProcessors.remove(prefix);
    }

    @Override
    public void unregisterChatPreProcessor(String prefix, ChatPreProcessor chatPreProcessor) {
        List<ChatPreProcessor> list = chatPreProcessors.getOrDefault(prefix, new ArrayList<>());
        list.remove(chatPreProcessor);
        chatPreProcessors.put(prefix, list);
    }

    @Override
    public void unregisterDefaultChatProcessor() {
        chatProcessors.remove(DEFAULT_STRING);
    }

    @Override
    public void unregisterChatProcessor(String prefix) {
        chatProcessors.remove(prefix);
    }

    @Override
    public void addChatListener(UUID uuid, MonoSink<String> callback) {
        chatCallbacks.put(uuid, callback.onDispose(() -> chatCallbacks.remove(uuid)));
    }

    @Override
    public void cancelChatListener(UUID uuid) {
        if (chatCallbacks.containsKey(uuid)) {
            chatCallbacks.remove(uuid).error(new IllegalStateException("Cancelled"));
        }
    }

    public boolean executeChatListener(UUID uuid, String text) {
        if (!chatCallbacks.containsKey(uuid))
            return false;

        chatCallbacks.remove(uuid).success(text);
        return true;
    }

    public ProcessedChat acceptPreProcess(ProcessedChat processedChat) {
        String c = String.valueOf(processedChat.getGlobalMessage().charAt(0));

        if (chatPreProcessors.containsKey(c)) {
            for (ChatPreProcessor chatPreProcessor : chatPreProcessors.get(c)) {
                processedChat = chatPreProcessor.accept(processedChat);

                if (processedChat.isCancelled())
                    return null;
            }
        }

        if (chatPreProcessors.containsKey(DEFAULT_STRING)) {
            for (ChatPreProcessor chatPreProcessor : chatPreProcessors.get(DEFAULT_STRING)) {
                processedChat = chatPreProcessor.accept(processedChat);

                if (processedChat == null)
                    return null;
            }
        }

        return processedChat;
    }

    public void acceptProcess(AsyncPlayerChatEvent event) {
        ProcessedChat processedChat = new ProcessedChat(event.getPlayer(), event.getMessage());

        processedChat = this.acceptPreProcess(processedChat);

        if (processedChat == null)
            return;

        if (processedChat.isCancelled())
            return;

        String c = String.valueOf(event.getMessage().charAt(0));

        if (chatProcessors.containsKey(c))
            chatProcessors.get(c).accept(processedChat);
        else if (chatProcessors.containsKey(DEFAULT_STRING))
            chatProcessors.get(DEFAULT_STRING).accept(processedChat);
    }

    @Override
    public void initialize() {
        /*registerDefaultChatPreProcessor(new DefaultChatMutePreProcessor());
        registerDefaultChatPreProcessor(new DefaultChatColorPreProcessor());
        registerDefaultChatPreProcessor(new DefaultChatMentionPreProcessor());
        setDefaultChatProcessor(new DefaultChatProcessor("defaultChatFormat"));*/

        registerDefaultChatPreProcessor(processedChat -> {
            /*processedChat.setGlobalMessage(ChatColor.of("#996633") + processedChat.getGlobalMessage());
            return processedChat;*/
            //String s = "<c:#42f5b6>Coucou à tous <c:#f5428a>les amis !";
            Pattern pattern = Pattern.compile("<c:#([a-fA-F0-9]{6})>");
            processedChat.setGlobalMessage(pattern.matcher(processedChat.getGlobalMessage()).replaceAll(matchResult -> ChatColor.of("#" + matchResult.group(1)).toString()));
            return processedChat;
        });

        setDefaultChatProcessor(processedChat -> Bukkit.broadcastMessage(processedChat.getSender().getName() + " : " + processedChat.getGlobalMessage()));
    }
}
