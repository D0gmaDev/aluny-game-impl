package fr.aluny.gameimpl.moderation.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.TabCompleter;
import fr.aluny.gameapi.inventory.Translation;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.utils.GameUtils;
import fr.aluny.gameimpl.api.PlayerAPI;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.moderation.sanction.DetailedPlayerSanction;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

@CommandInfo(name = "unban", permission = "fr.aluny.command.unban", asyncCall = true)
public class UnbanCommand extends Command {

    private final PlayerSanctionAPI playerSanctionAPI;
    private final PlayerAPI         playerAPI;
    private final ServiceManager    serviceManager;

    public UnbanCommand(PlayerSanctionAPI playerSanctionAPI, PlayerAPI playerAPI, ServiceManager serviceManager) {
        this.playerSanctionAPI = playerSanctionAPI;
        this.playerAPI = playerAPI;
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, String targetName, String[] args) {

        serviceManager.getPlayerAccountService().getPlayerAccountByName(targetName)
                .flatMap(playerAccount -> playerAPI.getDetailedPlayer(playerAccount.getUuid()))
                .ifPresentOrElse(playerAccount -> playerAccount.getCurrentSanctions().stream()
                        .filter(playerSanction -> playerSanction.isType(SanctionType.BAN) && !playerSanction.isCanceled() && playerSanction.isActive()).findAny()
                        .flatMap(sanction -> playerSanctionAPI.getPlayerSanctionById(sanction.getId()))
                        .ifPresentOrElse(sanction -> {

                            SimpleItem validationItem = new SimpleItem(new ItemBuilder(Material.LIME_CANDLE).setDisplayName(new Translation("sanction_validate")), click -> {
                                click.getPlayer().closeInventory();

                                playerSanctionAPI.cancelSanction(sanction.getId()).filter(DetailedPlayerSanction::isCanceled).ifPresentOrElse(
                                        updatedSanction -> player.getMessageHandler().sendComponentMessage("moderation_sanction_canceled", Placeholder.unparsed("id", String.valueOf(updatedSanction.getId())), name(playerAccount)),
                                        () -> player.getMessageHandler().sendComponentMessage("unexpected_error"));
                            });

                            SimpleItem infoItem = new SimpleItem(new ItemBuilder(Material.PAPER).setDisplayName(
                                    new Translation("sanction_unban_player", Placeholder.unparsed("id", String.valueOf(sanction.getId())), name(playerAccount))).addLoreLines(
                                    new Translation("sanction_author", Placeholder.unparsed("author", serviceManager.getPlayerAccountService().getPlayerAccount(sanction.getAuthor()).map(PlayerAccount::getName).orElse(sanction.getAuthor().toString()))),
                                    new Translation("sanction_reason", Placeholder.unparsed("reason", sanction.getDescription())))
                            );

                            SimpleItem cancelItem = new SimpleItem(new ItemBuilder(Material.RED_CANDLE).setDisplayName(new Translation("sanction_cancel")), click -> click.getPlayer().closeInventory());

                            Gui gui = Gui.normal().setStructure("v.i.a").addIngredient('v', validationItem).addIngredient('i', infoItem).addIngredient('a', cancelItem).build();

                            Window window = Window.single().setTitle(new Translation("sanction_unban_title")).setGui(gui).build(player.getPlayer());
                            serviceManager.getRunnableHelper().runSynchronously(window::open);

                        }, () -> player.getMessageHandler().sendComponentMessage("moderation_sanction_not_found", name(playerAccount))), () -> player.getMessageHandler().sendMessage("command_validation_player_not_found", targetName));
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private TagResolver name(PlayerAccount playerAccount) {
        return Placeholder.unparsed("name", playerAccount.getName());
    }

}
