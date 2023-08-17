package fr.aluny.gameimpl.moderation.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.SubCommand;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameimpl.player.GamePlayerImpl;
import fr.aluny.gameimpl.player.GamePlayerServiceImpl;
import fr.aluny.gameimpl.player.PlayerAccountServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

@CommandInfo(name = "debug", permission = "fr.aluny.command.debug")
public class DebugCommand extends Command {

    private final ServiceManager serviceManager;

    public DebugCommand(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @SubCommand(name = "account")
    public void accountContext(GamePlayer gamePlayer, PlayerAccount target, String[] args) {

        Component targetComponent = Component.text("Compte de " + target.getName(), TextColor.color(180, 87, 84)).appendNewline()
                .append(Component.text(String.valueOf(target.getUuid())).clickEvent(ClickEvent.copyToClipboard(String.valueOf(target.getUuid())))).appendNewline()
                .append(Component.text("Clique ici pour copier les infos").decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.copyToClipboard(target.toString())));

        gamePlayer.getPlayer().sendMessage(targetComponent);
    }

    @SubCommand(name = "account_cache_size")
    public void accountCacheSizeContext(GamePlayer gamePlayer, String[] args) {
        if (serviceManager.getPlayerAccountService() instanceof PlayerAccountServiceImpl accountService) {
            gamePlayer.getPlayer().sendMessage(Component.text("Cache size : " + accountService.getCacheSize()).appendSpace()
                    .append(Component.text("Clear cache", NamedTextColor.RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/debug clear_account_cache"))));
        }
    }

    @SubCommand(name = "clear_account_cache")
    public void clearAccountCacheContext(GamePlayer gamePlayer, String[] args) {
        if (serviceManager.getPlayerAccountService() instanceof PlayerAccountServiceImpl accountService) {
            accountService.clearCache();
            gamePlayer.getPlayer().sendMessage(Component.text("Cache cleared", NamedTextColor.DARK_GREEN));
        }
    }

    @SubCommand(name = "players_data_size")
    public void playersDataSizeContext(GamePlayer gamePlayer, String[] args) {
        if (serviceManager.getGamePlayerService() instanceof GamePlayerServiceImpl gamePlayerService) {
            gamePlayer.getPlayer().sendMessage(Component.text("Data size : " + gamePlayerService.getPlayersDataSize()));
        }
    }

    @SubCommand(name = "clear_offline_players_data")
    public void clearOfflinePlayersDataContext(GamePlayer gamePlayer, String[] args) {
        if (serviceManager.getGamePlayerService() instanceof GamePlayerServiceImpl gamePlayerService) {
            gamePlayerService.clearOfflinePlayersData();
            gamePlayer.getPlayer().sendMessage(Component.text("Cleared offline players data", NamedTextColor.DARK_GREEN));
        }
    }

    @SubCommand(name = "has_permission")
    public void playerPermissionContext(GamePlayer gamePlayer, PlayerAccount target, String permission, String[] args) {
        gamePlayer.getPlayer().sendMessage(Component.text(target.getName() + (target.hasPermission(permission) ? " a" : " n'a pas") + " la permission " + permission + "."));
    }

    @SubCommand(name = "update_cached_rank")
    public void updateCachedRankContext(GamePlayer gamePlayer, GamePlayer target, String[] args) {
        if (target instanceof GamePlayerImpl targetImpl) {
            Rank highestRank = serviceManager.getPlayerAccountService().getPlayerAccount(targetImpl).getHighestRank();
            targetImpl.setCachedHighestRank(highestRank);
            gamePlayer.getPlayer().sendMessage(Component.text("Updated " + targetImpl.getPlayerName() + "'s cached rank to ").append(Component.text(highestRank.getName(), highestRank.getTextColor())));
        }
    }

}
