package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.Timer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class TimerImpl implements Timer {

    private static final String HOURS_DATE_FORMAT   = "%02dh %02dm %02ds";
    private static final String MINUTES_DATE_FORMAT = "%02dm %02ds";
    private static final String SECONDS_DATE_FORMAT = "%02ds";

    private final String   key;
    private final Duration step;
    private       Long     stop; // can be null for endless timers

    private final Consumer<Timer> runOnTick;
    private final Consumer<Timer> runOnEnd;

    private final Many<Timer>    timerMany;
    private final Scheduler      scheduler;
    private final List<Runnable> endTasks = new ArrayList<>();

    private long            value = 0L;
    private java.util.Timer timer;
    private boolean         ended = false;

    public TimerImpl(String key, Duration step, Duration stop, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {
        if (step == null || step.isZero() || step.isNegative())
            throw new IllegalArgumentException("step must be strictly positive.");

        this.key = key;
        this.step = step;
        this.stop = stop != null ? stop.dividedBy(step) : null;

        this.runOnTick = runOnTick;
        this.runOnEnd = runOnEnd;

        this.timerMany = Sinks.many().replay().limit(Duration.ofSeconds(1));
        this.scheduler = Schedulers.boundedElastic();
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

                if (stop != null && value >= stop) {
                    ended = true;
                    timer.cancel();
                    if (runOnEnd != null)
                        runOnEnd.accept(instance);
                    endTasks.forEach(Runnable::run);
                    timer = null;
                }
            }
        }, this.step.toMillis(), this.step.toMillis());
    }

    @Override
    public void stop() {
        if (timer == null)
            throw new IllegalStateException("the timer is not running.");

        this.timer.cancel();
    }

    @Override
    public String getIncreasingFormattedValue() {
        Duration duration = this.step.multipliedBy(this.value);
        return formatDuration(duration);
    }

    @Override
    public String getDecreasingFormattedValue() {
        if (this.stop == null)
            return "∞";

        Duration duration = this.step.multipliedBy(this.stop - this.value);
        return formatDuration(duration);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0)
            return String.format(HOURS_DATE_FORMAT, hours, minutes, seconds);
        if (minutes > 0)
            return String.format(MINUTES_DATE_FORMAT, minutes, seconds);

        return String.format(SECONDS_DATE_FORMAT, seconds);
    }

    @Override
    public Flux<Timer> onTick() {
        return this.timerMany.asFlux().publishOn(this.scheduler);
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
        this.stop = value == Long.MAX_VALUE ? null : value;
    }

    @Override
    public void reset() {
        this.value = 0L;
        this.ended = false;
    }

    @Override
    public String toString() {
        return "TimerImpl{" +
                "key='" + key + '\'' +
                ", step=" + step +
                ", stop=" + stop +
                ", value=" + value +
                ", ended=" + ended +
                '}';
    }
}
