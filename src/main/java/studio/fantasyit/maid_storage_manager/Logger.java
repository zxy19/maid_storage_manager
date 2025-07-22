package studio.fantasyit.maid_storage_manager;

import com.mojang.logging.LogUtils;

import java.util.Arrays;

public class Logger {
    public static org.slf4j.Logger logger = LogUtils.getLogger();

    public static void info(String message, Object... a) {
        logger.info(message, a);
    }

    public static void warn(String message, Object... a) {
        logger.warn(message, a);
    }

    public static void error(String message, Object... a) {
        logger.error(message, a);
    }

    public static void debug(String message, Object... a) {
        String source = Arrays.stream(Thread.currentThread().getStackTrace())
                .skip(3)
                .findFirst()
                .map(element -> element.getClassName() + " | " + element.getMethodName() + "(" + element.getLineNumber() + ")")
                .orElse("Unknown Source");

        logger.debug(source + ": " + String.format(message, a));
    }
}
