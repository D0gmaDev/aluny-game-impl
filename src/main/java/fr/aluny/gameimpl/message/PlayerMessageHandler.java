package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.translation.Locale;
import java.time.Duration;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
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

        if (arguments.length == 0)
            sendComponentMessage(key);
        else if (arguments.length == 1) {
            sendComponentMessage(key, Placeholder.unparsed("s", arguments[0]));
        } else {

            TagResolver[] resolvers = new TagResolver[arguments.length];

            for (int i = 0; i < arguments.length; i++) {
                resolvers[i] = Placeholder.unparsed("s" + (i + 1), arguments[i]);
            }

            sendComponentMessage(key, resolvers);
        }

    }

    @Override
    public void sendComponentMessage(String key, TagResolver... arguments) {
        Component component = MessageServiceImpl.getComponentParser().deserialize(locale.translate(key), arguments);
        MessageServiceImpl.getAudiences().player(player).sendMessage(component);
    }

    @Override
    public void sendTitle(String titleKey, List<TagResolver> titleArgs, String messageKey, List<TagResolver> messageArgs, int fadeIn, int duration, int fadeOut) {
        Component title = titleKey != null ? MessageServiceImpl.getComponentParser().deserialize(locale.translate(titleKey), titleArgs.toArray(TagResolver[]::new)) : Component.empty();
        Component message = messageKey != null ? MessageServiceImpl.getComponentParser().deserialize(locale.translate(messageKey), messageArgs.toArray(TagResolver[]::new)) : Component.empty();

        Times times = Times.times(Duration.ofSeconds(fadeIn), Duration.ofSeconds(duration), Duration.ofSeconds(fadeOut));

        MessageServiceImpl.getAudiences().player(player).showTitle(Title.title(title, message, times));
    }

    @Override
    public void sendActionBar(String key, TagResolver... arguments) {
        Component component = MessageServiceImpl.getComponentParser().deserialize(locale.translate(key), arguments);
        MessageServiceImpl.getAudiences().player(player).sendActionBar(component);
    }
}
