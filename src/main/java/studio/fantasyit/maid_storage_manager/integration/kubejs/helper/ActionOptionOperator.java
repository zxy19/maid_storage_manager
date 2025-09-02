package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOptionSet;

import java.util.function.Function;
import java.util.function.Predicate;

public class ActionOptionOperator {
    public static ActionOptionOperator INSTANCE = new ActionOptionOperator();

    public <T> ActionOptionSet makeActionOptionSet(ActionOption<T> option, T selection) {
        return ActionOptionSet.with(option, selection);
    }

    public <T> ActionOptionSet makeActionOptionSetWithValue(ActionOption<T> option, T selection, String value) {
        return ActionOptionSet.with(option, selection, value);
    }

    public ActionOptionSet makeActionOptionSetOptional(boolean optional) {
        return makeActionOptionSet(ActionOption.OPTIONAL, optional);
    }

    public <T> ActionOption<T> makeActionOption(
            ResourceLocation id,
            Component[] tooltip,
            ResourceLocation[] icon,
            String defaultValue,
            @NotNull ActionOption.BiConverter<Integer, T> converter,
            @NotNull ActionOption.ValuePredicatorOrGetter<T> valuePredicatorOrGetter) {
        return new ActionOption<>(id, tooltip, icon, defaultValue, converter, valuePredicatorOrGetter);
    }

    public <T> ActionOption.ValuePredicatorOrGetter<T> makeValuePredicator(@NotNull Predicate<String> predicate) {
        return ActionOption.ValuePredicatorOrGetter.predicator(predicate);
    }

    public <T> ActionOption.ValuePredicatorOrGetter<T> makeValueGetter(@NotNull Function<T, Component> getter) {
        return ActionOption.ValuePredicatorOrGetter.getter(getter);
    }

    public <T> ActionOption.BiConverter<Integer, T> makeBiConverter(Function<Integer, T> converterA, Function<T, Integer> converterB) {
        return new ActionOption.BiConverter<>(converterA, converterB);
    }
}