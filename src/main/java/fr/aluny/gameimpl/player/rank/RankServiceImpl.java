package fr.aluny.gameimpl.player.rank;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.player.rank.RankService;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeam;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.settings.ServerSettings;
import fr.aluny.gameimpl.api.RankAPI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class RankServiceImpl implements RankService {

    private final JavaPlugin                plugin;
    private final RankAPI                   rankAPI;
    private final ServiceManager            serviceManager;
    private final ServerSettings            serverSettings;
    private final Map<Rank, ScoreboardTeam> ranks     = new HashMap<>();
    private final Map<Integer, Rank>        ranksById = new HashMap<>();

    public RankServiceImpl(JavaPlugin plugin, RankAPI rankAPI, ServiceManager serviceManager, ServerSettings serverSettings) {
        this.plugin = plugin;
        this.rankAPI = rankAPI;
        this.serviceManager = serviceManager;
        this.serverSettings = serverSettings;
    }

    @Override
    public List<Rank> getRanks() {
        return this.ranks.keySet().stream().toList();
    }

    @Override
    public Optional<Rank> getRankById(int id) {
        return Optional.ofNullable(this.ranksById.get(id));
    }

    @Override
    public void initialize() {
        for (Rank rank : rankAPI.loadAllRanks()) {
            char importancePrefix = (char) ('z' - rank.getImportanceIndex()); // To sort correctly the teams in the tab list (it sorts alphabetically by the name of the team)
            ScoreboardTeam scoreboardTeam = serviceManager.getScoreboardTeamService().registerScoreboardTeam(importancePrefix + rank.getName(), rank.getPrefix());
            this.ranks.put(rank, scoreboardTeam);
            this.ranksById.put(rank.getId(), rank);
        }
    }

    public void onPlayerJoin(GamePlayer gamePlayer, PlayerAccount playerAccount) {
        if (this.serverSettings.doesShowRank())
            this.ranks.get(playerAccount.getHighestRank()).addPlayer(gamePlayer);

        PermissionAttachment permissionAttachment = gamePlayer.getPlayer().addAttachment(this.plugin);
        serviceManager.getRunnableHelper().runAsynchronously(() -> {
            playerAccount.getRanks().forEach(rank -> rank.getPermissions().forEach(permission -> permissionAttachment.setPermission(permission, true)));
            serviceManager.getRunnableHelper().runSynchronously(gamePlayer.getPlayer()::updateCommands);
        });
    }
}
