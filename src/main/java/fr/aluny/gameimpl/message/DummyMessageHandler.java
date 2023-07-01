package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import java.time.Duration;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class DummyMessageHandler implements MessageHandler {

    @Override
    public void sendMessage(String key, String... arguments) {

    }

    @Override
    public void sendComponentMessage(String key, TagResolver... arguments) {

    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, Duration fadeIn, Duration stay, Duration fadeOut) {

    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void sendActionBar(String key, TagResolver... arguments) {

    }

    @Override
    public void showBossBar(String titleKey, TagResolver arguments, BossBar.Color color, BossBar.Overlay overlay, Duration duration) {

    }
}
