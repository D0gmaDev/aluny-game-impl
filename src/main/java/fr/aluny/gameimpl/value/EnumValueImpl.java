package fr.aluny.gameimpl.value;

import com.google.common.base.Preconditions;
import fr.aluny.gameapi.value.EnumValue;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.util.Arrays;

public class EnumValueImpl<T extends Enum<T>> extends Value<T> implements EnumValue<T> {

    private final String   nameKey;
    private final String[] descriptionKeys;
    private final T[]      enumeration;
    private final int      defaultValueIndex;

    private int valueIndex;

    public EnumValueImpl(String nameKey, Class<T> enumerationClass, T defaultValue, String... descriptionKeys) {
        Preconditions.checkState(enumerationClass.getEnumConstants().length == descriptionKeys.length, "descriptionKeys length must be the same as enumerationClass one !");

        this.nameKey = nameKey;
        this.enumeration = enumerationClass.getEnumConstants();
        this.valueIndex = Arrays.asList(this.enumeration).indexOf(defaultValue);
        this.defaultValueIndex = this.valueIndex;
        this.descriptionKeys = descriptionKeys;
    }

    @Override
    public void increment() {
        setIndexValue((getValueIndex() + 1) % this.enumeration.length);
    }

    @Override
    public void decrement() {
        setIndexValue((getValueIndex() - 1 + this.enumeration.length) % this.enumeration.length);
    }

    @Override
    public String getSelectedDescriptionKey() {
        return this.descriptionKeys[getValueIndex()];
    }

    @Override
    public int getValueIndex() {
        return this.valueIndex;
    }

    @Override
    public String[] getDescriptionKeys() {
        return this.descriptionKeys;
    }

    @Override
    public T[] getEnumeration() {
        return this.enumeration;
    }

    @Override
    public void setIndexValue(int index) {
        if (index < 0 || index >= this.enumeration.length)
            throw new IllegalStateException("Value is incorrect for the given enum");
        int oldIndex = this.valueIndex;
        this.valueIndex = index;
        onValueChanged(this.enumeration[oldIndex], this.enumeration[index]);
    }

    @Override
    public int getDefaultValueIndex() {
        return this.defaultValueIndex;
    }

    @Override
    public String getNameKey() {
        return this.nameKey;
    }

    @Override
    public T getValue() {
        return this.enumeration[getValueIndex()];
    }

    @Override
    public void setValue(T value) {
        setIndexValue(Arrays.asList(this.enumeration).indexOf(value));
    }

    @Override
    public boolean isEdited() {
        return getValueIndex() != getDefaultValueIndex();
    }

    @Override
    public T getDefaultValue() {
        return this.enumeration[getDefaultValueIndex()];
    }

    @Override
    public void reset() {
        setIndexValue(getDefaultValueIndex());
    }

    @Override
    public void addRestriction(String key, ValueRestriction<T> restriction) {
        super.addRestriction(key, restriction);
        if (restriction.isType(RestrictionType.LOCKED_VALUE) && getValue() != restriction.getValue())
            setValue(restriction.getValue());
    }
}
