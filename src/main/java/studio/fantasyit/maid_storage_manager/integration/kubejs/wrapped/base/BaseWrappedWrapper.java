package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

public class BaseWrappedWrapper<T, U extends T> implements TypeWrapperFactory<T> {
    private final TriFunction<NativeObject, Context, TypeInfo, U> constructor;

    public static @Nullable <U> U wrapStatic(Context context, Object o, TypeInfo info, TriFunction<NativeObject, Context, TypeInfo, U> constructor) {
        if (o instanceof NativeObject obj) {
            return constructor.apply(obj, context, info);
        }
        return null;
    }

    public BaseWrappedWrapper(TriFunction<NativeObject, Context, TypeInfo, U> constructor) {
        this.constructor = constructor;
    }

    @Override
    public U wrap(Context context, Object o, TypeInfo typeInfo) {
        return wrapStatic(context, o, typeInfo, constructor);
    }
}
