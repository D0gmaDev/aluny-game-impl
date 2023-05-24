package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.RunnableHelper;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class RunnableHelperImpl implements RunnableHelper {

    private static final IntPredicate FALSE_INT_PREDICATE = i -> false;

    private final JavaPlugin plugin;

    public RunnableHelperImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runSynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runAsynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runLaterSynchronously(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    @Override
    public void runLaterAsynchronously(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    @Override
    public BukkitTask runTimerSynchronously(Runnable runnable, long delay, long interval) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, interval);
    }

    @Override
    public BukkitTask runTimerAsynchronously(Runnable runnable, long delay, long interval) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, interval);
    }

    @Override
    public void runTimerSynchronously(Consumer<BukkitTask> task, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, interval);
    }

    @Override
    public void runTimerAsynchronously(Consumer<BukkitTask> task, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, interval);
    }

    @Override
    public void runRepeatSynchronously(IntConsumer runnable, IntPredicate earlyCancel, long delay, long interval, int occurrences) {
        if (occurrences <= 0)
            throw new IllegalArgumentException("the number of occurrences must be strictly greater than 0 (" + occurrences + ").");

        AtomicInteger occurrencesCounter = new AtomicInteger();

        runTimerSynchronously(bukkitTask -> {

            int currentOccurrence = occurrencesCounter.getAndIncrement();

            if (currentOccurrence + 1 == occurrences || earlyCancel.test(currentOccurrence))
                bukkitTask.cancel();

            runnable.accept(currentOccurrence);

        }, delay, interval);
    }

    @Override
    public void runRepeatAsynchronously(IntConsumer runnable, IntPredicate earlyCancel, long delay, long interval, int occurrences) {
        if (occurrences <= 0)
            throw new IllegalArgumentException("the number of occurrences must be strictly greater than 0 (" + occurrences + ").");

        AtomicInteger occurrencesCounter = new AtomicInteger();

        runTimerAsynchronously(bukkitTask -> {

            int currentOccurrence = occurrencesCounter.getAndIncrement();

            if (currentOccurrence + 1 == occurrences || earlyCancel.test(currentOccurrence))
                bukkitTask.cancel();

            runnable.accept(currentOccurrence);

        }, delay, interval);
    }

    @Override
    public void runRepeatSynchronously(IntConsumer runnable, long delay, long interval, int occurrences) {
        runRepeatSynchronously(runnable, FALSE_INT_PREDICATE, delay, interval, occurrences);
    }

    @Override
    public void runRepeatAsynchronously(IntConsumer runnable, long delay, long interval, int occurrences) {
        runRepeatAsynchronously(runnable, FALSE_INT_PREDICATE, delay, interval, occurrences);
    }
}
