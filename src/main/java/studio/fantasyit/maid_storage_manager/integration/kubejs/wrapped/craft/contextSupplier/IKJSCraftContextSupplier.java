package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.contextSupplier;

import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context.IKJSCraftContext;

@FunctionalInterface
public interface IKJSCraftContextSupplier {
    IKJSCraftContext get();
}
