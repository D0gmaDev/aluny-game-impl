package fr.aluny.gameimpl;

import fr.aluny.alunyapi.generated.ApiClient;
import fr.aluny.alunyapi.generated.Configuration;
import fr.aluny.alunyapi.generated.auth.HttpBearerAuth;
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
import fr.aluny.gameimpl.item.ItemServiceImpl;
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
import fr.aluny.gameimpl.world.VoidGenerator;
import fr.aluny.gameimpl.world.anchor.AnchorServiceImpl;
import java.io.BufferedReader;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.inventoryaccess.component.i18n.Languages;
import xyz.xenondevs.invui.InvUI;

public class GameImpl extends JavaPlugin implements IAlunyGame {

    private final static String API_BASE_PATH = System.getenv("GAME_API_BASE_PATH");
    private final static String API_TOKEN     = System.getenv("GAME_API_TOKEN");

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

        /* API connection */
        if (API_BASE_PATH == null)
            throw new IllegalStateException("No api connection path specified in the environment variables ('GAME_API_BASE_PATH')");

        if (API_TOKEN == null)
            throw new IllegalStateException("No api token specified in the environment variables ('GAME_API_TOKEN')");

        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setServerIndex(null);
        apiClient.setBasePath(API_BASE_PATH);
        ((HttpBearerAuth) apiClient.getAuthentication("api_key")).setBearerToken(API_TOKEN);

        /* API managers */
        RankAPI rankAPI = new RankAPI(apiClient);
        PlayerAPI playerAPI = new PlayerAPI(apiClient, serviceManager);
        PlayerSanctionAPI playerSanctionAPI = new PlayerSanctionAPI(apiClient);

        /* Services instantiation */
        RunnableHelperImpl runnableHelper = new RunnableHelperImpl(this);
        AnchorServiceImpl anchorService = new AnchorServiceImpl();
        ChatServiceImpl chatService = new ChatServiceImpl(serviceManager);
        CommandServiceImpl commandService = new CommandServiceImpl(new CommandManager(this), serviceManager);
        GamePlayerServiceImpl gamePlayerService = new GamePlayerServiceImpl(serviceManager);
        ItemServiceImpl itemService = new ItemServiceImpl(serviceManager, this);
        LootModifierServiceImpl lootModifierService = new LootModifierServiceImpl(serviceManager);
        MessageServiceImpl messageService = new MessageServiceImpl(serviceManager);
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
        serviceManager.setItemService(itemService);
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
        pluginManager.registerEvents(new PlayerListener(serviceManager), this);

        /* Commands registration */
        commandService.registerRuntimeCommand(new PrivateMessageCommand(serviceManager));

        /* Inventories startup */
        InvUI.getInstance().setPlugin(this);
        InventoryHelper.load();

        /* Version Matcher */
        VersionMatcherImpl.matchVersion();

        /* Translations */
        translationService.loadTranslations(this, Locale.FRANCE, "lang/fr.properties");
        Languages.getInstance().setLanguageProvider(gamePlayerService::getCachedLocaleCode);

        logger.info("===============[ GAME ]===============");
        logger.info("          Game plugin enabled         ");
        logger.info("======================================");
    }

    @Override
    public void onDisable() {
        /* Services shutdown */
        if (isEnabled())
            serviceManager.shutdownServices();

        Component kickComponent = Component.text("Server shutdown...", TextColor.color(120, 87, 41)).appendNewline().append(getRandomQuote());
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(kickComponent));
    }

    private Component getRandomQuote() {
        return Optional.ofNullable(getTextResource("text/quotes.txt"))
                .map(quotesResource -> new BufferedReader(quotesResource).lines().toList())
                .filter(Predicate.not(List::isEmpty))
                .map(quotes -> Component.text(quotes.get(new Random().nextInt(quotes.size())), NamedTextColor.GRAY, TextDecoration.ITALIC))
                .orElse(Component.empty());
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidGenerator();
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