package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.BooleanValue;
import fr.aluny.gameapi.value.EnumValue;
import fr.aluny.gameapi.value.NumericValue;
import fr.aluny.gameapi.value.StringValue;
import fr.aluny.gameapi.value.TimeValue;
import fr.aluny.gameapi.value.ValueService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class ValueServiceImpl implements ValueService {

    private static final ValueMap<NumericValueImpl<? extends Number>> NUMERIC_MAP = new ValueMap<>();
    private static final ValueMap<BooleanValueImpl>                   BOOLEAN_MAP = new ValueMap<>();
    private static final ValueMap<TimeValueImpl>                      TIME_MAP    = new ValueMap<>();
    private static final ValueMap<EnumValueImpl<?>>                   ENUM_MAP    = new ValueMap<>();
    private static final ValueMap<StringValueImpl>                    STRING_MAP  = new ValueMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> NumericValueImpl<T> createNumericValue(String key, String nameKey, String descriptionKey, T defaultValue, T minValue, T maxValue, T smallStep, T mediumStem, T largeStep) {
        return (NumericValueImpl<T>) NUMERIC_MAP.createValueOrReset(key, () -> createUnregisteredNumericValue(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep));
    }

    @Override
    public BooleanValueImpl createBooleanValue(String key, String nameKey, String yesDescriptionKey, String noDescriptionKey, boolean value) {
        return BOOLEAN_MAP.createValueOrReset(key, () -> createUnregisteredBooleanValue(nameKey, yesDescriptionKey, noDescriptionKey, value));
    }

    @Override
    public TimeValueImpl createTimeValue(String key, String nameKey, String descriptionKey, Long defaultValue, Long minValue, Long maxValue, Long smallStep, Long mediumStem, Long largeStep, TimeUnit timeUnit) {
        return TIME_MAP.createValueOrReset(key, () -> createUnregisteredTimeValue(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep, timeUnit));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum<T>> EnumValueImpl<T> createEnumValue(String key, String nameKey, Class<T> enumerationClass, T defaultValue, String... descriptionKeys) {
        return (EnumValueImpl<T>) ENUM_MAP.createValueOrReset(key, () -> createUnregisteredEnumValue(nameKey, enumerationClass, defaultValue, descriptionKeys));
    }

    @Override
    public StringValueImpl createStringValue(String key, String nameKey, String descriptionKey, String value, int minLength, int maxLength) {
        return STRING_MAP.createValueOrReset(key, () -> createUnregisteredStringValue(nameKey, descriptionKey, value, minLength, maxLength));
    }

    @Override
    public <T extends Number> NumericValueImpl<T> createUnregisteredNumericValue(String nameKey, String descriptionKey, T defaultValue, T minValue, T maxValue, T smallStep, T mediumStem, T largeStep) {
        return new NumericValueImpl<>(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep);
    }

    @Override
    public BooleanValueImpl createUnregisteredBooleanValue(String nameKey, String yesDescriptionKey, String noDescriptionKey, boolean value) {
        return new BooleanValueImpl(nameKey, yesDescriptionKey, noDescriptionKey, value);
    }

    @Override
    public TimeValueImpl createUnregisteredTimeValue(String nameKey, String descriptionKey, Long defaultValue, Long minValue, Long maxValue, Long smallStep, Long mediumStem, Long largeStep, TimeUnit timeUnit) {
        return new TimeValueImpl(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep, timeUnit);
    }

    @Override
    public <T extends Enum<T>> EnumValueImpl<T> createUnregisteredEnumValue(String nameKey, Class<T> enumerationClass, T defaultValue, String... descriptionKeys) {
        return new EnumValueImpl<>(nameKey, enumerationClass, defaultValue, descriptionKeys);
    }

    @Override
    public StringValueImpl createUnregisteredStringValue(String nameKey, String descriptionKey, String value, int minLength, int maxLength) {
        return new StringValueImpl(nameKey, descriptionKey, value, minLength, maxLength);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> Optional<NumericValue<T>> getNumericValue(String key) {
        try {
            return Optional.ofNullable((NumericValueImpl<T>) NUMERIC_MAP.get(key));
        } catch (ClassCastException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Tried to cast a NumericValueImpl to a different type!", ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<BooleanValue> getBooleanValue(String key) {
        return Optional.ofNullable(BOOLEAN_MAP.get(key));
    }

    @Override
    public Optional<TimeValue> getTimeValue(String key) {
        return Optional.ofNullable(TIME_MAP.get(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum<T>> Optional<EnumValue<T>> getEnumValue(String key) {
        try {
            return Optional.ofNullable((EnumValueImpl<T>) ENUM_MAP.get(key));
        } catch (ClassCastException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Tried to cast an EnumValueImpl to a different type!", ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<StringValue> getStringValue(String key) {
        return Optional.ofNullable(STRING_MAP.get(key));
    }

    @Override
    public void removeValue(String key) {
        NUMERIC_MAP.remove(key);
        BOOLEAN_MAP.remove(key);
        TIME_MAP.remove(key);
        ENUM_MAP.remove(key);
        STRING_MAP.remove(key);
    }

    private static class ValueMap<T extends Value<?>> {

        private final Map<String, T> values = new HashMap<>();

        public T createValueOrReset(String key, Supplier<T> instantiation) {
            if (values.containsKey(key)) {
                T value = values.get(key);

                value.reset();
                return value;
            }

            T value = instantiation.get();
            values.put(key, value);

            return value;
        }

        public T get(String key) {
            return values.get(key);
        }

        public Optional<T> remove(String key) {
            return Optional.ofNullable(values.remove(key));
        }
    }
}
