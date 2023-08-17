package fr.aluny.gameimpl.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.player.GamePlayer;
import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.utils.TimeUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Function;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class CommandInvoker {

    private final Map<Class<?>, ArgumentParser> argumentParserMap = new HashMap<>();

    public CommandInvoker(ServiceManager serviceManager) {
        argumentParserMap.put(String.class, new ArgumentParser(argument -> argument, null));
        argumentParserMap.put(UUID.class, new ArgumentParser(UUID::fromString, "command_validation_not_a_uuid"));
        argumentParserMap.put(Long.class, new ArgumentParser(Long::parseLong, "command_validation_not_an_integer"));
        argumentParserMap.put(long.class, new ArgumentParser(Long::parseLong, "command_validation_not_an_integer"));
        argumentParserMap.put(Integer.class, new ArgumentParser(Integer::parseInt, "command_validation_not_an_integer"));
        argumentParserMap.put(int.class, new ArgumentParser(Integer::parseInt, "command_validation_not_an_integer"));
        argumentParserMap.put(Double.class, new ArgumentParser(Double::parseDouble, "command_validation_not_a_number"));
        argumentParserMap.put(double.class, new ArgumentParser(Double::parseDouble, "command_validation_not_a_number"));

        argumentParserMap.put(Player.class, new ArgumentParser(name -> Optional.ofNullable(Bukkit.getPlayer(name)).orElseThrow(IllegalArgumentException::new), "command_validation_player_not_found"));
        argumentParserMap.put(GamePlayer.class, new ArgumentParser(name -> Optional.ofNullable(Bukkit.getPlayer(name)).map(serviceManager.getGamePlayerService()::getPlayer).orElseThrow(IllegalArgumentException::new), "command_validation_player_not_found"));
        argumentParserMap.put(PlayerAccount.class, new ArgumentParser(name -> serviceManager.getPlayerAccountService().getPlayerAccountByName(name).orElseThrow(IllegalArgumentException::new), "command_validation_player_not_found"));
        argumentParserMap.put(TemporalAmount.class, new ArgumentParser(durationString -> TimeUtils.parsePositiveTemporalAmount(durationString).orElseThrow(IllegalArgumentException::new), "command_validation_invalid_duration"));
    }

    public void invoke(GamePlayer player, Command command, Method method, String[] args) {
        try {

            if (method.getParameterCount() <= 1) {
                method.invoke(command, player);
                return;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();

            if (method.getParameterCount() == 2 && parameterTypes[1].isArray()) {
                method.invoke(command, player, args);
                return;
            }

            List<Object> arguments = new ArrayList<>();
            arguments.add(player);

            for (int i = 1; i < method.getParameterCount(); i++) {

                if (parameterTypes[i].isArray()) {
                    arguments.add(Arrays.copyOfRange(args, i - 1, args.length));
                    break;
                }

                if (args.length < i) {
                    player.getMessageHandler().sendMessage("command_validation_not_enough_args");
                    return;
                }

                ArgumentParser commandInvoker = argumentParserMap.get(parameterTypes[i]);

                if (commandInvoker == null)
                    throw new IllegalArgumentException("Argument class not recognized for command " + command.getClass().getName() + ": " + parameterTypes[i].getName());

                try {
                    arguments.add(commandInvoker.parser.apply(args[i - 1]));
                } catch (IllegalArgumentException e) {
                    player.getMessageHandler().sendMessage(commandInvoker.errorMessage, Placeholder.unparsed("arg", args[i - 1]));
                    return;
                }
            }

            method.invoke(command, arguments.toArray());

        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private record ArgumentParser(Function<String, Object> parser, String errorMessage) {

    }

}
