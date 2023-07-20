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
    public <T extends Number & Comparable<T>> NumericValueImpl<T> registerNumericValue(String key, String nameKey, String descriptionKey, T defaultValue, T minValue, T maxValue, T smallStep, T mediumStem, T largeStep) {
        return (NumericValueImpl<T>) NUMERIC_MAP.registerValueIfAbsent(key, () -> createNumericValue(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep));
    }

    @Override
    public BooleanValueImpl registerBooleanValue(String key, String nameKey, String trueDescriptionKey, String falseDescriptionKey, boolean defaultValue) {
        return BOOLEAN_MAP.registerValueIfAbsent(key, () -> createBooleanValue(nameKey, trueDescriptionKey, falseDescriptionKey, defaultValue));
    }

    @Override
    public TimeValueImpl registerTimeValue(String key, String nameKey, String descriptionKey, long defaultValue, long minValue, long maxValue, long smallStep, long mediumStem, long largeStep, TimeUnit timeUnit) {
        return TIME_MAP.registerValueIfAbsent(key, () -> createTimeValue(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep, timeUnit));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum<T>> EnumValueImpl<T> registerEnumValue(String key, String nameKey, Class<T> enumerationClass, T defaultValue, String... descriptionKeys) {
        return (EnumValueImpl<T>) ENUM_MAP.registerValueIfAbsent(key, () -> createEnumValue(nameKey, enumerationClass, defaultValue, descriptionKeys));
    }

    @Override
    public StringValueImpl registerStringValue(String key, String nameKey, String descriptionKey, String defaultValue, int minLength, int maxLength) {
        return STRING_MAP.registerValueIfAbsent(key, () -> createStringValue(nameKey, descriptionKey, defaultValue, minLength, maxLength));
    }

    @Override
    public <T extends Number & Comparable<T>> NumericValueImpl<T> createNumericValue(String nameKey, String descriptionKey, T defaultValue, T minValue, T maxValue, T smallStep, T mediumStem, T largeStep) {
        return new NumericValueImpl<>(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep);
    }

    @Override
    public BooleanValueImpl createBooleanValue(String nameKey, String trueDescriptionKey, String falseDescriptionKey, boolean defaultValue) {
        return new BooleanValueImpl(nameKey, trueDescriptionKey, falseDescriptionKey, defaultValue);
    }

    @Override
    public TimeValueImpl createTimeValue(String nameKey, String descriptionKey, long defaultValue, long minValue, long maxValue, long smallStep, long mediumStem, long largeStep, TimeUnit timeUnit) {
        return new TimeValueImpl(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep, timeUnit);
    }

    @Override
    public <T extends Enum<T>> EnumValueImpl<T> createEnumValue(String nameKey, Class<T> enumerationClass, T defaultValue, String... descriptionKeys) {
        return new EnumValueImpl<>(nameKey, enumerationClass, defaultValue, descriptionKeys);
    }

    @Override
    public StringValueImpl createStringValue(String nameKey, String descriptionKey, String defaultValue, int minLength, int maxLength) {
        return new StringValueImpl(nameKey, descriptionKey, defaultValue, minLength, maxLength);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number & Comparable<T>> Optional<NumericValue<T>> getNumericValue(Class<T> numericType, String key) {
        try {
            return Optional.ofNullable(NUMERIC_MAP.get(key)).filter(numericValue -> numericType.isInstance(numericValue.getValue())).map(numericValue -> (NumericValueImpl<T>) numericValue);
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
    public <T extends Enum<T>> Optional<EnumValue<T>> getEnumValue(Class<T> enumClass, String key) {
        try {
            return Optional.ofNullable(ENUM_MAP.get(key)).filter(enumValue -> enumClass.isInstance(enumValue.getValue())).map(enumValue -> (EnumValueImpl<T>) enumValue);
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

        public T registerValueIfAbsent(String key, Supplier<T> instantiation) {
            if (values.containsKey(key)) {
                return values.get(key);
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
