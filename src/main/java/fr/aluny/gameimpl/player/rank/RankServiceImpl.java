package fr.aluny.gameimpl.player.rank;

import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.player.rank.RankService;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeam;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.settings.ServerSettings;
import fr.aluny.gameimpl.api.RankAPI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
        List<Rank> ranks = rankAPI.loadAllRanks().stream().sorted(Comparator.comparingInt(Rank::getImportanceIndex).reversed()).toList();

        int offset = 0;
        for (Rank rank : ranks) {
            char importancePrefix = (char) ('A' + offset++); // To sort correctly the teams in the tab list (it sorts alphabetically by the name of the team)

            Component prefix = Component.text(rank.getPrefix(), rank.getTextColor());

            ScoreboardTeam scoreboardTeam = serviceManager.getScoreboardTeamService().registerScoreboardTeam(importancePrefix + rank.getName(), prefix);

            this.ranks.put(rank, scoreboardTeam);
            this.ranksById.put(rank.getId(), rank);
        }

        Bukkit.getLogger().info("[RANK] Successfully loaded " + ranks.size() + " ranks.");
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
