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

    private static final Map<String, Supplier<VersionWrapper>> VERSIONS = Map.of("1_20_R1", Wrapper_1_20_R1::new);


    public static void matchVersion() {
        VersionMatcher.setWrapper(getMatchingWrapper());
    }

    private static VersionWrapper getMatchingWrapper() {
        Optional<VersionWrapper> versionMatcher = Optional.ofNullable(VERSIONS.get(SERVER_VERSION)).map(Supplier::get);

        versionMatcher.ifPresentOrElse(
                versionWrapper -> Bukkit.getLogger().info("[VERSION] VersionWrapper " + versionWrapper.getClass().getSimpleName() + " found."),
                () -> Bukkit.getLogger().warning(("[VERSION] VersionMatcher cannot be found for version: " + SERVER_VERSION + ". Using DummyWrapper instead, server may not work as intended."))
        );

        return versionMatcher.orElse(DUMMY_WRAPPER);
    }
}
