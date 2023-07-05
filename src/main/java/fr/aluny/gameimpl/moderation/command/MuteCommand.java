package fr.aluny.gameimpl.moderation.command;

import static fr.aluny.gameapi.utils.ComponentUtils.argument;
import static fr.aluny.gameapi.utils.ComponentUtils.id;
import static fr.aluny.gameapi.utils.ComponentUtils.name;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.TabCompleter;
import fr.aluny.gameapi.inventory.Translation;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.utils.GameUtils;
import fr.aluny.gameapi.utils.TimeUtils;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.message.MessageServiceImpl;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.temporal.TemporalAmount;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

@CommandInfo(name = "mute", permission = "fr.aluny.command.mute")
public class MuteCommand extends Command {

    private final PlayerSanctionAPI playerSanctionAPI;
    private final ServiceManager    serviceManager;

    public MuteCommand(PlayerSanctionAPI playerSanctionAPI, ServiceManager serviceManager) {
        this.playerSanctionAPI = playerSanctionAPI;
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, String name, String durationString, String[] args) {

        serviceManager.getPlayerAccountService().getPlayerAccountByName(name).ifPresentOrElse(playerAccount -> {

            TimeUtils.parsePositiveTemporalAmount(durationString).ifPresentOrElse(duration -> {

                if (args.length == 0) {
                    player.getMessageHandler().sendMessage("moderation_provide_reason");
                    return;
                }

                SimpleItem validationItem = new SimpleItem(new ItemBuilder(Material.LIME_CANDLE).setDisplayName(new Translation("sanction_validate")), click -> {
                    click.getPlayer().closeInventory();
                    playerSanctionAPI.applySanction(playerAccount, player, SanctionType.MUTE, duration, String.join(" ", args)).ifPresent(sanction ->
                            player.getMessageHandler().sendMessage("moderation_successfully_muted", name(playerAccount), id(sanction.getId())));

                    serviceManager.getProxyMessagingService().sendMessage(player.getPlayer(), playerAccount.getName(), getMuteMessage(playerAccount.getLocale(), duration));
                });

                SimpleItem infoItem = new SimpleItem(new ItemBuilder(Material.PAPER).setDisplayName(new Translation("sanction_mute_player", name(playerAccount)))
                        .addLoreLines(new Translation("sanction_duration", Placeholder.unparsed("duration", TimeUtils.format(duration))), new Translation("sanction_reason", Placeholder.unparsed("reason", String.join(" ", args)))));

                SimpleItem cancelItem = new SimpleItem(new ItemBuilder(Material.RED_CANDLE).setDisplayName(new Translation("sanction_cancel")), click -> click.getPlayer().closeInventory());

                Gui gui = Gui.normal().setStructure("v.i.a").addIngredient('v', validationItem).addIngredient('i', infoItem).addIngredient('a', cancelItem).build();

                Window.single().setGui(gui).setTitle(new Translation("sanction_mute_title")).open(player.getPlayer());

            }, () -> player.getMessageHandler().sendMessage("command_validation_invalid_duration", argument(durationString)));

        }, () -> player.getMessageHandler().sendMessage("command_validation_player_not_found", argument(name)));
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private Component getMuteMessage(Locale locale, TemporalAmount duration) {
        return MessageServiceImpl.COMPONENT_PARSER.deserialize(locale.translate("moderation_mute_message"), Placeholder.unparsed("duration", TimeUtils.format(duration)));
    }

}
