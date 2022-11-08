package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.Timer;
import fr.aluny.gameapi.timer.TimerService;
import fr.aluny.gameapi.value.TimeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TimerServiceImpl implements TimerService {

    private static final Map<String, TimerImpl> TIMERS = new HashMap<>();

    @Override
    public TimerImpl registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Runnable runOnTick, Runnable runOnEnd) {
        TimerImpl timer = new TimerImpl(delay, step, stop, timeUnit, runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public TimerImpl createTimerFromTimeValue(String key, TimeValue step, TimeValue end, Runnable runOnTick, Runnable runOnEnd) {
        long endDelay = end == null ? Long.MAX_VALUE : end.getValue();

        if (end != null)
            endDelay = step.getTimeUnit().convert(endDelay, end.getTimeUnit());

        TimerImpl timer = new TimerImpl(step.getValue(), step.getValue(), endDelay, step.getTimeUnit(), runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public Timer registerTimer(String key, Long delay, Long step, Long stop, TimeUnit timeUnit, Consumer<Long> runOnTick, Consumer<Long> runOnEnd) {
        TimerImpl timer = new TimerImpl(delay, step, stop, timeUnit, runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public Timer createTimerFromTimeValue(String key, TimeValue step, TimeValue end, Consumer<Long> runOnTick, Consumer<Long> runOnEnd) {
        long endDelay = end == null ? Long.MAX_VALUE : end.getValue();

        if (end != null)
            endDelay = step.getTimeUnit().convert(endDelay, end.getTimeUnit());

        TimerImpl timer = new TimerImpl(step.getValue(), step.getValue(), endDelay, step.getTimeUnit(), runOnTick, runOnEnd);

        TIMERS.put(key, timer);
        return timer;
    }

    @Override
    public Optional<Timer> getTimer(String key) {
        return Optional.ofNullable(TIMERS.get(key));
    }

    @Override
    public boolean unregisterTimer(String key) {
        return TIMERS.remove(key) != null;
    }

}
