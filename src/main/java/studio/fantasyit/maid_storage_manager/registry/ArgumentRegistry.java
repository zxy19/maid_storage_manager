package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.argument.CraftingDebugControlArgument;

public class ArgumentRegistry {
    static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MaidStorageManager.MODID);
    static final Holder<ArgumentTypeInfo<?, ?>> DEBUG_ARG = REGISTRY.register("crafting_debug_control",
            () -> ArgumentTypeInfos.registerByClass(CraftingDebugControlArgument.class, SingletonArgumentInfo.contextFree(CraftingDebugControlArgument::new)));

    public static void init(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
