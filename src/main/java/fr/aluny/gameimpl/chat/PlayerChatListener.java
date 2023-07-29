package fr.aluny.gameimpl.chat;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameimpl.player.GamePlayerServiceImpl;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Set;
import java.util.regex.Pattern;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerChatListener implements Listener {

    private static final Set<String> FORBIDDEN_COMMANDS = Set.of("version", "ver", "icanhasbukkit", "help", "me", "?");
    private static final Pattern     COMMAND_PREFIX     = Pattern.compile("^((minecraft)|(bukkit)|(spigot)):");

    private final ChatServiceImpl       chatService;
    private final GamePlayerServiceImpl gamePlayerService;

    public PlayerChatListener(ChatServiceImpl chatService, GamePlayerServiceImpl gamePlayerService) {
        this.chatService = chatService;
        this.gamePlayerService = gamePlayerService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled())
            return;

        String messageContent = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (chatService.executeChatListener(event.getPlayer().getUniqueId(), messageContent))
            return;

        GamePlayer sender = gamePlayerService.getPlayer(event.getPlayer());

        chatService.acceptProcess(sender, event, messageContent);
    }

    private boolean isBlocked(String message) {
        message = message.substring(1); // remove '/'

        String command = COMMAND_PREFIX.matcher(message).replaceFirst("")
                .split(" ")[0].toLowerCase();

        return FORBIDDEN_COMMANDS.contains(command);
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (isBlocked(event.getMessage())) {
            event.setCancelled(true);
            gamePlayerService.getPlayer(event.getPlayer()).getMessageHandler().sendMessage("command_validation_no_permission");
        }
    }
}
