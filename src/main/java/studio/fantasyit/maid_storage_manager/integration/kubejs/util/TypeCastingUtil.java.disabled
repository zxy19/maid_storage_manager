package studio.fantasyit.maid_storage_manager.integration.kubejs.util;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaObject;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TypeCastingUtil {
    public static <T> @Nullable T cast(Object t, Class<T> clazz) {
        if (clazz.isInstance(t))
            return clazz.cast(t);
        if (t instanceof NativeJavaObject njo) {
            Object u = njo.unwrap();
            if (clazz.isInstance(u))
                return clazz.cast(u);
        }
        return null;
    }

    public static <T> @NotNull T castOrThrow(Object t, Class<T> clazz) {
        return Optional.ofNullable(cast(t, clazz)).orElseThrow();
    }

    public static <T> @NotNull T wrapOrThrow(Object t, Context context, TypeWrapperFactory<T> wrapper) {
        return Optional.ofNullable(wrapper.wrap(context, t, TypeInfo.VOID)).orElseThrow();
    }
}
