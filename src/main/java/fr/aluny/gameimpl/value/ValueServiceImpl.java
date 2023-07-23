package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.ValueService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ValueServiceImpl implements ValueService {

    private static final ValueMap<NumericValueImpl<? extends Number>> NUMERIC_MAP = new ValueMap<>();
    private static final ValueMap<BooleanValueImpl>                   BOOLEAN_MAP = new ValueMap<>();
    private static final ValueMap<TimeValueImpl>                      TIME_MAP    = new ValueMap<>();
    private static final ValueMap<EnumValueImpl<?>>                   ENUM_MAP    = new ValueMap<>();
    private static final ValueMap<StringValueImpl>                    STRING_MAP  = new ValueMap<>();

    @Override
    public <T extends Number & Comparable<T>> NumericValueImpl<T> registerNumericValue(String key, String nameKey, String descriptionKey, T defaultValue, T minValue, T maxValue, T smallStep, T mediumStem, T largeStep) {
        return NUMERIC_MAP.registerUnsafeValue(key, createNumericValue(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep));
    }

    @Override
    public BooleanValueImpl registerBooleanValue(String key, String nameKey, String trueDescriptionKey, String falseDescriptionKey, boolean defaultValue) {
        return BOOLEAN_MAP.registerValue(key, createBooleanValue(nameKey, trueDescriptionKey, falseDescriptionKey, defaultValue));
    }

    @Override
    public TimeValueImpl registerTimeValue(String key, String nameKey, String descriptionKey, long defaultValue, long minValue, long maxValue, long smallStep, long mediumStem, long largeStep, TimeUnit timeUnit) {
        return TIME_MAP.registerValue(key, createTimeValue(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep, timeUnit));
    }

    @Override
    public <T extends Enum<T>> EnumValueImpl<T> registerEnumValue(String key, String nameKey, Class<T> enumerationClass, T defaultValue, String... descriptionKeys) {
        return ENUM_MAP.registerUnsafeValue(key, createEnumValue(nameKey, enumerationClass, defaultValue, descriptionKeys));
    }

    @Override
    public StringValueImpl registerStringValue(String key, String nameKey, String descriptionKey, String defaultValue, int minLength, int maxLength) {
        return STRING_MAP.registerValue(key, createStringValue(nameKey, descriptionKey, defaultValue, minLength, maxLength));
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

    @Override
    public <T extends Number & Comparable<T>> Optional<NumericValueImpl<T>> getNumericValue(Class<T> numericType, String key) {
        return Optional.ofNullable(NUMERIC_MAP.getUnsafeValue(key));
    }

    @Override
    public Optional<BooleanValueImpl> getBooleanValue(String key) {
        return Optional.ofNullable(BOOLEAN_MAP.getValue(key));
    }

    @Override
    public Optional<TimeValueImpl> getTimeValue(String key) {
        return Optional.ofNullable(TIME_MAP.getValue(key));
    }

    @Override
    public <T extends Enum<T>> Optional<EnumValueImpl<T>> getEnumValue(Class<T> enumClass, String key) {
        return Optional.ofNullable(ENUM_MAP.getUnsafeValue(key));
    }

    @Override
    public Optional<StringValueImpl> getStringValue(String key) {
        return Optional.ofNullable(STRING_MAP.getValue(key));
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

        public T registerValue(String key, T value) {
            values.put(key, value);
            return value;
        }

        @SuppressWarnings("unchecked")
        public <U extends T> U registerUnsafeValue(String key, T value) {
            return (U) registerValue(key, value);
        }

        public T getValue(String key) {
            return values.get(key);
        }

        @SuppressWarnings("unchecked")
        public <U extends T> U getUnsafeValue(String key) {
            return (U) getValue(key);
        }

        public Optional<T> remove(String key) {
            return Optional.ofNullable(values.remove(key));
        }
    }
}
