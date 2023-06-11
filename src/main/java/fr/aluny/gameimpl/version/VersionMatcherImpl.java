package fr.aluny.gameimpl.version;

import fr.aluny.gameapi.version.VersionMatcher;
import fr.aluny.gameapi.version.VersionWrapper;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

public class VersionMatcherImpl {

    private static final String         SERVER_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
    private static final VersionWrapper DUMMY_WRAPPER  = new DummyWrapper();

    private static final Map<String, Supplier<VersionWrapper>> VERSIONS = Map.of("1_19_R3", Wrapper_1_19_R3::new);


    public static void matchVersion() {
        VersionMatcher.setWrapper(match());
    }

    private static VersionWrapper match() {
        Optional<Supplier<VersionWrapper>> versionMatcher = Optional.ofNullable(VERSIONS.get(SERVER_VERSION));

        versionMatcher.ifPresentOrElse(
                versionWrapper -> Bukkit.getLogger().info("[VERSION] VersionWrapper " + versionWrapper.getClass().getSimpleName() + " found."),
                () -> Bukkit.getLogger().warning(("[VERSION] VersionMatcher cannot be found for version: " + SERVER_VERSION + ". Using DummyWrapper instead, server may not work as intended."))
        );

        return versionMatcher.map(Supplier::get).orElse(DUMMY_WRAPPER);
    }
}
