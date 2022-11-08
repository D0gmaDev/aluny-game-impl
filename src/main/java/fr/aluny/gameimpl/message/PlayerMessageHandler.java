package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.ClickableMessageBuilder;
import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.translation.Locale;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class PlayerMessageHandler implements MessageHandler {

    private final Player player;
    private final Locale locale;

    public PlayerMessageHandler(Player player, Locale locale) {
        this.player = player;
        this.locale = locale;
    }

    @Override
    public void sendMessage(String key, String... arguments) {
        player.sendMessage(locale.translate(key, (Object[]) arguments));
    }

    @Override
    public void sendTitle(String titleKey, String[] titleArgs, String messageKey, String[] messageArgs, int fadeIn, int duration, int fadeOut) {
        player.sendTitle(locale.translate(titleKey, (Object[]) titleArgs), locale.translate(messageKey, (Object[]) messageArgs), fadeIn, duration, fadeOut);
    }

    @Override
    public void sendActionBar(String titleKey, String... arguments) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(locale.translate(titleKey, (Object[]) arguments)));
    }

    @Override
    public void sendClickableMessage(Consumer<ClickableMessageBuilder> builder) {
        ClickableMessageBuilderImpl messageBuilder = new ClickableMessageBuilderImpl();
        builder.accept(messageBuilder);
        player.spigot().sendMessage(messageBuilder.build(locale));
    }
}
