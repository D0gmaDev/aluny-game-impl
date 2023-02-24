package fr.aluny.gameimpl.chat;

import fr.aluny.gameimpl.player.GamePlayerServiceImpl;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerChatListener implements Listener {

    private static final Set<String> FORBIDDEN_COMMANDS = Set.of("version", "ver", "icanhasbukkit", "help", "me", "?");
    private static final Pattern     COMMAND_PREFIX     = Pattern.compile("^/((minecraft)|(bukkit)|(spigot)):");

    private final ChatServiceImpl       chatService;
    private final GamePlayerServiceImpl gamePlayerService;

    public PlayerChatListener(ChatServiceImpl chatService, GamePlayerServiceImpl gamePlayerService) {
        this.chatService = chatService;
        this.gamePlayerService = gamePlayerService;
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
