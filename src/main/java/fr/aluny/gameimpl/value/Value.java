package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.IOnValueChanged;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Value<T> {

    private final List<IOnValueChanged<T>> subscribers = Collections.synchronizedList(new ArrayList<>());

    private final Map<String, ValueRestriction<T>> restrictions = Collections.synchronizedMap(new HashMap<>());

    public abstract void reset();

    public IOnValueChanged<T> subscribeOnValueChanged(IOnValueChanged<T> sub) {
        if (!subscribers.contains(sub))
            subscribers.add(sub);
        return sub;
    }

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

    public void addRestriction(String key, RestrictionType type, T value) {
        addRestriction(key, new ValueRestriction<>(type, value));
    }

    public void addRestriction(String key, ValueRestriction<T> restriction) {
        restrictions.put(key, restriction);
    }

    public void removeRestriction(String key) {
        restrictions.remove(key);
    }

    public boolean isLocked() {
        return restrictions.values().stream().anyMatch(restriction -> restriction.isType(RestrictionType.LOCKED_VALUE));
    }

    protected List<ValueRestriction<T>> getRestrictions() {
        return new ArrayList<>(restrictions.values());
    }
}
