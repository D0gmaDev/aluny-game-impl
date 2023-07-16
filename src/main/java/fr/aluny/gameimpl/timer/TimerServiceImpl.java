package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.Timer;
import fr.aluny.gameapi.timer.TimerService;
import fr.aluny.gameapi.value.TimeValue;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class TimerServiceImpl implements TimerService {

    private static final Map<String, Timer> TIMERS = new HashMap<>();

    @Override
    public Timer registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Runnable runOnTick, Runnable runOnEnd) {
        LongConsumer tickConsumer = runOnTick != null ? l -> runOnTick.run() : null;
        LongConsumer endConsumer = runOnEnd != null ? l -> runOnEnd.run() : null;

        return registerValueTimer(key, step, stop, timeUnit, tickConsumer, endConsumer);
    }

    @Override
    public Timer registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, LongConsumer runOnTick, LongConsumer runOnEnd) {
        return registerValueTimer(key, step, stop, timeUnit, runOnTick, runOnEnd);
    }

    @Override
    public Timer createTimer(String key, Duration step, Duration stop, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {
        return new TimerImpl(key, step, stop, runOnTick, runOnEnd);
    }

    @Override
    public Timer registerTimer(String key, Duration step, Duration stop, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {
        Timer timer = createTimer(key, step, stop, runOnTick, runOnEnd);
        registerTimer(timer);
        return timer;
    }

    @Override
    public Timer registerRunnableTimer(String key, Duration step, Duration stop, Runnable runOnTick, Runnable runOnEnd) {
        Consumer<Timer> timerRunOnTick = runOnTick != null ? timer -> runOnTick.run() : null;
        Consumer<Timer> timerRunOnEnd = runOnEnd != null ? timer -> runOnEnd.run() : null;

        return registerTimer(key, step, stop, timerRunOnTick, timerRunOnEnd);
    }

    @Override
    public Timer registerValueTimer(String key, long step, Long stop, TimeUnit timeUnit, LongConsumer runOnTick, LongConsumer runOnEnd) {
        Consumer<Timer> timerRunOnTick = runOnTick != null ? timer -> runOnTick.accept(timer.getValue()) : null;
        Consumer<Timer> timerRunOnEnd = runOnEnd != null ? timer -> runOnEnd.accept(timer.getValue()) : null;

        return registerTimer(key, Duration.of(step, timeUnit.toChronoUnit()), stop != null ? Duration.of(stop, timeUnit.toChronoUnit()) : null, timerRunOnTick, timerRunOnEnd);
    }

    @Override
    public Timer registerTimerFromTimeValue(String key, TimeValue step, TimeValue stop, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {
        Duration stopDuration = stop != null ? stop.toDuration() : null;
        return registerTimer(key, step.toDuration(), stopDuration, runOnTick, runOnEnd);
    }

    @Override
    public Optional<Timer> getTimer(String key) {
        return Optional.ofNullable(TIMERS.get(key));
    }

    @Override
    public void registerTimer(Timer timer) {
        TIMERS.put(timer.getKey(), timer);
    }

    @Override
    public boolean unregisterTimer(String key) {
        return TIMERS.remove(key) != null;
    }

    @Override
    public boolean unregisterTimer(Timer timer) {
        return unregisterTimer(timer.getKey());
    }
}
