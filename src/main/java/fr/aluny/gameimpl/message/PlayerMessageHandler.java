package fr.aluny.gameimpl.message;

import fr.aluny.gameapi.message.MessageHandler;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameimpl.GameImpl;
import java.time.Duration;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerMessageHandler implements MessageHandler {

    private final Player player;
    private final Locale locale;

    public PlayerMessageHandler(Player player, Locale locale) {
        this.player = player;
        this.locale = locale;
    }

    @Override
    public void sendMessage(String key, TagResolver... arguments) {
        Component component = locale.translateComponent(key, arguments);
        player.sendMessage(component);
    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, Duration fadeIn, Duration stay, Duration fadeOut) {

        Component title = titleKey != null ? (titleArgs != null ? locale.translateComponent(titleKey, titleArgs) : locale.translateComponent(titleKey)) : Component.empty();
        Component message = messageKey != null ? (messageArgs != null ? locale.translateComponent(messageKey, messageArgs) : locale.translateComponent(messageKey)) : Component.empty();

        Times times = Times.times(fadeIn, stay, fadeOut);

        player.showTitle(Title.title(title, message, times));
    }

    @Override
    public void sendTitle(String titleKey, TagResolver titleArgs, String messageKey, TagResolver messageArgs, int fadeIn, int stay, int fadeOut) {
        sendTitle(titleKey, titleArgs, messageKey, messageArgs, Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeIn));
    }

    @Override
    public void sendActionBar(String key, TagResolver... arguments) {
        Component component = locale.translateComponent(key, arguments);
        player.sendActionBar(component);
    }

    @Override
    public void showBossBar(String titleKey, TagResolver arguments, Color color, Overlay overlay, Duration duration) {
        Component component = arguments != null ? locale.translateComponent(titleKey, arguments) : locale.translateComponent(titleKey);

        BossBar bossBar = BossBar.bossBar(component, 1f, color, overlay);
        player.showBossBar(bossBar);

        if (duration != null && !duration.isZero() && !duration.isNegative()) {

            Bukkit.getScheduler().runTaskTimerAsynchronously(GameImpl.getPlugin(), bukkitTask -> {

                bossBar.progress(Math.max(0f, bossBar.progress() - 0.05f));

                if (bossBar.progress() <= 0.025f) {
                    bukkitTask.cancel();
                    player.hideBossBar(bossBar);
                }

            }, 0, duration.toSeconds());
        }
    }
}
