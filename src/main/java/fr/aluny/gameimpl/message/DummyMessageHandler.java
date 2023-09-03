package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import java.time.Duration;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class DummyMessageHandler implements MessageHandler {

    @Override
    public void sendMessage(String key, TagResolver... arguments) {
        // dummy does nothing
    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, Duration fadeIn, Duration stay, Duration fadeOut) {
        // dummy does nothing
    }

    @Override
    public void sendActionBar(String key, TagResolver... arguments) {
        // dummy does nothing
    }

    @Override
    public void showBossBar(String titleKey, TagResolver arguments, BossBar.Color color, BossBar.Overlay overlay, Duration duration) {
        // dummy does nothing
    }
}
