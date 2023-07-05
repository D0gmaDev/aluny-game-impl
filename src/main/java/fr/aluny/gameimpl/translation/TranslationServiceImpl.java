package fr.aluny.gameimpl.translation;

import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.translation.TranslationService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.inventoryaccess.component.i18n.Languages;

public class TranslationServiceImpl implements TranslationService {

    private static final String DEFAULT_LOCALE_CODE = "fr-fr";

    private final Map<String, LocaleImpl> locales = new HashMap<>();

    @Override
    public void loadTranslations(JavaPlugin plugin, String code, String filePath) {
        if (code == null)
            code = DEFAULT_LOCALE_CODE;

        Properties properties = new Properties();
        try (InputStream langStream = plugin.getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (langStream == null)
                throw new IOException("Language translation file stream is null");

            properties.load(new InputStreamReader(langStream, StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> translations = properties.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), this::sanitizeValue));

        LocaleImpl locale = this.locales.get(code);

        if (locale == null) {
            locale = new LocaleImpl(code, code.equals(DEFAULT_LOCALE_CODE), this);
            this.locales.put(code, locale);
        }

        locale.addTranslations(translations);

        Languages.getInstance().setLanguageProvider(player -> DEFAULT_LOCALE_CODE); //todo change
        Languages.getInstance().addLanguage(code, locale.getTranslations());

        Bukkit.getLogger().info(String.format("Loaded %d translations from '%s' (%s) file of plugin %s.", translations.size(), filePath, code, plugin.getName()));
    }

    @Override
    public void loadTranslationsFromDirectory(JavaPlugin plugin, String directoryPath) {
        listFilesInDirectory(directoryPath)
                .filter(fileName -> fileName.toLowerCase().endsWith(".properties"))
                .map(fileName -> fileName.substring(0, fileName.length() - ".properties".length()).toLowerCase())
                .forEach(fileName -> loadTranslations(plugin, fileName, directoryPath + File.separator + fileName));
    }

    /**
     * Sanitizes the value obtained from a lang file entry.
     * This method removes any occurrences of the legacy character '§' from the value, if present.
     * If the value contains '§', a warning message is logged indicating the usage of an illegal legacy character,
     * and the '§' characters are removed from the value.
     *
     * @param entry the entry from the lang file
     * @return the sanitized value as a String
     */
    private String sanitizeValue(Map.Entry<Object, Object> entry) {
        String value = entry.getValue().toString();

        if (value.contains("§")) {
            Bukkit.getLogger().warning("Translation '" + entry.getKey().toString() + "' contains illegal legacy character. Please use Minimessage format.");
            value = value.replace("§", "");
        }
        return value;
    }

    @Override
    public Optional<Locale> getLocale(String code) {
        return Optional.ofNullable(this.locales.get(code));
    }

    @Override
    public Locale getDefaultLocale() {
        return getLocale(DEFAULT_LOCALE_CODE).orElseThrow();
    }

    @Override
    public List<Locale> getAllLocales() {
        return List.copyOf(this.locales.values());
    }

    @Override
    public String getTranslation(String key, Locale locale) {
        return locale.translate(key);
    }

    @Override
    public String getTranslation(String key, PlayerAccount playerAccount) {
        return getTranslation(key, playerAccount.getLocale());
    }

    @Override
    public Component getComponentTranslation(String key, Locale locale, TagResolver... arguments) {
        return locale.translateComponent(key, arguments);
    }

    @Override
    public Component getComponentTranslation(String key, PlayerAccount playerAccount, TagResolver... arguments) {
        return getComponentTranslation(key, playerAccount.getLocale(), arguments);
    }

    private Stream<String> listFilesInDirectory(String directoryPath) {
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream.filter(file -> !Files.isDirectory(file)).map(Path::toFile)
                    .filter(File::isFile).map(File::getName);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Cannot readin translations directory " + directoryPath);
            e.printStackTrace();
            return Stream.empty();
        }
    }

}
