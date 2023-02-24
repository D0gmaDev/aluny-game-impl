package fr.aluny.gameimpl.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.CommandService;
import fr.aluny.gameapi.command.Default;
import fr.aluny.gameapi.command.SubCommand;
import fr.aluny.gameapi.command.TabCompleter;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.service.ServiceManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CommandServiceImpl implements CommandService {

    private final CommandManager commandManager;

    private final ServiceManager serviceManager;

    public CommandServiceImpl(CommandManager commandManager, ServiceManager serviceManager) {
        this.commandManager = commandManager;
        this.serviceManager = serviceManager;
    }

    @Override
    public void registerRuntimeCommand(Command command) {
        CommandInfo commandInfo = command.getClass().getAnnotation(CommandInfo.class);

        if (commandInfo == null)
            return;

        Method[] methods = command.getClass().getDeclaredMethods();

        List<SubCommandWrapper> subCommands = new ArrayList<>();

        Method defaultMethod = null;
        Method tabCompleter = null;

        for (Method method : methods) {
            Default def = method.getAnnotation(Default.class);
            if (def != null) {
                defaultMethod = method;
                continue;
            }

            SubCommand subCommandInfo = method.getAnnotation(SubCommand.class);

            if (subCommandInfo == null) {
                TabCompleter tabCompleterAnnotation = method.getAnnotation(TabCompleter.class);

                if (tabCompleterAnnotation != null)
                    tabCompleter = method;

                continue;
            }

            subCommands.add(new SubCommandWrapper(subCommandInfo.name(), subCommandInfo.permission(), subCommandInfo.suggest(), method));
        }

        this.commandManager.register(new BukkitCommandWrapper(command, commandInfo.name(), commandInfo.aliases(), commandInfo.permission(), defaultMethod, tabCompleter, subCommands));
    }

    @Override
    public void unRegisterRuntimeCommand(String commandName) {
        this.commandManager.unregister(commandName);
    }

    @Override
    public boolean isAlreadyRegistered(String command) {
        return this.commandManager.getRegistered().containsKey(command);
    }

    public class BukkitCommandWrapper extends BukkitCommand implements Listener {

        private final Command                 command;
        private final String                  defaultPermission;
        private final Method                  defaultMethod;
        private final Method                  tabCompleter;
        private final List<SubCommandWrapper> subCommands;

        BukkitCommandWrapper(Command command, String name, String[] aliases, String defaultPermission, Method defaultMethod, Method tabCompleter, List<SubCommandWrapper> subCommands) {
            super(name, "", "", Arrays.asList(aliases));
            this.setPermission(defaultPermission);

            this.command = command;
            this.defaultPermission = defaultPermission;
            this.defaultMethod = defaultMethod;
            this.tabCompleter = tabCompleter;
            this.subCommands = subCommands;
        }

        @Override
        public boolean execute(CommandSender commandSender, String s, String[] args) {
            if (!(commandSender instanceof Player player))
                return true;

            GamePlayer gamePlayer = serviceManager.getGamePlayerService().getPlayer(player);

            if (!player.hasPermission(defaultPermission)) {
                gamePlayer.getMessageHandler().sendMessage("command_validation_no_permission");
                return true;
            }

            if (args.length > 0) {
                List<SubCommandWrapper> validSubCommands = subCommands.stream().filter(subCommand -> subCommand.name.equalsIgnoreCase(args[0])).toList();
                if (validSubCommands.size() == 0) {
                    if (defaultMethod != null)
                        CommandInvoker.invoke(gamePlayer, command, defaultMethod, args);
                } else
                    validSubCommands.forEach(subCommand -> {
                        if (subCommand.permission.equalsIgnoreCase("") || player.hasPermission(subCommand.permission)) {
                            String[] newArgs = Arrays.stream(args).skip(1).toArray(String[]::new);
                            CommandInvoker.invoke(gamePlayer, command, subCommand.method, newArgs);
                        } else {
                            gamePlayer.getMessageHandler().sendMessage("command_validation_no_permission");
                        }
                    });
                return true;
            }

            if (defaultMethod != null)
                CommandInvoker.invoke(gamePlayer, command, defaultMethod, args);

            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
            if (!(sender instanceof Player player))
                return List.of();

            if (args.length == 1 && this.subCommands.stream().anyMatch(SubCommandWrapper::suggest))
                return this.subCommands.stream().filter(SubCommandWrapper::suggest).map(SubCommandWrapper::name).filter(name -> name.startsWith(args[args.length - 1])).toList();

            if (this.tabCompleter != null)
                try {
                    return (List<String>) tabCompleter.invoke(command, player, alias, args);
                } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                    e.printStackTrace();
                }

            return List.of();
        }
    }

    private record SubCommandWrapper(String name, String permission, boolean suggest, Method method) {

    }
}
