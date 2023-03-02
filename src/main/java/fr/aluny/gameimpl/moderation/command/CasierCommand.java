package fr.aluny.gameimpl.moderation.command;

import de.studiocode.invui.gui.GUI;
import de.studiocode.invui.gui.builder.GUIBuilder;
import de.studiocode.invui.gui.builder.guitype.GUIType;
import de.studiocode.invui.item.Item;
import de.studiocode.invui.item.builder.ItemBuilder;
import de.studiocode.invui.item.builder.SkullBuilder;
import de.studiocode.invui.item.impl.AsyncItem;
import de.studiocode.invui.item.impl.SimpleItem;
import de.studiocode.invui.window.impl.single.SimpleWindow;
import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.utils.TimeUtils;
import fr.aluny.gameimpl.api.PlayerAPI;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.moderation.sanction.DetailedPlayerSanction;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

@CommandInfo(name = "casier", permission = "fr.aluny.command.casier")
public class CasierCommand extends Command {

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

            ChatColor color = ChatColor.of("#4e6654");

            Item headItem = new SimpleItem(new SkullBuilder(playerAccount.getName())
                    .setDisplayName(playerAccount.getHighestRank().getPrefix() + playerAccount.getName())
                    .addLoreLines(color + "1ère connexion: §7" + TimeUtils.formatDateToCET(playerAccount.getCreationDate(), Locale.FRENCH)));

            List<DetailedPlayerSanction> playerDetailedSanctions = playerSanctionAPI.getPlayerDetailedSanctions(playerAccount, 30, 0);

            List<Item> items = playerDetailedSanctions.stream().map(sanction -> new AsyncItem(new ItemBuilder(Material.BOOK).setDisplayName("§7Chargement..."), () -> new ItemBuilder(getMaterial(sanction.getSanctionType()))
                    .setDisplayName(ChatColor.of("#70cc89") + sanction.getSanctionType().name() + " §7(#" + sanction.getId() + ")")
                    .addLoreLines(
                            color + "Raison: §7" + sanction.getDescription(),
                            color + "Auteur: §7" + playerAPI.getPlayer(sanction.getAuthor()).map(PlayerAccount::getName).orElse("Erreur..."),
                            color + "Date: §7" + TimeUtils.formatDateToCET(sanction.getStartAt(), Locale.FRENCH),
                            color + "Durée: §7" + TimeUtils.format(Duration.between(sanction.getStartAt(), sanction.getEndAt())),
                            " ",
                            color + "Etat: §7" + (sanction.isCanceled() ? "§cAnnulé" : (sanction.isActive() ? "§aEn cours" : "§7Expiré"))
                    ))).collect(Collectors.toList());

            GUI gui = new GUIBuilder<>(GUIType.PAGED_ITEMS)
                    .setStructure(
                            "# # # # h # # # #",
                            "# x x x x x x x #",
                            "# x x x x x x x #",
                            "# x x x x x x x #",
                            "# # # < # > # # #")
                    .addIngredient('h', headItem).setItems(items).build();

            SimpleWindow simpleWindow = new SimpleWindow(player.getPlayer(), color + "Casier de " + playerAccount.getName(), gui);
            simpleWindow.show();

        }, () -> player.getMessageHandler().sendMessage("command_validation_player_not_found", name));

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
