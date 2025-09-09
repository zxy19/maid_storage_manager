package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.*;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Optional;
import java.util.UUID;

public class MemoryModuleRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> REGISTER
            = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MaidStorageManager.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> RETURN_TO_SCHEDULE_AT
            = REGISTER.register("return_to_schedule_work", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<RequestProgressMemory>> REQUEST_PROGRESS
            = REGISTER.register("request_progress", () -> new MemoryModuleType<>(Optional.of(RequestProgressMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<PlacingInventoryMemory>> PLACING_INVENTORY
            = REGISTER.register("placing_inventory", () -> new MemoryModuleType<>(Optional.of(PlacingInventoryMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<ViewedInventoryMemory>> VIEWED_INVENTORY
            = REGISTER.register("viewed_inventory", () -> new MemoryModuleType<>(Optional.of(ViewedInventoryMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<ResortingMemory>> RESORTING
            = REGISTER.register("resorting", () -> new MemoryModuleType<>(Optional.of(ResortingMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<SortingMemory>> SORTING
            = REGISTER.register("sorting", () -> new MemoryModuleType<>(Optional.of(SortingMemory.CODEC)));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<ScheduleBehavior.Schedule>> CURRENTLY_WORKING
            = REGISTER.register("working", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<CraftMemory>> CRAFTING
            = REGISTER.register("crafting", () -> new MemoryModuleType<>(Optional.of(CraftMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LogisticsMemory>> LOGISTICS
            = REGISTER.register("logistics", () -> new MemoryModuleType<>(Optional.of(LogisticsMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> INTERACTION_RESULT
            = REGISTER.register("interact_result", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Boolean>> CO_WORK_MODE
            = REGISTER.register("co_work", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Target>> CO_WORK_TARGET_STORAGE
            = REGISTER.register("co_work_target", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<MealMemory>> MEAL
            = REGISTER.register("meal", () -> new MemoryModuleType<>(Optional.of(MealMemory.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Boolean>> RETURN_CENTER
            = REGISTER.register("return_center", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Boolean>> IS_WORKING = REGISTER.register("is_working", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> PARALLEL_WORKING = REGISTER.register("parallel_working", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<UUID>> ENABLE_PICKUP_TEMP = REGISTER.register("enable_pickup_temp", () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}

