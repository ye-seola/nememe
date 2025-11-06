package io.nememe.shell.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DelayedSingleThreadExecutor {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final AtomicLong lastRun = new AtomicLong(0);
    private final long minDelayMs;

    public DelayedSingleThreadExecutor(long minDelayMs) {
        this.minDelayMs = minDelayMs;
    }

    public void submit(Runnable task) {
        exec.submit(() -> {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRun.get();
            long wait = Math.max(0, minDelayMs - elapsed);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                task.run();
            } finally {
                lastRun.set(System.currentTimeMillis());
            }
        });
    }

    public void shutdown() {
        exec.shutdown();
    }
}