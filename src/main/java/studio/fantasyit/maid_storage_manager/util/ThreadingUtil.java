package studio.fantasyit.maid_storage_manager.util;


import studio.fantasyit.maid_storage_manager.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadingUtil {
    public static final ExecutorService pool = Executors.newFixedThreadPool(8);
    public static final ExecutorService watchPool = Executors.newFixedThreadPool(8);

    public static Future<?> run(Runnable runnable) {
        Future<?> submitted = pool.submit(runnable);
        watchPool.submit(() -> {
            int i = 0;
            while (!submitted.isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {

                } finally {
                    i++;
                    if (i > 30) {
                        Logger.warn("Crafting task timeout after 30s");
                        submitted.cancel(true);
                    }
                }
            }
        });
        return submitted;
    }
}
