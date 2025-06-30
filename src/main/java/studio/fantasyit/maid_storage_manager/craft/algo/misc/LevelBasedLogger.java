package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import studio.fantasyit.maid_storage_manager.Config;

public class LevelBasedLogger {
    int level = 0;
    Logger logger = LogUtils.getLogger();

    public void logEntryNewLevel(String message, Object... a) {
        level++;
        log(message, a);
    }

    public void logExitLevel(String message, Object... a) {
        log(message, a);
        level--;
    }
    public void exitLogLevel(String message, Object... a) {
        level--;
        log(message, a);
    }

    public void log(String message, Object... a) {
        if (!Config.enableDebug) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) sb.append(" -");
        sb.append("> ");
        sb.append(String.format(message, a));
        logger.debug(sb.toString());
    }
}
