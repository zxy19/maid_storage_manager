package studio.fantasyit.maid_storage_manager.craft.debug;


import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import studio.fantasyit.maid_storage_manager.network.DebugDataPacket;

import java.util.Arrays;

public class ProgressDebugContext {
    public void convey(Object anyOther) {
        if (anyOther instanceof IProgressDebugContextSetter setter)
            setter.setDebugContext(this);
        else if (anyOther != null)
            log(TYPE.COMMON, "[?]Convey to non-debug class: %s", anyOther.getClass().getName());
        else
            log(TYPE.COMMON, "[?]Convey to null");
    }

    private record tData(Logger logger, LoggerConfig config) {
    }

    public static class Dummy extends ProgressDebugContext {
        public static final ProgressDebugContext INSTANCE = new Dummy();

        public Dummy() {
            super(true);
            Arrays.stream(TYPE.values()).forEach(this::disable);
        }

        @Override
        public void convey(Object anyOther) {
        }
    }

    public enum TYPE {
        COMMON(false),
        MOVE(false),
        WORK(false),
        STATUS(false),
        INTERACT(false),
        DEBUG(true);
        public final boolean disableByDefault;


        TYPE(boolean disableByDefault) {
            this.disableByDefault = disableByDefault;
        }
    }

    public final String id;
    public final Logger logger;
    private final LoggerConfig config;
    int iid = 0;
    protected boolean[] disable = new boolean[TYPE.values().length];

    public ProgressDebugContext(boolean b) {
        id = "debug_" + System.currentTimeMillis();
        if (b) {
            logger = null;
            config = null;
            return;
        }
        tData tmp = getLogger(id, null);
        logger = tmp.logger;
        config = tmp.config;
        Arrays.stream(TYPE.values()).filter(type -> type.disableByDefault).forEach(this::disable);
    }

    public ProgressDebugContext(boolean b, String pathOverride) {
        id = "debug_" + System.currentTimeMillis();
        if (b) {
            logger = null;
            config = null;
            return;
        }
        tData tmp = getLogger(id, pathOverride);
        logger = tmp.logger;
        config = tmp.config;
        Arrays.stream(TYPE.values()).filter(type -> type.disableByDefault).forEach(this::disable);
    }

    public void disable(TYPE type) {
        disable[type.ordinal()] = true;
    }

    public void enable(TYPE type) {
        disable[type.ordinal()] = false;
    }

    private static tData getLogger(String id, String pathOverride) {
        // 获取LoggerContext
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern("%d{HH:mm:ss.SSS}[%-20t] %msg%n")
                .build();
        String baseFilePath = (pathOverride != null ? pathOverride : (FMLPaths.GAMEDIR.get() + "/logs/msm_progress/"))
                + id + ".log";
        TriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(
                SizeBasedTriggeringPolicy.createPolicy("1 GB")
        );
        RollingFileAppender appender = RollingFileAppender.newBuilder()
                .withFileName(baseFilePath) // 当前日志文件
                .withFilePattern(baseFilePath + "-%i.log.gz")
                .setName(id)
                .setLayout(layout)
                .withPolicy(policy)
                .setConfiguration(config)
                .build();
        appender.start();
        config.addAppender(appender);
        LoggerConfig loggerConfig =
                LoggerConfig.newBuilder()
                        .withAdditivity(false)
                        .withLevel(Level.DEBUG)
                        .withLoggerName(id)
                        .withConfig(config)
                        .build();

        loggerConfig.addAppender(appender, Level.DEBUG, null);
        config.addLogger(id, loggerConfig);
        context.updateLoggers(config);
        return new tData(LogManager.getLogger(id), loggerConfig);
    }

    private static void closeLogger(Logger logger, LoggerConfig loggerConfig) {
        if (loggerConfig == null || logger == null) return;

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // 1. 从配置中移除 LoggerConfig
        config.removeLogger(loggerConfig.getName());

        // 2. 停止并移除 Appender
        Appender appender = loggerConfig.getAppenders().get(loggerConfig.getName());
        if (appender != null) {
            loggerConfig.removeAppender(appender.getName());
            appender.stop();
            config.getAppenders().remove(appender.getName());
        }
        context.updateLoggers(config);
    }

    public boolean isDummy() {
        return this instanceof Dummy || logger == null;
    }

    public void log(TYPE type, String message, Object... a) {
        if (isDummy()) return;
        if (disable[type.ordinal()]) return;
        logger.debug(name(type) + String.format("[%6d]", ++iid) + String.format(message, a));
        PacketDistributor.sendToAllPlayers(new DebugDataPacket(String.format("[%s][%6d]", id, ++iid) + String.format(message, a)));
    }

    public String name(TYPE type) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type.name());
        for (int i = 0; i < 18 - type.name().length(); i++)
            sb.append(" ");
        sb.append("]  ");
        return sb.toString();
    }

    public void stop() {
        closeLogger(logger, config);
    }
}
