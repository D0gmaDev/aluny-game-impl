package fr.aluny.gameimpl.player.command;

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
import fr.aluny.gameimpl.message.MessageServiceImpl;
import fr.aluny.gameimpl.player.DetailedPlayerAccount;
import fr.aluny.gameimpl.player.PlayerAccountServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandInfo(name = "msg", asyncCall = true)
public class PrivateMessageCommand extends Command {

    private static final String BYPASS_PERMISSION = "fr.aluny.bypass_private_message";

    private final ServiceManager serviceManager;

    public PrivateMessageCommand(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, String receiverName, String[] words) {

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

        Component senderComponent = getPrivateMessageComponent(senderAccount.getLocale(), senderAccount, receiverAccount, message);
        Component receiverComponent = getPrivateMessageComponent(receiverAccount.getLocale(), senderAccount, receiverAccount, message);

        serviceManager.getProxyMessagingService().sendMessage(player.getPlayer(), receiverAccount.getName(), receiverComponent);
        MessageServiceImpl.getAudiences().player(player.getPlayer()).sendMessage(senderComponent);
        Bukkit.getLogger().info("[CHAT] [PRIVATE] " + player.getPlayerName() + " > " + receiverAccount.getName() + " : " + message);
    }

    @TabCompleter
    public List<String> tabCompleter(Player player, String alias, String[] args) {
        return args.length == 1 ? GameUtils.getOnlinePlayersPrefixed(args[0]) : List.of();
    }

    private Component getPrivateMessageComponent(Locale locale, PlayerAccount senderAccount, PlayerAccount receiverAccount, String message){
        String format = locale.translate("private_message_format");

        return MessageService.COMPONENT_PARSER.deserialize(format,
                Placeholder.component("sender_prefix", Component.text(senderAccount.getHighestRank().getPrefix(), senderAccount.getHighestRank().getTextColor())),
                Placeholder.component("sender_name", Component.text(senderAccount.getName(), senderAccount.getHighestRank().getTextColor())),
                Placeholder.component("receiver_prefix", Component.text(receiverAccount.getHighestRank().getPrefix(), receiverAccount.getHighestRank().getTextColor())),
                Placeholder.component("receiver_name", Component.text(receiverAccount.getName(), receiverAccount.getHighestRank().getTextColor())),
                Placeholder.unparsed("message", message)
        );
    }
}
