package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Function;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BaseSupplierWrapper<T,U extends T> implements TypeWrapperFactory<T> {
    private final BiFunction<Function, Context, U> constructor;

    public static @Nullable <U> U wrapStatic(Context context, Object o, BiFunction<Function, Context, U> constructor) {
        if (o instanceof Function obj) {
            return constructor.apply(obj, context);
        }
        return null;
    }

    public BaseSupplierWrapper(BiFunction<Function, Context, U> constructor) {
        this.constructor = constructor;
    }

    @Override
    public U wrap(Context context, Object o) {
        return wrapStatic(context, o, constructor);
    }
}
