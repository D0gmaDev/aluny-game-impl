package fr.aluny.gameimpl.translation;

import fr.aluny.gameapi.player.PlayerAccount;
import fr.aluny.gameapi.translation.Locale;
import fr.aluny.gameapi.translation.TranslationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TranslationServiceImpl implements TranslationService {

    private static final java.util.Locale DEFAULT_LOCALE = java.util.Locale.FRANCE;

    private final Map<java.util.Locale, LocaleImpl> locales = new HashMap<>();

    private final TranslationsLoader translationsLoader = new TranslationsLoader(this, DEFAULT_LOCALE, locales::get, locales::put);

    @Override
    public void loadTranslations(JavaPlugin plugin, java.util.Locale javaLocale, String filePath) {

        java.util.Locale locale = javaLocale != null ? javaLocale : DEFAULT_LOCALE;
        int translationsNumber = this.translationsLoader.loadTranslations(plugin, locale, filePath);

        Bukkit.getLogger().info(String.format("Loaded %d translations from '%s' (%s) file of plugin %s.", translationsNumber, filePath, locale.toLanguageTag(), plugin.getName()));
    }

    @Override
    public void loadTranslationsFromDirectory(JavaPlugin plugin, String directoryPath) {
        System.err.println("not implemented loadTranslationsFromDirectory method called"); //TODO implement
    }

    @Override
    public Optional<Locale> getLocale(java.util.Locale locale) {
        return Optional.ofNullable(this.locales.get(locale));
    }

    @Override
    public Locale getDefaultLocale() {
        return getLocale(DEFAULT_LOCALE).orElseThrow();
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

}
