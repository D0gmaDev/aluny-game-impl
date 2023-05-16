package fr.aluny.gameimpl.translation;

import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameapi.utils.ChatUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.inventoryaccess.component.i18n.Languages;

public class TranslationServiceImpl implements TranslationService {

    private static final String DEFAULT_LOCALE_CODE = "fr-fr";

    private final Map<String, LocaleImpl> locales = new HashMap<>();

    @Override
    public void loadTranslations(JavaPlugin plugin, String code, String file) {
        Properties properties = new Properties();
        try (InputStream langStream = plugin.getClass().getClassLoader().getResourceAsStream(file)) {
            if (langStream == null)
                throw new IOException("lang stream is null");

            properties.load(new InputStreamReader(langStream, StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> translations = properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> ChatUtils.colorize(entry.getValue().toString())));

        LocaleImpl locale = this.locales.get(code);

        if (locale == null) {
            locale = new LocaleImpl(code, code.equals(DEFAULT_LOCALE_CODE), this);
            this.locales.put(code, locale);
        }

        locale.addTranslations(translations);

        Languages.getInstance().setLanguageProvider(player -> "fr-fr"); //todo change
        Languages.getInstance().addLanguage(code, locale.getTranslations());

        Bukkit.getLogger().info(String.format("Loaded %d translations from '%s' (%s) file of plugin %s.", translations.size(), file, code, plugin.getName()));
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
    public String getTranslation(String key, PlayerAccount playerAccount, String... args) {
        return getTranslation(key, playerAccount.getLocale(), args);
    }

    @Override
    public String getTranslation(String key, PlayerAccount playerAccount) {
        return getTranslation(key, playerAccount.getLocale());
    }

    @Override
    public String getTranslation(String key, Locale locale, String... args) {
        return locale.translate(key, (Object[]) args);
    }
}
