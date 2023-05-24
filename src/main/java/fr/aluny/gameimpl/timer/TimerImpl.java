package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.Timer;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class TimerImpl implements Timer {

    private static final SimpleDateFormat HOURS_DATE_FORMAT   = new SimpleDateFormat("HH'h' mm'm' ss's'");
    private static final SimpleDateFormat MINUTES_DATE_FORMAT = new SimpleDateFormat("mm'm' ss's'");
    private static final SimpleDateFormat SECONDS_DATE_FORMAT = new SimpleDateFormat("ss's'");

    static {
        HOURS_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        MINUTES_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        SECONDS_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final long     delay;
    private final long     step;
    private final TimeUnit timeUnit;

    private Long stop;

    private final LongConsumer runOnTick;
    private final LongConsumer runOnEnd;

    private final Many<Timer>    timerMany;
    private final Scheduler      scheduler;
    private final List<Runnable> endTasks = new ArrayList<>();

    private long            value = 0L;
    private java.util.Timer timer;
    private boolean         ended = false;

    public TimerImpl(Long delay, Long step, Long stop, TimeUnit timeUnit, Runnable runOnTick, Runnable runOnEnd) {
        this(delay, step, stop, timeUnit, (runOnTick != null) ? l -> runOnTick.run() : null, (runOnEnd != null) ? l -> runOnEnd.run() : null);
    }

    public TimerImpl(Long delay, Long step, Long stop, TimeUnit timeUnit, LongConsumer runOnTick, LongConsumer runOnEnd) {

        if (step == null || step <= 0L)
            throw new IllegalArgumentException("step must be a positive long.");

        this.delay = delay == null ? 0L : delay;
        this.step = step;
        this.stop = stop;
        this.timeUnit = timeUnit;
        this.runOnTick = runOnTick;
        this.runOnEnd = runOnEnd;

        this.timerMany = Sinks.many().replay().limit(Duration.ofSeconds(1));
        scheduler = Schedulers.boundedElastic();
    }

    @Override
    public void start() {
        if (timer != null)
            throw new IllegalStateException("the timer is already running.");

        Timer instance = this;

        this.timer = new java.util.Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {

                value++;

                if (runOnTick != null)
                    runOnTick.accept(value);

                timerMany.emitNext(instance, EmitFailureHandler.FAIL_FAST);

                if (stop != null && value >= stop / step) {
                    ended = true;
                    timer.cancel();
                    if (runOnEnd != null)
                        runOnEnd.accept(value);
                    endTasks.forEach(Runnable::run);
                    timer = null;
                }
            }
        }, TimeUnit.MILLISECONDS.convert(this.delay, this.timeUnit), TimeUnit.MILLISECONDS.convert(this.step, this.timeUnit));
    }

    @Override
    public void stop() {
        if (timer == null)
            throw new IllegalStateException("the timer is not running.");

        this.timer.cancel();
    }

    @Override
    public String getIncreasingFormattedValue() {
        SimpleDateFormat simpleDateFormat = this.value >= 3600 ? HOURS_DATE_FORMAT : (this.value >= 60 ? MINUTES_DATE_FORMAT : SECONDS_DATE_FORMAT);
        return simpleDateFormat.format(this.value * 1000);
    }

    @Override
    public String getDecreasingFormattedValue() {
        if (this.stop == null)
            return "∞";

        long value = TimeUnit.SECONDS.convert(this.stop, this.timeUnit) - this.value;

        SimpleDateFormat simpleDateFormat = value >= 3600 ? HOURS_DATE_FORMAT : (value >= 60 ? MINUTES_DATE_FORMAT : SECONDS_DATE_FORMAT);
        return simpleDateFormat.format(value * 1000);
    }

    @Override
    public Flux<Timer> onTick() {
        return timerMany.asFlux().publishOn(scheduler);
    }

    @Override
    public void addEndTask(Runnable consumer) {
        this.endTasks.add(consumer);
    }

    @Override
    public void setValue(long l) {
        this.value = l;
    }

    @Override
    public long getMaxValue() {
        return this.stop != null ? this.stop : Long.MAX_VALUE;
    }

    @Override
    public boolean isEnded() {
        return this.ended;
    }

    @Override
    public long getValue() {
        return this.value;
    }

    @Override
    public void setMaxValue(long value) {
        this.stop = value;
    }

    @Override
    public void reset() {
        this.value = 0L;
        this.ended = false;
    }
}
