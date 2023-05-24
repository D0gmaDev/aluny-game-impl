package fr.aluny.gameimpl.moderation.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.TabCompleter;
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
import net.md_5.bungee.api.ChatColor;
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

                SimpleItem validationItem = new SimpleItem(new ItemBuilder(Material.LIME_CANDLE).setDisplayName("§aValider"), click -> {
                    click.getPlayer().closeInventory();
                    playerSanctionAPI.applySanction(playerAccount, player, SanctionType.MUTE, duration, String.join(" ", args)).ifPresent(sanction ->
                            player.getMessageHandler().sendComponentMessage("moderation_successfully_muted", Placeholder.unparsed("name", playerAccount.getName()), Placeholder.unparsed("id", String.valueOf(sanction.getId()))));

                    serviceManager.getProxyMessagingService().sendMessage(player.getPlayer(), playerAccount.getName(), getMuteMessage(playerAccount.getLocale(), duration));
                });

                ChatColor color = ChatColor.of("#4e6654");

                SimpleItem infoItem = new SimpleItem(new ItemBuilder(Material.PAPER).setDisplayName(ChatColor.of("#70cc89") + "Rendre muet " + playerAccount.getName())
                        .addLoreLines(color + "Durée: §7" + TimeUtils.format(duration), color + "Raison: §7" + String.join(" ", args)));

                SimpleItem cancelItem = new SimpleItem(new ItemBuilder(Material.RED_CANDLE).setDisplayName("§cAnnuler"), click -> click.getPlayer().closeInventory());

                Gui gui = Gui.normal().setStructure("v.i.a").addIngredient('v', validationItem).addIngredient('i', infoItem).addIngredient('a', cancelItem).build();

                Window.single().setGui(gui).setTitle(color + "Confirmation du mute").open(player.getPlayer());

            }, () -> player.getMessageHandler().sendMessage("command_validation_invalid_duration", durationString));

        }, () -> player.getMessageHandler().sendMessage("command_validation_player_not_found", name));
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private Component getMuteMessage(Locale locale, TemporalAmount duration) {
        return MessageServiceImpl.COMPONENT_PARSER.deserialize(locale.translate("moderation_mute_message"), Placeholder.unparsed("duration", TimeUtils.format(duration)));
    }

}
