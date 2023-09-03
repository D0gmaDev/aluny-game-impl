package fr.aluny.gameimpl.translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.inventoryaccess.component.i18n.Languages;

class TranslationsLoader {

    private final TranslationServiceImpl translationService;
    private final Locale                 defaultLocale;

    private final Function<Locale, LocaleImpl>   localeFetcher;
    private final BiConsumer<Locale, LocaleImpl> newLocaleRegisterer;

    TranslationsLoader(TranslationServiceImpl translationService, Locale defaultLocale, Function<Locale, LocaleImpl> localeFetcher, BiConsumer<Locale, LocaleImpl> newLocaleRegisterer) {
        this.translationService = translationService;
        this.defaultLocale = defaultLocale;
        this.localeFetcher = localeFetcher;
        this.newLocaleRegisterer = newLocaleRegisterer;
    }

    int loadTranslations(JavaPlugin plugin, Locale locale, String resourcePath) {
        Properties properties = new Properties();
        try (InputStream langStream = plugin.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (langStream == null)
                throw new IOException("Language translation file stream is null");

            properties.load(new InputStreamReader(langStream, StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> translations = properties.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), TranslationsLoader::sanitizeValue));

        LocaleImpl localeImpl = this.localeFetcher.apply(locale);

        if (localeImpl == null) {
            localeImpl = new LocaleImpl(locale, locale.equals(this.defaultLocale), this.translationService);
            newLocaleRegisterer.accept(locale, localeImpl);
        }

        localeImpl.addTranslations(translations);

        Languages.getInstance().addLanguage(locale.toLanguageTag(), localeImpl.getTranslations()); // add gui translations

        return translations.size();
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
    private static String sanitizeValue(Map.Entry<Object, Object> entry) {
        String value = entry.getValue().toString();

        if (value.contains("§")) {
            Bukkit.getLogger().warning("Translation '" + entry.getKey().toString() + "' contains illegal legacy character. Please use Minimessage format.");
            value = value.replace("§", "");
        }
        return value;
    }
}
