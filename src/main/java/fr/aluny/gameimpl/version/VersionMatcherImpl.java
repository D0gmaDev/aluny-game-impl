package fr.aluny.gameimpl.version;

import fr.aluny.gameapi.version.VersionMatcher;
import fr.aluny.gameapi.version.VersionWrapper;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

public class VersionMatcherImpl {

    private static final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);

    private static final Map<String, Supplier<? extends VersionWrapper>> versions = Map.of("1_19_R1", Wrapper_1_19_R1::new);

    public static void matchVersion() {
        VersionMatcher.setWrapper(match());
    }

    private static VersionWrapper match() {
        VersionWrapper versionWrapper = Optional.ofNullable(versions.get(serverVersion)).orElseThrow(() -> new IllegalStateException("[VERSION] VersionMatcher cannot be found: " + serverVersion)).get();
        Bukkit.getLogger().info("[VERSION] VersionWrapper " + versionWrapper.getClass().getSimpleName() + " found.");
        return versionWrapper;
    }
}
