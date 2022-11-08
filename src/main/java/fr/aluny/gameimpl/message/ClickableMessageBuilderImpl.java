package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.ClickableMessageBuilder;
import fr.aluny.gameapi.translation.Locale;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ClickableMessageBuilderImpl implements ClickableMessageBuilder {

    private final List<ClickablePart> messageParts = new ArrayList<>();

    public BaseComponent build(Locale locale) {
        BaseComponent baseComponent = new TextComponent("");

        for (ClickablePart messagePart : this.messageParts) {

            String translatedMessage = locale.translate(messagePart.key, (Object[]) messagePart.args);
            BaseComponent part = new TextComponent(messagePart.color != null ? org.bukkit.ChatColor.stripColor(translatedMessage) : translatedMessage);

            if (messagePart.color != null)
                part.setColor(messagePart.color);

            if (messagePart.clickAction != null)
                part.setClickEvent(new ClickEvent(messagePart.clickAction, messagePart.clickValue));

            if (messagePart.hoverKey != null)
                part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(locale.translate(messagePart.hoverKey, (Object[]) messagePart.hoverArgs))));

            baseComponent.addExtra(part);
        }

        return baseComponent;
    }

    @Override
    public ClickableMessageBuilder addPart(String key, String... args) {
        this.messageParts.add(new ClickablePart(key, args));
        return this;
    }

    @Override
    public ClickableMessageBuilder withColor(ChatColor color) {
        this.messageParts.get(this.messageParts.size() - 1).color = color;
        return this;
    }

    @Override
    public ClickableMessageBuilder withClickAction(Action action, String value) {
        this.messageParts.get(this.messageParts.size() - 1).clickAction = action;
        this.messageParts.get(this.messageParts.size() - 1).clickValue = value;
        return this;
    }

    @Override
    public ClickableMessageBuilder withHoverText(String key, String... args) {
        this.messageParts.get(this.messageParts.size() - 1).hoverKey = key;
        this.messageParts.get(this.messageParts.size() - 1).hoverArgs = args;
        return this;
    }

    private static final class ClickablePart {

        private final String   key;
        private final String[] args;

        private ChatColor color;
        private Action    clickAction;
        private String    clickValue;
        private String    hoverKey;
        private String[]  hoverArgs;

        private ClickablePart(String key, String[] args) {
            this.key = key;
            this.args = args;
        }
    }
}
