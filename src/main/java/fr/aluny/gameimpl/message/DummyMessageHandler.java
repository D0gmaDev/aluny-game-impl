package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.ClickableMessageBuilder;
import fr.aluny.gameapi.message.MessageHandler;
import java.util.function.Consumer;

public class DummyMessageHandler implements MessageHandler {

    @Override
    public void sendMessage(String key, String... arguments) {
        //DO NOTHING
    }

    @Override
    public void sendTitle(String titleKey, String[] titleArgs, String messageKey, String[] messageArgs, int fadeIn, int duration, int fadeOut) {
        //DO NOTHING
    }

    @Override
    public void sendActionBar(String titleKey, String... arguments) {
        //DO NOTHING
    }

    @Override
    public void sendClickableMessage(Consumer<ClickableMessageBuilder> builder) {
        //DO NOTHING
    }
}
