package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.contextSupplier;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Function;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.AbstractWrapped;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.BaseWrappedWrapper;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context.IKJSCraftContext;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context.KJSCraftContext;

public class KJSCraftContextSupplier extends AbstractWrapped implements IKJSCraftContextSupplier {
    public static TypeWrapperFactory<KJSCraftContext> wrapper = new BaseWrappedWrapper<>(KJSCraftContext::new);
    private final Function func;

    public KJSCraftContextSupplier(Function func, Context context, TypeInfo typeInfo) {
        super(context, typeInfo);
        this.func = func;
    }

    @Override
    public IKJSCraftContext get() {
        Object t = context.callSync(func, func, func, new Object[]{});
        return wrapper.wrap(context, t, typeInfo);
    }
}
