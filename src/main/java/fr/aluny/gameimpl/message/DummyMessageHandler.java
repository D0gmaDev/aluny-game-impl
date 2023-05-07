package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class DummyMessageHandler implements MessageHandler {

    @Override
    public void sendMessage(String key, String... arguments) {

    }

    @Override
    public void sendComponentMessage(String key, TagResolver... arguments) {

    }

    @Override
    public void sendTitle(String titleKey, List<TagResolver> titleArgs, String messageKey, List<TagResolver> messageArgs, int fadeIn, int duration, int fadeOut) {

    }

    @Override
    public void sendActionBar(String key, TagResolver... arguments) {

    }
}
