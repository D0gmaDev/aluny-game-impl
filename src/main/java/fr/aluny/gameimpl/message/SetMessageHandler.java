package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.player.OfflineGamePlayer;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SetMessageHandler implements MessageHandler {

    private final GamePlayerService          gamePlayerService;
    private final Supplier<Collection<UUID>> receivers;

    public SetMessageHandler(GamePlayerService gamePlayerService, Supplier<Collection<UUID>> receivers) {
        this.gamePlayerService = gamePlayerService;
        this.receivers = receivers;
    }

    private Stream<MessageHandler> fetchReceivers() {
        return receivers.get().stream().map(gamePlayerService::getPlayer).filter(Optional::isPresent).map(Optional::orElseThrow).map(OfflineGamePlayer::getMessageHandler);
    }

    @Override
    public void sendMessage(String key, String... arguments) {
        fetchReceivers().forEach(messageHandler -> messageHandler.sendMessage(key, arguments));
    }

    @Override
    public void sendComponentMessage(String key, TagResolver... arguments) {
        fetchReceivers().forEach(messageHandler -> messageHandler.sendComponentMessage(key, arguments));
    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, Duration fadeIn, Duration stay, Duration fadeOut) {
        fetchReceivers().forEach(messageHandler -> messageHandler.sendTitle(titleKey, titleArgs, messageKey, messageArgs, fadeIn, stay, fadeOut));
    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, int fadeIn, int stay, int fadeOut) {
        fetchReceivers().forEach(messageHandler -> messageHandler.sendTitle(titleKey, titleArgs, messageKey, messageArgs, fadeIn, stay, fadeOut));
    }

    @Override
    public void sendActionBar(String key, TagResolver... arguments) {
        fetchReceivers().forEach(messageHandler -> messageHandler.sendActionBar(key, arguments));
    }

    @Override
    public void showBossBar(String titleKey, TagResolver arguments, BossBar.Color color, BossBar.Overlay overlay, Duration duration) {
        fetchReceivers().forEach(messageHandler -> messageHandler.showBossBar(titleKey, arguments, color, overlay, duration));
    }

    public static class BroadcastMessageHandler extends SetMessageHandler {

        public BroadcastMessageHandler(GamePlayerService gamePlayerService) {
            super(gamePlayerService, () -> Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList()));
        }
    }
}
