package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.Timer;
import fr.aluny.gameapi.timer.TimerService;
import fr.aluny.gameapi.value.TimeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class TimerServiceImpl implements TimerService {

    private static final Map<String, TimerImpl> TIMERS = new HashMap<>();

    @Override
    public TimerImpl registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Runnable runOnTick, Runnable runOnEnd) {
        TimerImpl timer = new TimerImpl(key, delay, step, stop, timeUnit, runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public TimerImpl registerTimerFromTimeValue(String key, TimeValue step, TimeValue stop, Runnable runOnTick, Runnable runOnEnd) {
        Long stopLong = stop != null ? step.getTimeUnit().convert(stop.getLongValue(), stop.getTimeUnit()) : null;
        return registerTimer(key, 0L, step.getLongValue(), stopLong, step.getTimeUnit(), runOnTick, runOnEnd);
    }

    @Override
    public Timer registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, LongConsumer runOnTick, LongConsumer runOnEnd) {
        TimerImpl timer = new TimerImpl(key, delay, step, stop, timeUnit, runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public Timer registerTimerFromTimeValue(String key, TimeValue step, TimeValue stop, LongConsumer runOnTick, LongConsumer runOnEnd) {
        Long stopLong = stop != null ? step.getTimeUnit().convert(stop.getLongValue(), stop.getTimeUnit()) : null;
        return registerTimer(key, 0L, step.getLongValue(), stopLong, step.getTimeUnit(), runOnTick, runOnEnd);
    }

    @Override
    public Timer registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {
        TimerImpl timer = new TimerImpl(key, delay, step, stop, timeUnit, runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public Timer registerTimerFromTimeValue(String key, TimeValue step, TimeValue stop, Consumer<Timer> runOnTick, Consumer<Timer> runOnEnd) {
        Long stopLong = stop != null ? step.getTimeUnit().convert(stop.getLongValue(), stop.getTimeUnit()) : null;
        return registerTimer(key, 0L, step.getLongValue(), stopLong, step.getTimeUnit(), runOnTick, runOnEnd);
    }

    @Override
    public Optional<Timer> getTimer(String key) {
        return Optional.ofNullable(TIMERS.get(key));
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
