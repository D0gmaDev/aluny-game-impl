package fr.aluny.gameimpl.player.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.TabCompleter;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.utils.GameUtils;
import java.util.List;
import org.bukkit.entity.Player;

@CommandInfo(name = "msg", asyncCall = true)
public class PrivateMessageCommand extends Command {

    private static final String BYPASS_PERMISSION = "fr.aluny.bypass_private_message";

    private static final String PRIVATE_MESSAGE_FORMAT = "<sender_prefix><sender_name> <dark_gray>» <receiver_prefix><receiver_name> <dark_gray>: <#9bcc91><message>";

    private final ServiceManager serviceManager;

    public PrivateMessageCommand(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, String receiverName, String[] words) {

        //TODO uncomment when game-impl pull #3 is merged

        /*
        if (words.length == 0) {
            player.getMessageHandler().sendMessage("private_message_empty");
            return;
        }

        PlayerAccount senderAccount = serviceManager.getPlayerAccountService().getPlayerAccount(player);

        if (serviceManager.getModerationService().isMuted(player.getUuid())) {
            player.getMessageHandler().sendComponentMessage("moderation_cancelled_muted", Formatter.date("date", serviceManager.getModerationService().getUnMuteDate(player.getUuid())));
            return;
        }

        UUID receiverUuid;

        Player receiver = Bukkit.getPlayer(receiverName);

        if (receiver != null) {
            receiverUuid = receiver.getUniqueId();
        } else {
            Optional<PlayerAccount> playerAccountByName = serviceManager.getPlayerAccountService().getPlayerAccountByName(receiverName);

            if (playerAccountByName.isEmpty() || !playerAccountByName.get().isOnline()) {
                player.getMessageHandler().sendComponentMessage("private_message_offline", Placeholder.unparsed("name", receiverName));
                return;
            }

            receiverUuid = playerAccountByName.get().getUuid();
        }

        if (player.getUuid().equals(receiverUuid)) {
            player.getMessageHandler().sendMessage("private_message_self");
            return;
        }

        DetailedPlayerAccount receiverAccount = ((PlayerAccountServiceImpl) serviceManager.getPlayerAccountService()).getDetailedPlayerAccount(receiverUuid).orElseThrow();

        if (!receiverAccount.doesAllowPrivateMessages() && !senderAccount.hasPermission(BYPASS_PERMISSION)) {
            player.getMessageHandler().sendComponentMessage("private_message_disallow", Placeholder.unparsed("name", receiverAccount.getName()));
            return;
        }

        String message = String.join(" ", words);

        Component privateMessageComponent = MessageService.COMPONENT_PARSER.deserialize(PRIVATE_MESSAGE_FORMAT,
                Placeholder.component("sender_prefix", Component.text(senderAccount.getHighestRank().getPrefix(), senderAccount.getHighestRank().getTextColor())),
                Placeholder.component("sender_name", Component.text(senderAccount.getName(), senderAccount.getHighestRank().getTextColor())),
                Placeholder.component("receiver_prefix", Component.text(receiverAccount.getHighestRank().getPrefix(), receiverAccount.getHighestRank().getTextColor())),
                Placeholder.component("receiver_name", Component.text(receiverAccount.getName(), receiverAccount.getHighestRank().getTextColor())),
                Placeholder.unparsed("message", message)
        );

        serviceManager.getProxyMessagingService().sendMessage(player.getPlayer(), receiverAccount.getName(), privateMessageComponent);
        MessageServiceImpl.getAudiences().player(player.getPlayer()).sendMessage(privateMessageComponent);
        Bukkit.getLogger().info("[PRIVATE-CHAT] " + player.getPlayerName() + " > " + receiverAccount.getName() + " : " + message);
        */
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

}
