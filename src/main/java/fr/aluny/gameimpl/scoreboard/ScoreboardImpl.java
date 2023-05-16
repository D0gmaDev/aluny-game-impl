package fr.aluny.gameimpl.scoreboard;

import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.scoreboard.Scoreboard;
import fr.aluny.gameapi.timer.Timer;
import fr.aluny.gameapi.translation.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import me.catcoder.sidebar.ProtocolSidebar;
import me.catcoder.sidebar.Sidebar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardImpl implements Scoreboard {

    private final GamePlayerService gamePlayerService;

    private final Locale             locale;
    private final Sidebar<Component> componentSidebar;

    public ScoreboardImpl(JavaPlugin plugin, GamePlayerService gamePlayerService, Locale locale, Component title) {
        this.gamePlayerService = gamePlayerService;
        this.locale = locale;
        this.componentSidebar = ProtocolSidebar.newAdventureSidebar(title, plugin);
    }

    @Override
    public Scoreboard addRawTextLine(String text) {
        this.componentSidebar.addTextLine(text);
        return this;
    }

    @Override
    public Scoreboard addLine(Component component) {
        this.componentSidebar.addLine(component);
        return this;
    }

    @Override
    public Scoreboard addLine(String key, TagResolver... arguments) {
        return addLine(component(key, TagResolver.resolver(arguments)));
    }

    @Override
    public Scoreboard addBlankLine() {
        this.componentSidebar.addBlankLine();
        return this;
    }

    @Override
    public Scoreboard addUpdatableLine(Supplier<Component> line) {
        this.componentSidebar.addUpdatableLine(player -> line.get());
        return this;
    }

    @Override
    public Scoreboard addUpdatableLine(String key, Supplier<TagResolver> arguments) {
        return addUpdatableLine(() -> component(key, arguments.get()));
    }

    @Override
    public Scoreboard addUpdatableLine(Function<GamePlayer, Component> line) {
        this.componentSidebar.addUpdatableLine(player -> line.apply(gamePlayerService.getPlayer(player)));
        return this;
    }

    @Override
    public Scoreboard addUpdatableLine(String key, Function<GamePlayer, TagResolver> arguments) {
        return addUpdatableLine(gamePlayer -> component(key, arguments.apply(gamePlayer)));
    }

    @Override
    public Scoreboard addConditionalLine(Component component, BooleanSupplier condition) {
        this.componentSidebar.addConditionalLine(player -> component, player -> condition.getAsBoolean());
        return this;
    }

    @Override
    public Scoreboard addConditionalLine(String key, Supplier<TagResolver> arguments, BooleanSupplier condition) {
        return addConditionalLine(component(key, arguments.get()), condition);
    }

    @Override
    public Scoreboard addConditionalLine(Function<GamePlayer, Component> line, Predicate<GamePlayer> condition) {
        this.componentSidebar.addConditionalLine(
                player -> line.apply(gamePlayerService.getPlayer(player)),
                player -> condition.test(gamePlayerService.getPlayer(player)));
        return this;
    }

    @Override
    public Scoreboard addConditionalLine(String key, Function<GamePlayer, TagResolver> arguments, Predicate<GamePlayer> condition) {
        return addConditionalLine(gamePlayer -> component(key, arguments.apply(gamePlayer)), condition);
    }

    @Override
    public Scoreboard addIncreasingTimerLine(String key, Timer timer) {
        return addUpdatableLine(key, () -> Placeholder.unparsed("timer", timer.getIncreasingFormattedValue()));
    }

    @Override
    public Scoreboard addDecreasingTimerLine(String key, Timer timer, String endArgument) {
        return addUpdatableLine(key, () -> Placeholder.unparsed("timer", timer.isEnded() ? endArgument : timer.getDecreasingFormattedValue()));
    }

    @Override
    public void addViewer(GamePlayer gamePlayer) {
        this.componentSidebar.addViewer(gamePlayer.getPlayer());
    }

    @Override
    public void removeViewer(GamePlayer gamePlayer) {
        this.componentSidebar.removeViewer(gamePlayer.getPlayer());
    }

    @Override
    public void updateLinesPeriodically(long delay, long period) {
        this.componentSidebar.updateLinesPeriodically(delay, period);
    }

    @Override
    public void destroy() {
        this.componentSidebar.destroy();
    }

    public Sidebar<Component> getComponentSidebar() {
        return componentSidebar;
    }

    private Component component(String key, TagResolver arguments) {
        return MessageService.COMPONENT_PARSER.deserialize(this.locale.translate(key), arguments);
    }

}
