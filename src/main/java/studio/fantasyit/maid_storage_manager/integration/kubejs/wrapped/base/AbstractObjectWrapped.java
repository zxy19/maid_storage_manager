package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base;

import dev.latvian.mods.rhino.Callable;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.Scriptable;

import java.util.Optional;
import java.util.function.Function;

public class AbstractObjectWrapped extends AbstractWrapped{
    protected final NativeObject obj;

    public AbstractObjectWrapped(NativeObject obj, Context context) {
        super(context);
        this.obj = obj;
    }

    protected <T> Optional<T> get(String key, Function<Object, T> converter, Object... args) {
        if (!obj.containsKey(key))
            return Optional.empty();
        Object o = obj.get(key);
        if (o instanceof Callable callable && o instanceof Scriptable scope) {
            return Optional.ofNullable(converter.apply(callable.call(context, obj, scope, args)));
        }
        return Optional.ofNullable(converter.apply(o));
    }
}
