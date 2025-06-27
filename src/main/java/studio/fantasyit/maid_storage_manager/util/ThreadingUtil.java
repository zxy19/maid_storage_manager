package studio.fantasyit.maid_storage_manager.util;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadingUtil {
    public static final ExecutorService pool = Executors.newFixedThreadPool(4);

    public static Future<?> run(Runnable runnable) {
        return pool.submit(runnable);
    }
}
