package fr.aluny.gameimpl.translation;

import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.translation.Locale;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class LocaleImpl implements Locale {

    private final String              code;
    private final boolean             defaultLocale;
    private final Map<String, String> translations = new HashMap<>();

    private final TranslationServiceImpl translationService;

    public LocaleImpl(String code, boolean defaultLocale, TranslationServiceImpl translationService) {
        this.code = code;
        this.defaultLocale = defaultLocale;
        this.translationService = translationService;
    }

    @Override
    public void addTranslations(Map<String, String> translations) {
        this.translations.putAll(translations);
    }

    @Override
    public String translate(String key) {
        if (this.translations.containsKey(key))
            return this.translations.get(key);

        if (this.defaultLocale)
            return "Missing translation [" + key + "]";
        else
            return translationService.getDefaultLocale().translate(key);
    }

    @Override
    public Component translateComponent(String key, TagResolver... arguments) {
        return MessageService.COMPONENT_PARSER.deserialize(translate(key), arguments);
    }

    @Override
    public boolean hasTranslation(String key) {
        return this.translations.containsKey(key);
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public boolean isDefaultLocale() {
        return this.defaultLocale;
    }

    public Map<String, String> getTranslations() {
        return this.translations;
    }

    @Override
    public String toString() {
        return "LocaleImpl{" +
                "code='" + code + '\'' +
                ", defaultLocale=" + defaultLocale +
                ", translations=" + translations.size() +
                '}';
    }
}
