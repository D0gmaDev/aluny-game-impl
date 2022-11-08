package fr.aluny.gameimpl.value;

import fr.aluny.gameapi.value.TimeValue;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeValueImpl extends NumericValueImpl<Long> implements TimeValue {

    private static final SimpleDateFormat HOUR_DATE_FORMAT     = new SimpleDateFormat("HH'h' mm'm' ss's'");
    private static final SimpleDateFormat MINUTE_DATE_FORMAT   = new SimpleDateFormat("mm'm' ss's'");
    private static final SimpleDateFormat SECOND_DATE_FORMAT   = new SimpleDateFormat("ss's'");
    private static final SimpleDateFormat SECOND_2_DATE_FORMAT = new SimpleDateFormat("s's'");

    static {
        HOUR_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        MINUTE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        SECOND_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        SECOND_2_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final TimeUnit timeUnit;

    public TimeValueImpl(String nameKey, String descriptionKey, Long defaultValue, Long minValue, Long maxValue, Long smallStep, Long mediumStem, Long largeStep, TimeUnit timeUnit) {
        super(nameKey, descriptionKey, defaultValue, minValue, maxValue, smallStep, mediumStem, largeStep);

        this.timeUnit = timeUnit;
    }

    @Override
    public String getFormattedTime() {
        return getDateFormat(this.getValue()).format(TimeUnit.MILLISECONDS.convert(this.getValue(), this.timeUnit));
    }

    @Override
    public String getFormattedSmallStep() {
        return getDateFormat(this.getSmallStep()).format(TimeUnit.MILLISECONDS.convert(this.getSmallStep(), this.timeUnit));
    }

    @Override
    public String getFormattedMediumStep() {
        return getDateFormat(this.getMediumStep()).format(TimeUnit.MILLISECONDS.convert(this.getMediumStep(), this.timeUnit));
    }

    @Override
    public String getFormattedLargeStep() {
        return getDateFormat(this.getLargeStep()).format(TimeUnit.MILLISECONDS.convert(this.getLargeStep(), this.timeUnit));
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
}
