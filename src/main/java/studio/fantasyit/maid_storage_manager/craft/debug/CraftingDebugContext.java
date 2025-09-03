package studio.fantasyit.maid_storage_manager.craft.debug;


import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;

import java.util.Arrays;

public class CraftingDebugContext {
    public void convey(Object anyOther) {
        if (anyOther instanceof IDebugContextSetter setter)
            setter.setDebugContext(this);
        else if (anyOther != null)
            logNoLevel(TYPE.COMMON, "[?]Convey to non-debug class: %s", anyOther.getClass().getName());
        else
            logNoLevel(TYPE.COMMON, "[?]Convey to null");
    }

    private record tData(Logger logger, LoggerConfig config) {
    }

    public static class Dummy extends CraftingDebugContext {
        public static final CraftingDebugContext INSTANCE = new CraftingDebugContext.Dummy();

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
        PLANNER(false),
        GENERATOR(false),
        CRAFT(false),
        LOOP_RESOLVER(false),
        PREFILTER(false),
        GENERATOR_RECIPE(true),
        SIMULATOR(false);
        public final boolean disableByDefault;


        TYPE(boolean disableByDefault) {
            this.disableByDefault = disableByDefault;
        }
    }

    public final String id;
    public final Logger logger;
    private final LoggerConfig config;
    int level = 0;
    protected boolean[] disable = new boolean[TYPE.values().length];

    public CraftingDebugContext(boolean b) {
        id = "debug_" + System.currentTimeMillis();
        if (b) {
            logger = null;
            config = null;
            return;
        }
        tData tmp = getLogger(id);
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

    public static tData getLogger(String id) {
        // 获取LoggerContext
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern("%d{HH:mm:ss.SSS}[%-20t] %msg%n")
                .build();
        String baseFilePath = FMLPaths.GAMEDIR.get() + "/logs/msm_crafting/" + id + ".log";
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
        org.apache.logging.log4j.core.config.LoggerConfig loggerConfig =
                org.apache.logging.log4j.core.config.LoggerConfig.newBuilder()
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


    public void logEntryNewLevel(TYPE type, String message, Object... a) {
        level++;
        log(type, message, a);
    }

    public void logExitLevel(TYPE type, String message, Object... a) {
        log(type, message, a);
        level--;
    }

    public void exitLogLevel(TYPE type, String message, Object... a) {
        level--;
        log(type, message, a);
    }

    public boolean isDummy() {
        return this instanceof Dummy || logger == null;
    }

    public void log(TYPE type, String message, Object... a) {
        if (isDummy()) return;
        if (disable[type.ordinal()]) return;
        StringBuilder sb = new StringBuilder();
        sb.append(name(type));
        for (int i = 0; i < level; i++) sb.append(" -");
        sb.append("> ");
        sb.append(String.format(message, a));
        logger.debug(sb.toString());
    }

    public void logNoLevel(TYPE type, String message, Object... a) {
        if (isDummy()) return;
        if (disable[type.ordinal()]) return;
        logger.debug(name(type) + String.format(message, a));
    }

    public String name(TYPE type) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type.name());
        for (int i = 0; i < 18 - type.name().length(); i++)
            sb.append(" ");
        sb.append("]  ");
        return sb.toString();
    }

    public void clearLevel() {
        level = 0;
    }

    public void stop() {
        config.stop();
    }

    int graphIndex = 0;

    public void saveGraph(AbstractBiCraftGraph graph, ItemStack target, int count) {
        if (isDummy()) return;
        String path = FMLPaths.GAMEDIR.get() + "/logs/msm_crafting/" + id + "_crafting_data_" + target.getItem() + "_" + (graphIndex++) + ".json";
        Tester.exportTo(graph, target, count, path);
        graph.restoreCurrent();
    }
}
