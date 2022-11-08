package fr.aluny.gameimpl.timer;

import fr.aluny.gameapi.timer.RunnableHelper;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class RunnableHelperImpl implements RunnableHelper {

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
    public BukkitTask runRepeatSynchronously(IntConsumer runnable, long delay, long interval, int occurrences) {
        if (occurrences <= 0)
            throw new IllegalArgumentException("the number of occurrences must be strictly greater than 0 (" + occurrences + ").");

        AtomicInteger occurrencesCounter = new AtomicInteger();
        AtomicReference<BukkitTask> taskReference = new AtomicReference<>();
        taskReference.set(runTimerSynchronously(() -> {

            int currentOccurrence = occurrencesCounter.getAndIncrement();

            if (currentOccurrence + 1 == occurrences)
                taskReference.get().cancel();

            runnable.accept(currentOccurrence);

        }, delay, interval));

        return taskReference.get();
    }

    @Override
    public BukkitTask runRepeatAsynchronously(IntConsumer runnable, long delay, long interval, int occurrences) {
        if (occurrences <= 0)
            throw new IllegalArgumentException("the number of occurrences must be strictly greater than 0 (" + occurrences + ").");

        AtomicInteger occurrencesCounter = new AtomicInteger();
        AtomicReference<BukkitTask> taskReference = new AtomicReference<>();
        taskReference.set(runTimerAsynchronously(() -> {

            int currentOccurrence = occurrencesCounter.getAndIncrement();

            if (currentOccurrence + 1 == occurrences)
                taskReference.get().cancel();

            runnable.accept(currentOccurrence);

        }, delay, interval));

        return taskReference.get();
    }
}
