package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;

import net.minecraft.network.chat.Component;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;

public class GeneratorConfigOperator {
    public static GeneratorConfigOperator INSTANCE = new GeneratorConfigOperator();

    public ConfigTypes.ConfigType<Integer> integerConfig(String key, int defaultValue, Component name) {
        return new ConfigTypes.ConfigType<>(key, defaultValue, name, ConfigTypes.ConfigTypeEnum.Integer);
    }
    public ConfigTypes.ConfigType<String> stringConfig(String key, String defaultValue, Component name) {
        return new ConfigTypes.ConfigType<>(key, defaultValue, name, ConfigTypes.ConfigTypeEnum.String);
    }
    public ConfigTypes.ConfigType<Boolean> booleanConfig(String key, boolean defaultValue, Component name) {
        return new ConfigTypes.ConfigType<>(key, defaultValue, name, ConfigTypes.ConfigTypeEnum.Boolean);
    }
    public ConfigTypes.ConfigType<Double> doubleConfig(String key, double defaultValue, Component name) {
        return new ConfigTypes.ConfigType<>(key, defaultValue, name, ConfigTypes.ConfigTypeEnum.Double);
    }
}
