package studio.fantasyit.maid_storage_manager.maid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.List;

@LittleMaidExtension
public class MaidExtension implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        ILittleMaid.super.addMaidTask(manager);
        manager.add(new StorageManageTask());
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new IExtraMaidBrain() {
            @Override
            public List<MemoryModuleType<?>> getExtraMemoryTypes() {
                return List.of(
                        MemoryModuleRegistry.MAID_VISITED_POS.get(),
                        MemoryModuleRegistry.RETURN_STORAGE.get(),
                        MemoryModuleRegistry.ARRIVE_TARGET.get(),
                        MemoryModuleRegistry.LAST_TAKE_LIST.get(),
                        MemoryModuleRegistry.CURRENT_CHEST_POS.get(),
                        MemoryModuleRegistry.IS_WORKING_REQUEST.get(),
                        MemoryModuleRegistry.VIEWED_INVENTORY.get(),
                        MemoryModuleRegistry.FINISH_TERMINAL.get(),
                        MemoryModuleRegistry.FINISH_CHEST.get(),
                        MemoryModuleRegistry.CURRENT_TERMINAL_POS.get(),
                        MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get()
                );
            }
        });
    }
}
