package fr.aluny.gameimpl.moderation.command;

import static fr.aluny.gameapi.utils.ComponentUtils.id;
import static fr.aluny.gameapi.utils.ComponentUtils.name;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.TabCompleter;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.utils.GameUtils;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.Duration;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandInfo(name = "kick", permission = "fr.aluny.command.kick")
public class KickCommand extends Command {

    private final PlayerSanctionAPI playerSanctionAPI;
    private final ServiceManager    serviceManager;

    public KickCommand(PlayerSanctionAPI playerSanctionAPI, ServiceManager serviceManager) {
        this.playerSanctionAPI = playerSanctionAPI;
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, PlayerAccount playerAccount, String[] args) {

        if (!playerAccount.isOnline()) {
            player.getMessageHandler().sendMessage("moderation_target_offline", name(playerAccount));
            return;
        }

        if (args.length == 0) {
            player.getMessageHandler().sendMessage("moderation_provide_reason");
            return;
        }

        playerSanctionAPI.applySanction(playerAccount, player, SanctionType.KICK, Duration.ZERO, String.join(" ", args)).ifPresent(sanction ->
                player.getMessageHandler().sendMessage("moderation_successfully_kicked", name(playerAccount), id(sanction.getId())));

        serviceManager.getProxyMessagingService().kickFromProxy(player.getPlayer(), playerAccount.getName(), getReason(playerAccount.getLocale()));
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private Component getReason(Locale locale) {
        return MessageService.COMPONENT_PARSER.deserialize(locale.translate("moderation_kick_screen"));
    }

}
