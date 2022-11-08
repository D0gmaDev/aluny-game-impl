package fr.aluny.gameimpl.chat;

import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerChatListener implements Listener {

    private static final Set<String> FORBIDDEN_COMMANDS = Set.of("version", "ver", "icanhasbukkit", "help", "me", "?");

    private final ChatServiceImpl chatService;

    public PlayerChatListener(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        event.setCancelled(true);

        if (chatService.executeChatListener(event.getPlayer().getUniqueId(), event.getMessage()))
            return;

        chatService.acceptProcess(event);
    }

    private boolean isBlocked(String message) {

        String command = message.substring(1)
                .replaceFirst("^((minecraft)|(bukkit)|(spigot)):", "")
                .split(" ")[0].toLowerCase();

        return FORBIDDEN_COMMANDS.contains(command);
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (isBlocked(event.getMessage())) {
            event.setCancelled(true);
            //TODO send no permission message
        }
    }

    //Note that due to client changes, if the sender is a Player, this event will only begin to fire once command arguments
    // are specified, not commands themselves. Plugins wishing to remove commands from tab completion are advised to ensure
    // the client does not have permission for the relevant commands, or use PlayerCommandSendEvent.
}
