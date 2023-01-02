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

public class RankServiceImpl implements RankService {

    private final RankAPI                   rankAPI;
    private final ServiceManager            serviceManager;
    private final ServerSettings            serverSettings;
    private final Map<Rank, ScoreboardTeam> ranks     = new HashMap<>();
    private final Map<Integer, Rank>        ranksById = new HashMap<>();

    public RankServiceImpl(RankAPI rankAPI, ServiceManager serviceManager, ServerSettings serverSettings) {
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
            ScoreboardTeam scoreboardTeam = serviceManager.getScoreboardTeamService().registerScoreboardTeam(rank.getName(), rank.getPrefix());
            this.ranks.put(rank, scoreboardTeam);
            this.ranksById.put(rank.getId(), rank);
        }
    }

    public void onPlayerJoin(GamePlayer gamePlayer, PlayerAccount playerAccount) {
        this.ranks.get(playerAccount.getHighestRank()).addPlayer(gamePlayer);
    }
}
