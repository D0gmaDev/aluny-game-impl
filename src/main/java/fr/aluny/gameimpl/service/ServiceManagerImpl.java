package fr.aluny.gameimpl.service;

import fr.aluny.gameapi.chat.ChatService;
import fr.aluny.gameapi.command.CommandService;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.moderation.ModerationService;
import fr.aluny.gameapi.moderation.VanishService;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.player.rank.RankService;
import fr.aluny.gameapi.proxy.ProxyMessagingService;
import fr.aluny.gameapi.scoreboard.ScoreboardService;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeamService;
import fr.aluny.gameapi.service.Service;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.timer.RunnableHelper;
import fr.aluny.gameapi.timer.TimerService;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameapi.value.ValueService;
import fr.aluny.gameapi.world.LootModifierService;
import fr.aluny.gameapi.world.SchematicService;
import fr.aluny.gameapi.world.anchor.AnchorService;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;

public class ServiceManagerImpl implements ServiceManager {

    private AnchorService         anchorService;
    private ChatService           chatService;
    private CommandService        commandService;
    private GamePlayerService     gamePlayerService;
    private LootModifierService   lootModifierService;
    private MessageService        messageService;
    private ModerationService     moderationService;
    private PlayerAccountService  playerAccountService;
    private ProxyMessagingService proxyMessagingService;
    private RankService           rankService;
    private SchematicService      schematicService;
    private ScoreboardService     scoreboardService;
    private ScoreboardTeamService scoreboardTeamService;
    private TimerService          timerService;
    private TranslationService    translationService;
    private ValueService          valueService;
    private VanishService         vanishService;
    private RunnableHelper        runnableHelper;

    public void initializeServices() {
        Collection<Service> services = getAllServices();
        services.forEach(Service::initialize);
        Bukkit.getLogger().info("[SERVICE] Successfully initialized " + services.size() + " services.");
    }

    public void shutdownServices() {
        Collection<Service> services = getAllServices();
        services.forEach(Service::shutdown);
        Bukkit.getLogger().info("[SERVICE] Successfully shut down " + services.size() + " services.");
    }

    private Collection<Service> getAllServices() {
        return List.of(
                anchorService, chatService, commandService, gamePlayerService, lootModifierService,
                messageService, moderationService, playerAccountService, proxyMessagingService, rankService,
                schematicService, scoreboardService, scoreboardTeamService, timerService, translationService,
                valueService, vanishService, runnableHelper
        );
    }

    @Override
    public AnchorService getAnchorService() {
        return anchorService;
    }

    public void setAnchorService(AnchorService anchorService) {
        this.anchorService = anchorService;
    }

    @Override
    public ChatService getChatService() {
        return chatService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public CommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public GamePlayerService getGamePlayerService() {
        return gamePlayerService;
    }

    public void setGamePlayerService(GamePlayerService gamePlayerService) {
        this.gamePlayerService = gamePlayerService;
    }

    @Override
    public LootModifierService getLootModifierService() {
        return lootModifierService;
    }

    public void setLootModifierService(LootModifierService lootModifierService) {
        this.lootModifierService = lootModifierService;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ModerationService getModerationService() {
        return moderationService;
    }

    public void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @Override
    public PlayerAccountService getPlayerAccountService() {
        return playerAccountService;
    }

    public void setPlayerAccountService(PlayerAccountService playerAccountService) {
        this.playerAccountService = playerAccountService;
    }

    @Override
    public ProxyMessagingService getProxyMessagingService() {
        return proxyMessagingService;
    }

    public void setProxyMessagingService(ProxyMessagingService proxyMessagingService) {
        this.proxyMessagingService = proxyMessagingService;
    }

    @Override
    public RankService getRankService() {
        return rankService;
    }

    public void setRankService(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    public SchematicService getSchematicService() {
        return schematicService;
    }

    public void setSchematicService(SchematicService schematicService) {
        this.schematicService = schematicService;
    }

    @Override
    public ScoreboardService getScoreboardService() {
        return scoreboardService;
    }

    public void setScoreboardService(ScoreboardService scoreboardService) {
        this.scoreboardService = scoreboardService;
    }

    @Override
    public ScoreboardTeamService getScoreboardTeamService() {
        return scoreboardTeamService;
    }

    public void setScoreboardTeamService(ScoreboardTeamService scoreboardTeamService) {
        this.scoreboardTeamService = scoreboardTeamService;
    }

    @Override
    public TimerService getTimerService() {
        return timerService;
    }

    public void setTimerService(TimerService timerService) {
        this.timerService = timerService;
    }

    @Override
    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public ValueService getValueService() {
        return valueService;
    }

    public void setValueService(ValueService valueService) {
        this.valueService = valueService;
    }

    @Override
    public VanishService getVanishService() {
        return vanishService;
    }

    public void setVanishService(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    @Override
    public RunnableHelper getRunnableHelper() {
        return runnableHelper;
    }

    public void setRunnableHelper(RunnableHelper runnableHelper) {
        this.runnableHelper = runnableHelper;
    }
}
