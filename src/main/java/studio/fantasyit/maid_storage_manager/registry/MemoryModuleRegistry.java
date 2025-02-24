package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MemoryModuleRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> REGISTER
            = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MaidStorageManager.MODID);

    public static final RegistryObject<MemoryModuleType<Set<BlockPos>>> MAID_VISITED_POS
            = REGISTER.register("maid_visited_pos", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<BlockPos>> CURRENT_CHEST_POS
            = REGISTER.register("current_chest_pos", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<BlockPos>> CURRENT_TERMINAL_POS
            = REGISTER.register("current_terminal_pos", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> FINISH_CHEST
            = REGISTER.register("finish_chest", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Boolean>> FINISH_TERMINAL
            = REGISTER.register("finish_terminal", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Integer>> RETURN_TO_SCHEDULE_AT
            = REGISTER.register("return_to_schedule_work", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> RETURN_STORAGE
            = REGISTER.register("return_storage", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Boolean>> ARRIVE_TARGET
            = REGISTER.register("arrive_target", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<UUID>> LAST_TAKE_LIST
            = REGISTER.register("last_take_list", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_WORKING_REQUEST
            = REGISTER.register("is_working_request", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<ViewedInventoryMemory>> VIEWED_INVENTORY
            = REGISTER.register("viewed_inventory", () -> new MemoryModuleType<>(Optional.of(ViewedInventoryMemory.CODEC)));


    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
