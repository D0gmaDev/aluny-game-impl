package fr.aluny.gameimpl.command;

import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.player.GamePlayer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class CommandInvoker {

    private static final Map<Class<?>, ArgumentParser> PARSER_MAP = new HashMap<>();

    static {
        PARSER_MAP.put(String.class, new ArgumentParser(Function.identity(), null));
        PARSER_MAP.put(UUID.class, new ArgumentParser(UUID::fromString, "command_validation_not_a_uuid"));
        PARSER_MAP.put(Long.class, new ArgumentParser(Long::parseLong, "command_validation_not_an_integer"));
        PARSER_MAP.put(long.class, new ArgumentParser(Long::parseLong, "command_validation_not_an_integer"));
        PARSER_MAP.put(Integer.class, new ArgumentParser(Integer::parseInt, "command_validation_not_an_integer"));
        PARSER_MAP.put(int.class, new ArgumentParser(Integer::parseInt, "command_validation_not_an_integer"));
        PARSER_MAP.put(Double.class, new ArgumentParser(Double::parseDouble, "command_validation_not_a_number"));
        PARSER_MAP.put(double.class, new ArgumentParser(Double::parseDouble, "command_validation_not_a_number"));
        PARSER_MAP.put(Player.class, new ArgumentParser(s -> Optional.ofNullable(Bukkit.getPlayer(s)).orElseThrow(IllegalArgumentException::new), "command_validation_player_not_found"));
    }

    static void invoke(GamePlayer player, Command command, Method method, String[] args) {
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
                    arguments.add(Arrays.stream(args).skip(i - 1).toArray(String[]::new));
                    break;
                }

                if (args.length < i) {
                    player.getMessageHandler().sendMessage("command_validation_not_enough_args");
                    return;
                }

                ArgumentParser commandInvoker = PARSER_MAP.get(parameterTypes[i]);

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

    private record ArgumentParser(Function<String, ?> parser, String errorMessage) {

    }

}
