package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.TimeValue;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class TimeValueImpl extends NumericValueImpl<Long> implements TimeValue {

    private static final SimpleDateFormat HOUR_DATE_FORMAT     = new SimpleDateFormat("HH'h' mm'm' ss's'");
    private static final SimpleDateFormat MINUTE_DATE_FORMAT   = new SimpleDateFormat("mm'm' ss's'");
    private static final SimpleDateFormat SECOND_DATE_FORMAT   = new SimpleDateFormat("ss's'");
    private static final SimpleDateFormat SECOND_2_DATE_FORMAT = new SimpleDateFormat("s's'");

    static {
        TimeZone timeZone = TimeZone.getTimeZone("GMT");

        HOUR_DATE_FORMAT.setTimeZone(timeZone);
        MINUTE_DATE_FORMAT.setTimeZone(timeZone);
        SECOND_DATE_FORMAT.setTimeZone(timeZone);
        SECOND_2_DATE_FORMAT.setTimeZone(timeZone);
    }

    private final TimeUnit timeUnit;

    public TimeValueImpl(String nameKey, String descriptionKey, long defaultValue, long minValue, long maxValue, long smallStep, long mediumStem, long largeStep, TimeUnit timeUnit) {
        super(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep);

        this.timeUnit = Objects.requireNonNull(timeUnit);
    }

    @Override
    public long getLongValue() {
        return getValue();
    }

    @Override
    public String getFormattedTime() {
        return getDateFormat(getLongValue()).format(TimeUnit.MILLISECONDS.convert(getValue(), this.timeUnit));
    }

    @Override
    public String getFormattedSmallStep() {
        return getDateFormat(getSmallStep()).format(TimeUnit.MILLISECONDS.convert(getSmallStep(), this.timeUnit));
    }

    @Override
    public String getFormattedMediumStep() {
        return getDateFormat(getMediumStep()).format(TimeUnit.MILLISECONDS.convert(getMediumStep(), this.timeUnit));
    }

    @Override
    public String getFormattedLargeStep() {
        return getDateFormat(this.getLargeStep()).format(TimeUnit.MILLISECONDS.convert(getLargeStep(), this.timeUnit));
    }

    private SimpleDateFormat getDateFormat(long value) {
        SimpleDateFormat simpleDateFormat;

        if (TimeUnit.MINUTES.convert(value, this.timeUnit) >= 60)
            simpleDateFormat = HOUR_DATE_FORMAT;
        else if (TimeUnit.SECONDS.convert(value, this.timeUnit) >= 60)
            simpleDateFormat = MINUTE_DATE_FORMAT;
        else if (TimeUnit.SECONDS.convert(value, this.timeUnit) >= 10)
            simpleDateFormat = SECOND_DATE_FORMAT;
        else
            simpleDateFormat = SECOND_2_DATE_FORMAT;

        return simpleDateFormat;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    @Override
    public Duration toDuration() {
        return Duration.of(getLongValue(), this.timeUnit.toChronoUnit());
    }

    @Override
    public String toString() {
        return "TimeValue(" + getStringValue() + ", " + timeUnit.toString() + ')';
    }
}
