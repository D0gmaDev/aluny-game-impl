package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.StringValue;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.util.Objects;

public final class StringValueImpl extends Value<String> implements StringValue {

    private final String nameKey;
    private final String descriptionKey;
    private final String defaultValue;

    private final int minLength;
    private final int maxLength;

    private String value;

    public StringValueImpl(String nameKey, String descriptionKey, String defaultValue, int minLength, int maxLength) {
        this.nameKey = Objects.requireNonNull(nameKey);
        this.descriptionKey = descriptionKey;
        this.minLength = minLength;
        this.maxLength = maxLength;

        if (defaultValue == null || defaultValue.length() < minLength || defaultValue.length() > maxLength)
            throw new IllegalArgumentException("default value must be of acceptable length.");

        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String getNameKey() {
        return this.nameKey;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        if (value.length() < getMinLength())
            value += " ".repeat(getMinLength() - value.length());
        if (value.length() > getMaxLength())
            value = value.substring(0, getMaxLength());

        String oldValue = this.value;
        this.value = value;
        onValueChanged(oldValue, value);
    }

    @Override
    public boolean isEdited() {
        return !Objects.equals(getValue(), getDefaultValue());
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    @Override
    public int getMinLength() {
        return this.minLength;
    }

    @Override
    public int getMaxLength() {
        return this.maxLength;
    }

    @Override
    public void reset() {
        setValue(getDefaultValue());
    }

    @Override
    public void addRestriction(ValueRestriction<String> restriction) {
        super.addRestriction(restriction);

        if (restriction.isType(RestrictionType.LOCKED_VALUE) && !restriction.value().equals(getValue()))
            setValue(restriction.value());
    }

    @Override
    public String toString() {
        return "StringValue(" + value + ')';
    }
}
