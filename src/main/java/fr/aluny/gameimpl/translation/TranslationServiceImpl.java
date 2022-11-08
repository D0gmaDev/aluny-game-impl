package fr.aluny.gameimpl.translation;

import fr.aluny.gameapi.player.PlayerBean;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameapi.utils.ChatUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.bukkit.plugin.java.JavaPlugin;

public class TranslationServiceImpl implements TranslationService {

    private final Map<String, Locale> locales = new HashMap<>();

    @Override
    public void loadTranslations(JavaPlugin plugin, String code, String file) {
        Properties properties = new Properties();
        try (InputStream langStream = plugin.getClass().getClassLoader().getResourceAsStream(file)) {
            properties.load(langStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> translations = properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> ChatUtils.colorize(entry.getValue().toString())));

        Locale frenchLocale = new Locale(code, true, this, translations);
        locales.put(frenchLocale.getCode(), frenchLocale);
    }

    @Override
    public Optional<Locale> getLocale(String code) {
        return Optional.ofNullable(this.locales.get(code));
    }

    @Override
    public Locale getLocale(PlayerBean playerBean) {
        return null;
    }

    @Override
    public Locale getDefaultLocale() {
        return getLocale("fr").orElseThrow();
    }

    @Override
    public List<Locale> getAllLocales() {
        return new ArrayList<>(this.locales.values());
    }

    @Override
    public String getTranslation(String key, Locale locale) {
        return locale.translate(key);
    }

    @Override
    public String getTranslation(String key, PlayerBean playerBean, String... args) {
        return getTranslation(key, getLocale(playerBean), args);
    }

    @Override
    public String getTranslation(String key, PlayerBean playerBean) {
        return getTranslation(key, getLocale(playerBean));
    }

    @Override
    public String getTranslation(String key, Locale locale, String... args) {
        return locale.translate(key, (Object[]) args);
    }
}
