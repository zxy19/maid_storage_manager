package studio.fantasyit.maid_storage_manager;

import com.mojang.logging.LogUtils;

public class Logger {
    static org.slf4j.Logger logger = LogUtils.getLogger();

    public static void info(String message, Object... a) {
        logger.info(message, a);
    }
    public static void warn(String message, Object... a) {
        logger.warn(message, a);
    }
    public static void error(String message, Object... a) {
        logger.error(message, a);
    }
}
