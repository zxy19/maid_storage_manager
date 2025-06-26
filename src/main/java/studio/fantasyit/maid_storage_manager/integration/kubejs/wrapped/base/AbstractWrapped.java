package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.integration.kubejs.util.TypeCastingUtil;

import java.util.function.Function;

public class AbstractWrapped {
    protected final Context context;

    public AbstractWrapped(Context context) {
        this.context = context;
    }

    protected ResourceLocation resourceLocationParser(Object t) {
        ResourceLocation casted = cast(t, ResourceLocation.class);
        if (casted != null)
            return casted;
        if (t instanceof String string)
            return ResourceLocation.tryParse(string);
        return ResourceLocation.tryParse(context.toString(t));
    }

    protected Component componentParser(Object t) {
        Component casted = cast(t, Component.class);
        if (casted != null)
            return casted;
        return Component.literal(context.toString(t));
    }

    protected Boolean booleanParser(Object t) {
        Boolean casted = cast(t, Boolean.class);
        if (casted != null)
            return casted;
        return context.toBoolean(t);
    }

    protected <T> Function<Object, T> classTest(Class<T> clazz) {
        return (Object t) -> cast(t, clazz);
    }

    protected <T> @Nullable T cast(Object t, Class<T> clazz) {
        return TypeCastingUtil.cast(t, clazz);
    }

    protected <T> Function<Object, T> useWrapper(TypeWrapperFactory<T> wrapper) {
        return t -> wrapper.wrap(context, t);
    }

    protected Object any(Object t) {
        return t;
    }
}
