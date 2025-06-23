package studio.fantasyit.maid_storage_manager.craft.generator.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.function.Consumer;

public class ConfigTypes {

    public enum ConfigTypeEnum {
        Boolean,
        Integer,
        String,
        Double
    }

    public static class ConfigType<T> {
        protected String key;
        protected T value;
        protected Component translatableName;
        public ConfigTypeEnum type;
        protected Consumer<T> onChange = null;

        public ConfigType(String key, T value, Component translatableName, ConfigTypeEnum type) {
            this.key = key;
            this.value = value;
            this.translatableName = translatableName;
            this.type = type;
        }

        public void setOnChange(Consumer<T> onChange) {
            this.onChange = onChange;
        }

        public void setValue(T value) {
            this.value = value;
            if (onChange != null) onChange.accept(value);
            GeneratingConfig.save();
        }

        public T getValue() {
            return value;
        }

        public void read(CommentedConfig reader, String base, MutableBoolean changeFlag) {
            value = reader.getOrElse(base + "." + key, () -> {
                reader.set(base + "." + key, value);
                changeFlag.setTrue();
                return value;
            });
            if (onChange != null) onChange.accept(value);
        }

        public void save(CommentedConfig write, String base) {
            write.set(base + "." + key, value);
        }

        public Component getTranslatableName() {
            return translatableName;
        }
    }
}
