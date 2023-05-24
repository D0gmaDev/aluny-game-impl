package fr.aluny.gameimpl;

import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.Configuration;
import fr.aluny.gameapi.IAlunyGame;
import fr.aluny.gameapi.inventory.InventoryHelper;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.settings.ServerSettings;
import fr.aluny.gameimpl.api.PlayerAPI;
import fr.aluny.gameimpl.api.PlayerSanctionAPI;
import fr.aluny.gameimpl.api.RankAPI;
import fr.aluny.gameimpl.chat.ChatServiceImpl;
import fr.aluny.gameimpl.chat.PlayerChatListener;
import fr.aluny.gameimpl.command.CommandManager;
import fr.aluny.gameimpl.command.CommandServiceImpl;
import fr.aluny.gameimpl.message.MessageServiceImpl;
import fr.aluny.gameimpl.moderation.ModerationServiceImpl;
import fr.aluny.gameimpl.moderation.VanishServiceImpl;
import fr.aluny.gameimpl.player.GamePlayerServiceImpl;
import fr.aluny.gameimpl.player.PlayerAccountServiceImpl;
import fr.aluny.gameimpl.player.PlayerListener;
import fr.aluny.gameimpl.player.command.PrivateMessageCommand;
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
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;

public class GameImpl extends JavaPlugin implements IAlunyGame {

    private static JavaPlugin plugin;

    private ServiceManagerImpl serviceManager;

    private ServerSettingsImpl serverSettings;

    @Override
    public void onEnable() {
        plugin = this;

        Logger logger = Bukkit.getLogger();

        logger.info("===============[ GAME ]===============");
        logger.info("        Enabling Game by D0gma_       ");
        logger.info("   Please read carefully any output   ");
        logger.info("======================================");

        this.serverSettings = new ServerSettingsImpl();

        this.serviceManager = new ServiceManagerImpl();

        /* API */
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://elity.fr:8080");

        RankAPI rankAPI = new RankAPI(apiClient);
        PlayerAPI playerAPI = new PlayerAPI(apiClient, serviceManager);
        PlayerSanctionAPI playerSanctionAPI = new PlayerSanctionAPI(apiClient);

        /* Services instantiation */
        RunnableHelperImpl runnableHelper = new RunnableHelperImpl(this);
        AnchorServiceImpl anchorService = new AnchorServiceImpl();
        ChatServiceImpl chatService = new ChatServiceImpl(serviceManager);
        CommandServiceImpl commandService = new CommandServiceImpl(new CommandManager(this), serviceManager);
        GamePlayerServiceImpl gamePlayerService = new GamePlayerServiceImpl(serviceManager);
        LootModifierServiceImpl lootModifierService = new LootModifierServiceImpl(serviceManager);
        MessageServiceImpl messageService = new MessageServiceImpl(this, serviceManager);
        ModerationServiceImpl moderationService = new ModerationServiceImpl(playerSanctionAPI, playerAPI, serviceManager);
        PlayerAccountServiceImpl playerService = new PlayerAccountServiceImpl(playerAPI, serviceManager);
        ProxyMessagingServiceImpl proxyMessagingService = new ProxyMessagingServiceImpl(this);
        RankServiceImpl rankService = new RankServiceImpl(this, rankAPI, serviceManager, serverSettings);
        SchematicServiceImpl schematicService = new SchematicServiceImpl();
        ScoreboardServiceImpl scoreboardService = new ScoreboardServiceImpl(this, serviceManager);
        ScoreboardTeamServiceImpl scoreboardTeamService = new ScoreboardTeamServiceImpl();
        TimerServiceImpl timerService = new TimerServiceImpl();
        TranslationServiceImpl translationService = new TranslationServiceImpl();
        ValueServiceImpl valueService = new ValueServiceImpl();
        VanishServiceImpl vanishService = new VanishServiceImpl(this, gamePlayerService);

        /* Services registration */
        serviceManager.setRunnableHelper(runnableHelper);
        serviceManager.setAnchorService(anchorService);
        serviceManager.setChatService(chatService);
        serviceManager.setCommandService(commandService);
        serviceManager.setGamePlayerService(gamePlayerService);
        serviceManager.setLootModifierService(lootModifierService);
        serviceManager.setMessageService(messageService);
        serviceManager.setModerationService(moderationService);
        serviceManager.setPlayerAccountService(playerService);
        serviceManager.setProxyMessagingService(proxyMessagingService);
        serviceManager.setRankService(rankService);
        serviceManager.setSchematicService(schematicService);
        serviceManager.setScoreboardService(scoreboardService);
        serviceManager.setScoreboardTeamService(scoreboardTeamService);
        serviceManager.setTimerService(timerService);
        serviceManager.setTranslationService(translationService);
        serviceManager.setValueService(valueService);
        serviceManager.setVanishService(vanishService);

        /* Services initialisation */
        serviceManager.initializeServices();

        /* Listeners registration */
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerChatListener(chatService, gamePlayerService), this);
        pluginManager.registerEvents(new LootModifierListener(this, lootModifierService), this);
        pluginManager.registerEvents(new PlayerListener(this, serviceManager), this);

        /* Commands registration */
        commandService.registerRuntimeCommand(new PrivateMessageCommand(serviceManager));

        /* Inventories startup */
        InvUI.getInstance().setPlugin(this);
        InventoryHelper.load();

        /* Version Matcher */
        VersionMatcherImpl.matchVersion();

        /* Translations */
        translationService.loadTranslations(this, "fr-fr", "lang/fr.properties");

        logger.info("===============[ GAME ]===============");
        logger.info("          Game plugin enabled         ");
        logger.info("======================================");
    }

    @Override
    public void onDisable() {
        /* Services shutdown */
        serviceManager.shutdownServices();
    }

    @Override
    public ServiceManager getServiceManager() {
        return Optional.ofNullable(this.serviceManager).orElseThrow(() -> new IllegalStateException("Game ServiceManager not initialized"));
    }

    @Override
    public ServerSettings getServerSettings() {
        return Optional.ofNullable(this.serverSettings).orElseThrow(() -> new IllegalStateException("Game ServerSettings not initialized"));
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }
}