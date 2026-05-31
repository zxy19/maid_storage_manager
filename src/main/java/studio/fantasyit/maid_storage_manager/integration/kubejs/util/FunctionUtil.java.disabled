package studio.fantasyit.maid_storage_manager.integration.kubejs.util;

import dev.latvian.mods.rhino.Callable;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;

import java.util.function.Supplier;

public class FunctionUtil {
    public static class WrappedSupplier<U, T extends Supplier<U>> implements Supplier<U>, Callable {
        private final T t;

        public WrappedSupplier(T t) {
            this.t = t;
        }

        @Override
        public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
            return t.get();
        }

        @Override
        public U get() {
            return t.get();
        }
    }

    public static <U, T extends Supplier<U>> WrappedSupplier<U, T> wrap(T t) {
        return new WrappedSupplier<>(t);
    }
}
