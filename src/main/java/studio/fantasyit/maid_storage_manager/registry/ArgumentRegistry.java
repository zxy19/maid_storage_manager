package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.argument.CraftingDebugControlArgument;
import studio.fantasyit.maid_storage_manager.argument.ProgressDebugControlArgument;

public class ArgumentRegistry {
    static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MaidStorageManager.MODID);
    static final RegistryObject<ArgumentTypeInfo<?, ?>> DEBUG_ARG = REGISTRY.register("crafting_debug_control",
            () -> ArgumentTypeInfos.registerByClass(CraftingDebugControlArgument.class, SingletonArgumentInfo.contextFree(CraftingDebugControlArgument::new)));
    static final RegistryObject<ArgumentTypeInfo<?, ?>> DEBUG_PROG_ARG = REGISTRY.register("progress_debug_control",
            () -> ArgumentTypeInfos.registerByClass(ProgressDebugControlArgument.class, SingletonArgumentInfo.contextFree(ProgressDebugControlArgument::new)));

    public static void init(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
