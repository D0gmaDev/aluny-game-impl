package fr.aluny.gameimpl.value;


import fr.aluny.gameapi.value.NumericValue;
import fr.aluny.gameapi.value.ValueRestriction;
import fr.aluny.gameapi.value.ValueRestriction.RestrictionType;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unchecked")
public sealed class NumericValueImpl<T extends Number & Comparable<T>> extends Value<T> implements NumericValue<T> permits TimeValueImpl {

    private static final DecimalFormat DOUBLE_DECIMAL = new DecimalFormat("#.##");

    private static final Map<Class<? extends Number>, Operations<?>> OPERATIONS = Map.of(
            Integer.class, new Operations<>(Integer::sum, (a, b) -> a - b, i -> Integer.toString(i)),
            Long.class, new Operations<>(Long::sum, (a, b) -> a - b, l -> Long.toString(l)),
            Double.class, new Operations<>(Double::sum, (a, b) -> a - b, DOUBLE_DECIMAL::format),
            Float.class, new Operations<>(Float::sum, (a, b) -> a - b, DOUBLE_DECIMAL::format)
    );

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
        this.nameKey = Objects.requireNonNull(nameKey);
        this.descriptionKey = descriptionKey;
        this.defaultMaxValue = Objects.requireNonNull(maxValue);
        this.defaultMinValue = Objects.requireNonNull(minValue);
        this.smallStep = Objects.requireNonNull(smallStep);
        this.mediumStep = Objects.requireNonNull(mediumStem);
        this.largeStep = Objects.requireNonNull(largeStep);

        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.value = defaultValue;

        addRestriction(RestrictionType.MINIMAL_VALUE, minValue);
        addRestriction(RestrictionType.MAXIMAL_VALUE, maxValue);
    }

    @Override
    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    @Override
    public String getStringValue() {
        return Optional.ofNullable(OPERATIONS.get(getValue().getClass()))
                .map(operations -> (Operations<T>) operations).map(Operations::toStringOperation)
                .orElse(Object::toString).toString(getValue());
    }

    @Override
    public T getMaxValue() {
        return getRestrictions().stream().filter(restriction -> restriction.isType(RestrictionType.MAXIMAL_VALUE))
                .map(ValueRestriction::value).min(Comparator.naturalOrder()).orElse(this.defaultMaxValue);
    }

    @Override
    public T getMinValue() {
        return getRestrictions().stream().filter(restriction -> restriction.isType(RestrictionType.MINIMAL_VALUE))
                .map(ValueRestriction::value).max(Comparator.naturalOrder()).orElse(this.defaultMinValue);
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
        Objects.requireNonNull(value);

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
        return a.compareTo(b) > 0;
    }

    private boolean isInferior(T a, T b) {
        return a.compareTo(b) < 0;
    }

    private T add(T a, T b) {
        Operations<T> operation = (Operations<T>) OPERATIONS.get(a.getClass());

        if (operation == null)
            throw new UnsupportedOperationException("Cannot add " + a.getClass().getName() + " types !");

        return operation.addOperation().add(a, b);
    }

    private T subtract(T a, T b) {
        Operations<T> operation = (Operations<T>) OPERATIONS.get(a.getClass());

        if (operation == null)
            throw new UnsupportedOperationException("Cannot add " + a.getClass().getName() + " types !");

        return operation.removeOperation().remove(a, b);
    }

    @Override
    public void reset() {
        setValue(getDefaultValue());
    }

    @Override
    public boolean canApply(ValueRestriction<T> restriction) {
        boolean unlocked = super.canApply(restriction);

        if (!unlocked)
            return false;

        return restriction.isType(RestrictionType.MINIMAL_VALUE) && restriction.value().compareTo(getMaxValue()) <= 0 ||
                restriction.isType(RestrictionType.MAXIMAL_VALUE) && restriction.value().compareTo(getMinValue()) >= 0;
    }

    @Override
    public void addRestriction(ValueRestriction<T> restriction) {
        super.addRestriction(restriction);

        if (restriction.isType(RestrictionType.LOCKED_VALUE) && !restriction.value().equals(getValue())) {
            setValue(restriction.value());
            return;
        }
        setValue(getValue());
    }

    private interface AddOperation<N> {

        N add(N n1, N n2);
    }

    private interface RemoveOperation<N> {

        N remove(N n1, N n2);
    }

    private interface ToStringOperation<N> {

        String toString(N n);
    }

    private record Operations<N>(AddOperation<N> addOperation, RemoveOperation<N> removeOperation, ToStringOperation<N> toStringOperation) {

    }

    @Override
    public String toString() {
        return "NumericValue<" + value.getClass().getSimpleName() + ">(" + getStringValue() + ')';
    }
}
