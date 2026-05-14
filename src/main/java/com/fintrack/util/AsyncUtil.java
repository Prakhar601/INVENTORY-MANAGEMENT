package com.fintrack.util;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for running heavy database/processing tasks off the JavaFX Application Thread.
 */
public class AsyncUtil {

    private static final Logger LOGGER = LoggerUtil.getLogger(AsyncUtil.class);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Executes a background task and consumes the result on the UI thread.
     * 
     * @param backgroundTask The task to run on a background thread.
     * @param uiCallback The callback to execute on the UI thread with the result.
     * @param errorHandler The callback to execute on the UI thread if an error occurs.
     * @param <T> The return type of the background task.
     */
    public static <T> void runAsync(Supplier<T> backgroundTask, Consumer<T> uiCallback, Consumer<Throwable> errorHandler) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                long start = System.currentTimeMillis();
                T result = backgroundTask.get();
                long end = System.currentTimeMillis();
                System.out.println("[PERF] Async task completed in " + (end - start) + "ms");
                return result;
            }
        };

        task.setOnSucceeded(e -> {
            if (uiCallback != null) {
                uiCallback.accept(task.getValue());
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "[ERROR] Async task failed: " + ex.getMessage(), ex);
            
            if (errorHandler != null) {
                errorHandler.accept(ex);
            } else {
                // If no specific error handler is provided, route to the global friendly dialog
                ErrorDialogUtil.showFriendlyError(ex);
            }
        });

        EXECUTOR.submit(task);
    }

    /**
     * Executes a simple background runnable.
     */
    public static void runAsync(Runnable backgroundRunnable) {
        EXECUTOR.submit(() -> {
            long start = System.currentTimeMillis();
            try {
                backgroundRunnable.run();
                long end = System.currentTimeMillis();
                LOGGER.info("[PERF] Async runnable completed in " + (end - start) + "ms");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[ERROR] Async runnable failed.", e);
                ErrorDialogUtil.showFriendlyError(e);
            }
        });
    }

    /**
     * Shuts down the global executor. Call this on application exit.
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}
