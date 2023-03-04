package fr.aluny.gameimpl.moderation.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.TabCompleter;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.utils.GameUtils;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.Duration;
import java.util.List;
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
    public void defaultContext(GamePlayer player, String name, String[] args) {

        //TODO check isOnline
        serviceManager.getPlayerAccountService().getPlayerAccountByName(name).ifPresentOrElse(playerAccount -> {

            if (args.length == 0) {
                player.getMessageHandler().sendMessage("moderation_provide_reason");
                return;
            }

            playerSanctionAPI.applySanction(playerAccount, player, SanctionType.KICK, Duration.ZERO, String.join(" ", args)).ifPresent(sanction ->
                    player.getMessageHandler().sendMessage("moderation_successfully_kicked", playerAccount.getName(), String.valueOf(sanction.getId())));
            serviceManager.getProxyMessagingService().kickFromProxy(player.getPlayer(), playerAccount.getName(), getReasonString(playerAccount.getLocale()));

        }, () -> player.getMessageHandler().sendMessage("command_validation_player_not_found", name));
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private String getReasonString(Locale locale) {
        return locale.translate("moderation_kick_screen");
    }

}
