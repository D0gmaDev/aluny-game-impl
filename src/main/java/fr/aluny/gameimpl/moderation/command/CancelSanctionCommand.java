package fr.aluny.gameimpl.moderation.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

@CommandInfo(name = "unban", aliases = "unmute", permission = "fr.aluny.command.unban")
public class CancelSanctionCommand extends Command {

    private final PlayerSanctionAPI playerSanctionAPI;
    private final ServiceManager    serviceManager;

    public CancelSanctionCommand(PlayerSanctionAPI playerSanctionAPI, ServiceManager serviceManager) {
        this.playerSanctionAPI = playerSanctionAPI;
        this.serviceManager = serviceManager;
    }

    @Default
    public void defaultContext(GamePlayer player, int sanctionId, String[] args) {

        playerSanctionAPI.cancelSanction(sanctionId).ifPresentOrElse(
                sanction -> player.getMessageHandler().sendComponentMessage("moderation_sanction_canceled", Placeholder.unparsed("name", sanction.getPlayer().toString())),
                () -> player.getMessageHandler().sendComponentMessage("moderation_sanction_not_found", Placeholder.unparsed("id", String.valueOf(sanctionId))));
    }

}
