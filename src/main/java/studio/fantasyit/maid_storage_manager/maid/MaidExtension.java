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
                        MemoryModuleRegistry.VIEWED_INVENTORY.get(),
                        MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get(),
                        MemoryModuleRegistry.PLACING_INVENTORY.get(),
                        MemoryModuleRegistry.REQUEST_PROGRESS.get(),
                        MemoryModuleRegistry.RESORTING.get(),
                        MemoryModuleRegistry.CURRENTLY_WORKING.get()
                );
            }
        });
    }
}
