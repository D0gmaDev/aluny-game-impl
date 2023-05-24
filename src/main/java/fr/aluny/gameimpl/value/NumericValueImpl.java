package fr.aluny.gameimpl.value;


import fr.aluny.gameapi.value.NumericValue;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class NumericValueImpl<T extends Number> extends Value<T> implements NumericValue<T> {

    private static final DecimalFormat DOUBLE_DECIMAL = new DecimalFormat("#.##");

    private final String nameKey;
    private final String descriptionKey;
    private final T      smallStep;
    private final T      mediumStep;
    private final T      largeStep;
    private final T      defaultValue;

    private final T defaultMaxValue;
    private final T defaultMinValue;

    private T value;

    public NumericValueImpl(String nameKey, String descriptionKey, T defaultValue, T minValue, T maxValue, T smallStep, T mediumStem, T largeStep) {
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.defaultMaxValue = maxValue;
        this.defaultMinValue = minValue;
        this.smallStep = smallStep;
        this.mediumStep = mediumStem;
        this.largeStep = largeStep;

        this.defaultValue = defaultValue;
        this.value = defaultValue;

        addRestriction("default", RestrictionType.MINIMAL_VALUE, minValue);
        addRestriction("default", RestrictionType.MAXIMAL_VALUE, maxValue);
    }

    @Override
    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    @Override
    public String getStringValue() {
        if (getValue() instanceof Integer integerValue) {
            return Integer.toString(integerValue);
        } else if (getValue() instanceof Long longValue) {
            return Long.toString(longValue);
        } else if (getValue() instanceof Double doubleValue) {
            return DOUBLE_DECIMAL.format(doubleValue);
        } else if (getValue() instanceof Float floatValue) {
            return DOUBLE_DECIMAL.format(floatValue);
        }

        return getValue().toString();
    }

    @Override
    public T getMaxValue() {
        return getRestrictions().stream().filter(restriction -> restriction.isType(RestrictionType.MAXIMAL_VALUE))
                .min(Comparator.comparingDouble(restriction -> restriction.getValue().doubleValue()))
                .map(ValueRestriction::getValue).orElse(this.defaultMaxValue);
    }

    @Override
    public T getMinValue() {
        return getRestrictions().stream().filter(restriction -> restriction.isType(RestrictionType.MINIMAL_VALUE))
                .max(Comparator.comparingDouble(restriction -> restriction.getValue().doubleValue()))
                .map(ValueRestriction::getValue).orElse(this.defaultMinValue);
    }

    @Override
    public T getSmallStep() {
        return this.smallStep;
    }

    @Override
    public T getMediumStep() {
        return this.mediumStep;
    }

    @Override
    public T getLargeStep() {
        return this.largeStep;
    }

    @Override
    public void smallIncrement() {
        setValue(add(getValue(), getSmallStep()));
    }

    @Override
    public void mediumIncrement() {
        setValue(add(getValue(), getMediumStep()));
    }

    @Override
    public void largeIncrement() {
        setValue(add(getValue(), getLargeStep()));
    }

    @Override
    public void smallDecrement() {
        setValue(subtract(getValue(), getSmallStep()));
    }

    @Override
    public void mediumDecrement() {
        setValue(subtract(getValue(), getMediumStep()));
    }

    @Override
    public void largeDecrement() {
        setValue(subtract(getValue(), getLargeStep()));
    }

    @Override
    public String getNameKey() {
        return this.nameKey;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T value) {
        T minValue = getMinValue();
        T maxValue = getMaxValue();
        if (isInferior(value, minValue))
            value = minValue;
        if (isSuperior(value, maxValue))
            value = maxValue;

        T oldValue = this.value;
        this.value = value;
        onValueChanged(oldValue, value);
    }

    @Override
    public boolean isEdited() {
        return !Objects.equals(getValue(), getDefaultValue());
    }

    @Override
    public T getDefaultValue() {
        return this.defaultValue;
    }

    private boolean isSuperior(T a, T b) {
        if (a instanceof Integer) {
            return a.intValue() > b.intValue();
        } else if (a instanceof Double) {
            return a.doubleValue() > b.doubleValue();
        } else if (a instanceof Float) {
            return a.floatValue() > b.floatValue();
        } else if (a instanceof Long) {
            return a.longValue() > b.longValue();
        } else {
            throw new UnsupportedOperationException("Cannot compare " + a.getClass().getName() + " types !");
        }
    }

    private boolean isInferior(T a, T b) {
        if (a instanceof Integer) {
            return a.intValue() < b.intValue();
        } else if (a instanceof Double) {
            return a.doubleValue() < b.doubleValue();
        } else if (a instanceof Float) {
            return a.floatValue() < b.floatValue();
        } else if (a instanceof Long) {
            return a.longValue() < b.longValue();
        } else {
            throw new UnsupportedOperationException("Cannot compare " + a.getClass().getName() + " types !");
        }
    }

    private T add(T a, T b) {
        T total;
        if (a instanceof Integer) {
            total = (T) ((Integer) (a.intValue() + b.intValue()));
        } else if (a instanceof Double) {
            total = (T) ((Double) (a.doubleValue() + b.doubleValue()));
        } else if (a instanceof Float) {
            total = (T) ((Float) (a.floatValue() + b.floatValue()));
        } else if (a instanceof Long) {
            total = (T) ((Long) (a.longValue() + b.longValue()));
        } else {
            throw new UnsupportedOperationException("Cannot add " + a.getClass().getName() + " types !");
        }

        return total;
    }

    private T subtract(T a, T b) {
        T total;
        if (a instanceof Integer) {
            total = (T) ((Integer) (a.intValue() - b.intValue()));
        } else if (a instanceof Double) {
            total = (T) ((Double) (a.doubleValue() - b.doubleValue()));
        } else if (a instanceof Float) {
            total = (T) ((Float) (a.floatValue() - b.floatValue()));
        } else if (a instanceof Long) {
            total = (T) ((Long) (a.longValue() - b.longValue()));
        } else {
            throw new UnsupportedOperationException("Cannot subtract " + a.getClass().getName() + " types !");
        }

        return total;
    }

    @Override
    public void reset() {
        setValue(getDefaultValue());
    }

    @Override
    public void addRestriction(String key, ValueRestriction<T> restriction) {
        super.addRestriction(key, restriction);
        if (restriction.isType(RestrictionType.LOCKED_VALUE) && !restriction.getValue().equals(getValue())) {
            setValue(restriction.getValue());
            return;
        }
        setValue(getValue());
    }
}
