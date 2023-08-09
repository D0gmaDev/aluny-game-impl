package fr.aluny.gameimpl.moderation;

import fr.aluny.gameapi.command.CommandService;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.api.PlayerAPI;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.moderation.command.*;
import fr.aluny.gameimpl.moderation.sanction.PlayerSanction;
import fr.aluny.gameimpl.moderation.sanction.SanctionType;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.UUID;

public class ModerationServiceImpl implements fr.aluny.gameapi.moderation.ModerationService {

    private final PlayerSanctionAPI playerSanctionAPI;
    private final PlayerAPI         playerAPI;
    private final ServiceManager    serviceManager;

    public ModerationServiceImpl(PlayerSanctionAPI playerSanctionAPI, PlayerAPI playerAPI, ServiceManager serviceManager) {
        this.playerSanctionAPI = playerSanctionAPI;
        this.playerAPI = playerAPI;
        this.serviceManager = serviceManager;
    }

    @Override
    public void initialize() {
        CommandService commandService = serviceManager.getCommandService();

        commandService.registerRuntimeCommand(new BanCommand(playerSanctionAPI, serviceManager));
        commandService.registerRuntimeCommand(new MuteCommand(playerSanctionAPI, serviceManager));
        commandService.registerRuntimeCommand(new KickCommand(playerSanctionAPI, serviceManager));
        commandService.registerRuntimeCommand(new UnbanCommand(playerSanctionAPI, playerAPI, serviceManager));
        commandService.registerRuntimeCommand(new UnmuteCommand(playerSanctionAPI, playerAPI, serviceManager));
        commandService.registerRuntimeCommand(new CasierCommand(playerSanctionAPI, playerAPI, serviceManager));

        commandService.registerRuntimeCommand(new DebugCommand(serviceManager));
    }

    @Override
    public boolean isMuted(UUID uuid) {
        return this.playerAPI.getDetailedPlayer(uuid).stream()
                .flatMap(account -> account.getCurrentSanctions().stream())
                .anyMatch(sanction -> sanction.isType(SanctionType.MUTE) && sanction.isActive());
    }

    @Override
    public OffsetDateTime getUnMuteDate(UUID uuid) {
        return this.playerAPI.getDetailedPlayer(uuid)
                .flatMap(account -> account.getCurrentSanctions().stream()
                        .filter(sanction -> sanction.isType(SanctionType.MUTE) && sanction.isActive())
                        .map(PlayerSanction::getEndAt).max(Comparator.comparingLong(OffsetDateTime::toEpochSecond)))
                .orElse(OffsetDateTime.now());
    }
}
