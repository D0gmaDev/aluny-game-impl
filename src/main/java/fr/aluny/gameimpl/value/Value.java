package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.GeneralValue;
import fr.aluny.gameapi.value.IOnValueChanged;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract sealed class Value<T> implements GeneralValue<T> permits BooleanValueImpl, EnumValueImpl, NumericValueImpl, StringValueImpl {

    private final List<IOnValueChanged<T>>  subscribers  = new ArrayList<>();
    private final List<ValueRestriction<T>> restrictions = new ArrayList<>();

    @Override
    public IOnValueChanged<T> subscribeOnValueChanged(IOnValueChanged<T> sub) {
        if (!subscribers.contains(sub))
            subscribers.add(sub);
        return sub;
    }

    @Override
    public boolean unsubscribeOnValueChanged(IOnValueChanged<T> sub) {
        return subscribers.remove(sub);
    }

    protected void onValueChanged(T oldValue, T newValue) {
        // Don't propagate if there are no updates
        if (Objects.equals(oldValue, newValue))
            return;

        for (IOnValueChanged<T> sub : subscribers)
            sub.valueChanged(oldValue, newValue);
    }

    @Override
    public ValueRestriction<T> addRestriction(RestrictionType type, T value) {
        ValueRestriction<T> restriction = new ValueRestriction<>(UUID.randomUUID(), type, value);
        addRestriction(restriction);
        return restriction;
    }

    @Override
    public void addRestriction(ValueRestriction<T> restriction) {
        if (canApply(restriction) && !restrictions.contains(restriction))
            restrictions.add(restriction);
    }

    @Override
    public void removeRestriction(ValueRestriction<T> restriction) {
        restrictions.remove(restriction);
    }

    @Override
    public boolean canApply(ValueRestriction<T> restriction) {
        if (restriction.type().equals(RestrictionType.LOCKED_VALUE))
            return !isLocked() || Objects.equals(restriction.value(), getValue());

        return true;
    }

    @Override
    public boolean isLocked() {
        return restrictions.stream().anyMatch(restriction -> restriction.isType(RestrictionType.LOCKED_VALUE));
    }

    protected List<ValueRestriction<T>> getRestrictions() {
        return Collections.unmodifiableList(restrictions);
    }
}
