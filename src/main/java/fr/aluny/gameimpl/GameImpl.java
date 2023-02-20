package fr.aluny.gameimpl;

import de.studiocode.invui.InvUI;
import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.Configuration;
import fr.aluny.gameapi.IAlunyGame;
import fr.aluny.gameapi.chat.ChatService;
import fr.aluny.gameapi.command.CommandService;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.moderation.VanishService;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.player.rank.RankService;
import fr.aluny.gameapi.proxy.ProxyMessagingService;
import fr.aluny.gameapi.scoreboard.ScoreboardService;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeamService;
import fr.aluny.gameapi.service.Service;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.settings.ServerSettings;
import fr.aluny.gameapi.timer.RunnableHelper;
import fr.aluny.gameapi.timer.TimerService;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameapi.value.ValueService;
import fr.aluny.gameapi.world.LootModifierService;
import fr.aluny.gameapi.world.SchematicService;
import fr.aluny.gameapi.world.anchor.AnchorService;
import fr.aluny.gameimpl.api.PlayerAPI;
import fr.aluny.gameimpl.api.RankAPI;
import fr.aluny.gameimpl.chat.ChatServiceImpl;
import fr.aluny.gameimpl.chat.PlayerChatListener;
import fr.aluny.gameimpl.command.CommandManager;
import fr.aluny.gameimpl.command.CommandServiceImpl;
import fr.aluny.gameimpl.message.MessageServiceImpl;
import fr.aluny.gameimpl.moderation.VanishServiceImpl;
import fr.aluny.gameimpl.player.GamePlayerServiceImpl;
import fr.aluny.gameimpl.player.PlayerAccountServiceImpl;
import fr.aluny.gameimpl.player.PlayerListener;
import fr.aluny.gameimpl.player.rank.RankServiceImpl;
import fr.aluny.gameimpl.proxy.ProxyMessagingServiceImpl;
import fr.aluny.gameimpl.scoreboard.ScoreboardServiceImpl;
import fr.aluny.gameimpl.scoreboard.team.ScoreboardTeamServiceImpl;
import fr.aluny.gameimpl.service.ServiceManagerImpl;
import fr.aluny.gameimpl.settings.ServerSettingsImpl;
import fr.aluny.gameimpl.timer.RunnableHelperImpl;
import fr.aluny.gameimpl.timer.TimerServiceImpl;
import fr.aluny.gameimpl.translation.TranslationServiceImpl;
import fr.aluny.gameimpl.value.ValueServiceImpl;
import fr.aluny.gameimpl.version.VersionMatcherImpl;
import fr.aluny.gameimpl.world.LootModifierListener;
import fr.aluny.gameimpl.world.LootModifierServiceImpl;
import fr.aluny.gameimpl.world.SchematicServiceImpl;
import fr.aluny.gameimpl.world.anchor.AnchorServiceImpl;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GameImpl extends JavaPlugin implements IAlunyGame {

    private ServiceManagerImpl serviceManager;

    private ServerSettingsImpl serverSettings;

    @Override
    public void onEnable() {
        Logger logger = Bukkit.getLogger();

        logger.log(Level.INFO, "===============[ GAME ]===============");
        logger.log(Level.INFO, "        Enabling Game by D0gma_       ");
        logger.log(Level.INFO, "   Please read carefully any output   ");
        logger.log(Level.INFO, "======================================");

        this.serverSettings = new ServerSettingsImpl();

        this.serviceManager = new ServiceManagerImpl();

        /* API */
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://elity.fr:8080");

        RankAPI rankAPI = new RankAPI(apiClient);
        PlayerAPI playerAPI = new PlayerAPI(apiClient, serviceManager);

        /* Services instantiation */
        AnchorServiceImpl anchorService = new AnchorServiceImpl();
        ChatServiceImpl chatService = new ChatServiceImpl();
        CommandServiceImpl commandService = new CommandServiceImpl(new CommandManager(this), serviceManager);
        GamePlayerServiceImpl gamePlayerService = new GamePlayerServiceImpl(serviceManager);
        LootModifierServiceImpl lootModifierService = new LootModifierServiceImpl(serviceManager);
        MessageServiceImpl messageService = new MessageServiceImpl(serviceManager);
        PlayerAccountServiceImpl playerService = new PlayerAccountServiceImpl(playerAPI, serviceManager);
        ProxyMessagingServiceImpl proxyMessagingService = new ProxyMessagingServiceImpl(this);
        RankServiceImpl rankService = new RankServiceImpl(rankAPI, serviceManager, serverSettings);
        SchematicServiceImpl schematicService = new SchematicServiceImpl();
        ScoreboardServiceImpl scoreboardService = new ScoreboardServiceImpl(serviceManager);
        ScoreboardTeamServiceImpl scoreboardTeamService = new ScoreboardTeamServiceImpl();
        TimerServiceImpl timerService = new TimerServiceImpl();
        TranslationServiceImpl translationService = new TranslationServiceImpl();
        ValueServiceImpl valueService = new ValueServiceImpl();
        VanishServiceImpl vanishService = new VanishServiceImpl(this, gamePlayerService);
        RunnableHelperImpl runnableHelper = new RunnableHelperImpl(this);

        /* Services registration */
        serviceManager.registerService(AnchorService.class, anchorService);
        serviceManager.registerService(ChatService.class, chatService);
        serviceManager.registerService(CommandService.class, commandService);
        serviceManager.registerService(GamePlayerService.class, gamePlayerService);
        serviceManager.registerService(LootModifierService.class, lootModifierService);
        serviceManager.registerService(MessageService.class, messageService);
        serviceManager.registerService(PlayerAccountService.class, playerService);
        serviceManager.registerService(ProxyMessagingService.class, proxyMessagingService);
        serviceManager.registerService(RankService.class, rankService);
        serviceManager.registerService(SchematicService.class, schematicService);
        serviceManager.registerService(ScoreboardService.class, scoreboardService);
        serviceManager.registerService(ScoreboardTeamService.class, scoreboardTeamService);
        serviceManager.registerService(TimerService.class, timerService);
        serviceManager.registerService(TranslationService.class, translationService);
        serviceManager.registerService(ValueService.class, valueService);
        serviceManager.registerService(VanishService.class, vanishService);
        serviceManager.registerService(RunnableHelper.class, runnableHelper);

        /* Services initialisation */
        serviceManager.getAllServices().forEach(Service::initialize);

        /* Listeners registration */
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerChatListener(chatService), this);
        pluginManager.registerEvents(new LootModifierListener(this, lootModifierService), this);
        pluginManager.registerEvents(new PlayerListener(this, serviceManager), this);

        /* Commands registration */
        //commandService.registerRuntimeCommand(new TestCommand());

        /* Inventories startup */
        InvUI.getInstance().setPlugin(this);

        /* Version Matcher */
        VersionMatcherImpl.matchVersion();

        /* Translations */
        translationService.loadTranslations(this, "fr-fr", "lang/fr.properties");

        logger.log(Level.INFO, "===============[ GAME ]===============");
        logger.log(Level.INFO, "          Game plugin enabled         ");
        logger.log(Level.INFO, "======================================");
    }

    @Override
    public ServiceManager getServiceManager() {
        return Optional.ofNullable(this.serviceManager).orElseThrow(() -> new IllegalStateException("Game ServiceManager not initialized"));
    }

    @Override
    public ServerSettings getServerSettings() {
        return Optional.ofNullable(this.serverSettings).orElseThrow(() -> new IllegalStateException("Game ServerSettings not initialized"));
    }
}