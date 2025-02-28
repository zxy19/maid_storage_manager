package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.PlacingInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ResortingMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;

import java.util.Optional;

public class MemoryModuleRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> REGISTER
            = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MaidStorageManager.MODID);

    public static final RegistryObject<MemoryModuleType<Integer>> RETURN_TO_SCHEDULE_AT
            = REGISTER.register("return_to_schedule_work", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<RequestProgressMemory>> REQUEST_PROGRESS
            = REGISTER.register("request_progress", () -> new MemoryModuleType<>(Optional.of(RequestProgressMemory.CODEC)));
    public static final RegistryObject<MemoryModuleType<PlacingInventoryMemory>> PLACING_INVENTORY
            = REGISTER.register("placing_inventory", () -> new MemoryModuleType<>(Optional.of(PlacingInventoryMemory.CODEC)));
    public static final RegistryObject<MemoryModuleType<ViewedInventoryMemory>> VIEWED_INVENTORY
            = REGISTER.register("viewed_inventory", () -> new MemoryModuleType<>(Optional.of(ViewedInventoryMemory.CODEC)));
    public static final RegistryObject<MemoryModuleType<ResortingMemory>> RESORTING
            = REGISTER.register("resorting", () -> new MemoryModuleType<>(Optional.of(ResortingMemory.CODEC)));

    public static final RegistryObject<MemoryModuleType<ScheduleBehavior.Schedule>> CURRENTLY_WORKING
            = REGISTER.register("working", () -> new MemoryModuleType<>(Optional.empty()));


    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
