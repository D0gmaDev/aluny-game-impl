package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.Timer;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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

    private final String key;

    private final long     delay;
    private final long     step;
    private final TimeUnit timeUnit;

    private Long stop;

    private final Consumer<Timer> runOnTick;
    private final Consumer<Timer> runOnEnd;

    private final Many<Timer>    timerMany;
    private final Scheduler      scheduler;
    private final List<Runnable> endTasks = new ArrayList<>();

    private long            value = 0L;
    private java.util.Timer timer;
    private boolean         ended = false;

    public TimerImpl(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Runnable runOnTick, Runnable runOnEnd) {
        this(key, delay, step, stop, timeUnit, (runOnTick != null) ? ((Consumer<Timer>) timer -> runOnTick.run()) : null, (runOnEnd != null) ? timer -> runOnEnd.run() : null);
    }

    public TimerImpl(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, LongConsumer runOnTick, LongConsumer runOnEnd) {
        this(key, delay, step, stop, timeUnit, (runOnTick != null) ? ((Consumer<Timer>) timer -> runOnTick.accept(timer.getValue())) : null, (runOnEnd != null) ? timer -> runOnEnd.accept(timer.getValue()) : null);
    }

    public TimerImpl(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {

        if (step == null || step <= 0L)
            throw new IllegalArgumentException("step must be a positive long.");

        this.key = key;
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
    public String getKey() {
        return this.key;
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
                    runOnTick.accept(instance);

                timerMany.emitNext(instance, EmitFailureHandler.FAIL_FAST);

                if (stop != null && value >= stop / step) {
                    ended = true;
                    timer.cancel();
                    if (runOnEnd != null)
                        runOnEnd.accept(instance);
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
