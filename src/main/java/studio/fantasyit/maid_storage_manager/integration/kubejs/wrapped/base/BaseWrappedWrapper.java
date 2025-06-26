package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BaseWrappedWrapper<T,U extends T> implements TypeWrapperFactory<T> {
    private final BiFunction<NativeObject, Context, U> constructor;

    public static @Nullable <U> U wrapStatic(Context context, Object o, BiFunction<NativeObject, Context, U> constructor) {
        if (o instanceof NativeObject obj) {
            return constructor.apply(obj, context);
        }
        return null;
    }

    public BaseWrappedWrapper(BiFunction<NativeObject, Context, U> constructor) {
        this.constructor = constructor;
    }

    @Override
    public U wrap(Context context, Object o) {
        return wrapStatic(context, o, constructor);
    }
}
