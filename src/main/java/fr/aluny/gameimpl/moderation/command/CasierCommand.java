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
import fr.aluny.gameapi.utils.TimeUtils;
import fr.aluny.gameimpl.api.PlayerAPI;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.moderation.sanction.DetailedPlayerSanction;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.builder.SkullBuilder;
import xyz.xenondevs.invui.item.impl.AsyncItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

@CommandInfo(name = "casier", permission = "fr.aluny.command.casier", asyncCall = true)
public class CasierCommand extends Command {

    private static final String CASIER_STRUCTURE = """
            # # # # h # # # #
            # x x x x x x x #
            # x x x x x x x #
            # x x x x x x x #
            # # # < # > # # #
            """;

    private final PlayerSanctionAPI playerSanctionAPI;
    private final PlayerAPI         playerAPI;
    private final ServiceManager    serviceManager;

    public CasierCommand(PlayerSanctionAPI playerSanctionAPI, PlayerAPI playerAPI, ServiceManager serviceManager) {
        this.playerSanctionAPI = playerSanctionAPI;
        this.playerAPI = playerAPI;
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, String name, String[] args) {

        serviceManager.getPlayerAccountService().getPlayerAccountByName(name).ifPresentOrElse(playerAccount -> {

            Item headItem = new SimpleItem(new SkullBuilder(playerAccount.getName())
                    .setDisplayName(new Translation("casier_target_name", Placeholder.component("name", LegacyComponentSerializer.legacySection().deserialize(playerAccount.getHighestRank().getPrefix() + playerAccount.getName()))))
                    .addLoreLines(new Translation("casier_first_connection", Formatter.date("date", playerAccount.getCreationDate()))));

            List<DetailedPlayerSanction> playerDetailedSanctions = playerSanctionAPI.getPlayerDetailedSanctions(playerAccount, 30, 0);

            List<Item> items = playerDetailedSanctions.stream().sorted().map(sanction -> new AsyncItem(new ItemBuilder(Material.BOOK).setDisplayName("§7Chargement..."), () -> new ItemBuilder(getMaterial(sanction.getSanctionType()))
                    .setDisplayName(new Translation("casier_details_title", Placeholder.unparsed("type", sanction.getSanctionType().name()), Placeholder.unparsed("id", String.valueOf(sanction.getId()))))
                    .addLoreLines(
                            new Translation("casier_reason", Placeholder.unparsed("reason", sanction.getDescription())),
                            new Translation("casier_author", Placeholder.unparsed("author", playerAPI.getPlayer(sanction.getAuthor()).map(PlayerAccount::getName).orElse("Erreur..."))),
                            new Translation("casier_date", Formatter.date("date", sanction.getStartAt())),
                            new Translation("casier_duration", Placeholder.unparsed("duration", TimeUtils.format(Duration.between(sanction.getStartAt(), sanction.getEndAt())))))
                    .addLoreLines(" ")
                    .addLoreLines(new Translation("casier_state", Formatter.choice("state", sanction.isCanceled() ? 2 : (sanction.isActive() ? 0 : 1)))
                    ))).collect(Collectors.toList());


            PagedGui<Item> gui = PagedGui.items().setStructure(9, 5, CASIER_STRUCTURE).addIngredient('h', headItem).setContent(items).build();

            Window window = Window.single().setGui(gui).setTitle(new Translation("casier_inventory_title", Placeholder.unparsed("name", playerAccount.getName()))).build(player.getPlayer());
            serviceManager.getRunnableHelper().runSynchronously(window::open);

        }, () -> player.getMessageHandler().sendMessage("command_validation_player_not_found", name));

    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private Material getMaterial(SanctionType sanctionType) {
        return switch (sanctionType) {
            case BAN -> Material.CRIMSON_SIGN;
            case MUTE -> Material.MANGROVE_SIGN;
            case KICK -> Material.BIRCH_SIGN;
            case UNKNOWN -> Material.OAK_SIGN;
        };
    }
}
