package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.BooleanValue;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;

public class BooleanValueImpl extends Value<Boolean> implements BooleanValue {

    private final String  nameKey;
    private final String  yesDescriptionKey;
    private final String  noDescriptionKey;
    private final boolean defaultValue;

    private boolean value;

    public BooleanValueImpl(String nameKey, String yesDescriptionKey, String noDescriptionKey, boolean value) {
        this.nameKey = nameKey;
        this.yesDescriptionKey = yesDescriptionKey;
        this.noDescriptionKey = noDescriptionKey;
        this.value = value;
        this.defaultValue = this.value;
    }

    @Override
    public String getNameKey() {
        return this.nameKey;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public void setValue(Boolean value) {
        boolean oldValue = this.value;
        this.value = value != null && value;
        onValueChanged(oldValue, this.value);
    }

    @Override
    public boolean isEdited() {
        return getValue() != getDefaultValue();
    }

    @Override
    public Boolean getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String getDescriptionKey() {
        return getValue() ? this.yesDescriptionKey : this.noDescriptionKey;
    }

    @Override
    public boolean getBooleanValue() {
        return this.value;
    }

    @Override
    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public void reset() {
        setValue(getDefaultValue());
    }

    @Override
    public void addRestriction(String key, ValueRestriction<Boolean> valueRestriction) {
        super.addRestriction(key, valueRestriction);
        if (valueRestriction.isType(RestrictionType.LOCKED_VALUE) && valueRestriction.getValue() != getValue())
            toggle();
    }
}
