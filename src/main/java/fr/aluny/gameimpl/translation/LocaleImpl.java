package fr.aluny.gameimpl.translation;

import fr.aluny.gameapi.translation.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class LocaleImpl implements Locale {

    private static final Random RANDOM = new Random();

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
    public List<TranslationPair> getAllTranslationsStartingWith(String prefix) {
        List<TranslationPair> translations = new ArrayList<>();
        this.translations.forEach((s, s2) -> {
            if (s.startsWith(prefix))
                translations.add(new TranslationPair(s, s2));
        });

        return translations.stream().sorted(Comparator.comparing(TranslationPair::key)).collect(Collectors.toList());
    }

    @Override
    public String getRandomTranslationsStartingWith(String prefix) {
        List<TranslationPair> list = getAllTranslationsStartingWith(prefix);
        if (list.isEmpty())
            return "Missing translation [" + prefix + "]";

        return list.get(RANDOM.nextInt(list.size())).value();
    }

    @Override
    public String getRandomTranslationsStartingWith(String prefix, Object... arguments) {
        List<TranslationPair> list = getAllTranslationsStartingWith(prefix);
        if (list.isEmpty())
            return "Missing translation [" + prefix + "]";

        return String.format(list.get(RANDOM.nextInt(list.size())).value(), arguments);
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
    public String translate(String key, Object... arguments) {
        if (this.translations.containsKey(key))
            return String.format(this.translations.get(key), arguments);

        if (this.defaultLocale)
            return "Missing translation [" + key + "]";
        else
            return translationService.getDefaultLocale().translate(key, arguments);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public boolean isDefaultLocale() {
        return defaultLocale;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    @Override
    public String toString() {
        return "LocaleImpl{" +
                "code='" + code + '\'' +
                ", defaultLocale=" + defaultLocale +
                '}';
    }
}
