package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.StringValue;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.util.Objects;

public class StringValueImpl extends Value<String> implements StringValue {

    private final String nameKey;
    private final String descriptionKey;
    private final String defaultValue;

    private final int minLength;
    private final int maxLength;

    private String value;

    public StringValueImpl(String nameKey, String descriptionKey, String value, int minLength, int maxLength) {
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.value = value;
        this.defaultValue = this.value;
        this.minLength = minLength;
        this.maxLength = maxLength;
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
    public void addRestriction(String key, ValueRestriction<String> restriction) {
        super.addRestriction(key, restriction);
        if (restriction.isType(RestrictionType.LOCKED_VALUE) && !restriction.getValue().equals(getValue()))
            setValue(restriction.getValue());
    }
}
