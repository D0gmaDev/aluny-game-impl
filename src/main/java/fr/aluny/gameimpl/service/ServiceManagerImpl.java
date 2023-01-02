package fr.aluny.gameimpl.service;

import fr.aluny.gameapi.anchor.AnchorService;
import fr.aluny.gameapi.chat.ChatService;
import fr.aluny.gameapi.command.CommandService;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.moderation.VanishService;
import fr.aluny.gameapi.player.GamePlayerService;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.player.rank.RankService;
import fr.aluny.gameapi.scoreboard.ScoreboardService;
import fr.aluny.gameapi.scoreboard.team.ScoreboardTeamService;
import fr.aluny.gameapi.service.NoServiceException;
import fr.aluny.gameapi.service.Service;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.timer.RunnableHelper;
import fr.aluny.gameapi.timer.TimerService;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameapi.value.ValueService;
import fr.aluny.gameapi.world.LootModifierService;
import fr.aluny.gameapi.world.SchematicService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public class ServiceManagerImpl implements ServiceManager {

    private static final Logger LOGGER = Bukkit.getLogger();

    private final Map<Class<? extends Service>, Service> serviceMap = new HashMap<>();

    public <T extends Service> void registerService(Class<T> facadeService, T service) {
        serviceMap.put(facadeService, service);
        LOGGER.log(Level.INFO, "Service " + facadeService.getName() + " registered.");
    }

    private <T extends Service> T getService(Class<T> serviceClass) {
        return Optional.ofNullable(serviceMap.get(serviceClass)).map(serviceClass::cast).orElseThrow(() -> new NoServiceException(serviceClass.getName()));
    }

    public Collection<Service> getAllServices() {
        return serviceMap.values();
    }

    /* SERVICES */

    @Override
    public AnchorService getAnchorService() {
        return getService(AnchorService.class);
    }

    @Override
    public ChatService getChatService() {
        return getService(ChatService.class);
    }

    @Override
    public CommandService getCommandService() {
        return getService(CommandService.class);
    }

    @Override
    public GamePlayerService getGamePlayerService() {
        return getService(GamePlayerService.class);
    }

    @Override
    public LootModifierService getLootModifierService() {
        return getService(LootModifierService.class);
    }

    @Override
    public MessageService getMessageService() {
        return getService(MessageService.class);
    }

    @Override
    public PlayerAccountService getPlayerAccountService() {
        return getService(PlayerAccountService.class);
    }

    @Override
    public RankService getRankService() {
        return getService(RankService.class);
    }

    @Override
    public SchematicService getSchematicService() {
        return getService(SchematicService.class);
    }

    @Override
    public ScoreboardService getScoreboardService() {
        return getService(ScoreboardService.class);
    }

    @Override
    public ScoreboardTeamService getScoreboardTeamService() {
        return getService(ScoreboardTeamService.class);
    }

    @Override
    public TimerService getTimerService() {
        return getService(TimerService.class);
    }

    @Override
    public TranslationService getTranslationService() {
        return getService(TranslationService.class);
    }

    @Override
    public ValueService getValueService() {
        return getService(ValueService.class);
    }

    @Override
    public VanishService getVanishService() {
        return getService(VanishService.class);
    }

    @Override
    public RunnableHelper getRunnableHelper() {
        return getService(RunnableHelper.class);
    }
}
