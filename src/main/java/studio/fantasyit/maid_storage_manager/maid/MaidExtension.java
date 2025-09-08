package studio.fantasyit.maid_storage_manager.maid;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.FunctionCallRegister;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.client.overlay.MaidTipsOverlay;
import com.github.tartaricacid.touhoulittlemaid.debug.target.DebugTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Items;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.ai.CoWorkSwitchFunction;
import studio.fantasyit.maid_storage_manager.ai.FindAndMarkStorageFunction;
import studio.fantasyit.maid_storage_manager.ai.GetStorageFunction;
import studio.fantasyit.maid_storage_manager.ai.StorageFetchFunction;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.MaidInteractItem;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.render.CraftingChatBubbleData;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@LittleMaidExtension
public class MaidExtension implements ILittleMaid {

    @Override
    public void addMaidTask(TaskManager manager) {
        ILittleMaid.super.addMaidTask(manager);
        manager.add(new StorageManageTask());
    }

    @Override
    public void bindMaidBauble(BaubleManager manager) {
        manager.bind(ItemRegistry.STORAGE_DEFINE_BAUBLE.get(), (IMaidBauble) ItemRegistry.STORAGE_DEFINE_BAUBLE.get());
        manager.bind(ItemRegistry.LOGISTICS_GUIDE.get(), (IMaidBauble) ItemRegistry.LOGISTICS_GUIDE.get());
        manager.bind(ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get(), (IMaidBauble) ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get());
        manager.bind(ItemRegistry.WORK_CARD.get(), (IMaidBauble) ItemRegistry.WORK_CARD.get());
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
                        MemoryModuleRegistry.SORTING.get(),
                        MemoryModuleRegistry.CRAFTING.get(),
                        MemoryModuleRegistry.LOGISTICS.get(),
                        MemoryModuleRegistry.CURRENTLY_WORKING.get(),
                        MemoryModuleRegistry.INTERACTION_RESULT.get(),
                        MemoryModuleRegistry.CO_WORK_MODE.get(),
                        MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get(),
                        MemoryModuleRegistry.MEAL.get(),
                        MemoryModuleRegistry.IS_WORKING.get(),
                        MemoryModuleRegistry.PARALLEL_WORKING.get(),
                        MemoryModuleRegistry.ENABLE_PICKUP_TEMP.get(),
                        MemoryModuleRegistry.RETURN_CENTER.get()
                );
            }
        });
    }

    @Override
    public void registerTaskData(TaskDataRegister register) {
        StorageManagerConfigData.KEY = register.register(new StorageManagerConfigData());
    }

    @Override
    public void registerAIFunctionCall(FunctionCallRegister register) {
        register.register(new StorageFetchFunction());
        register.register(new GetStorageFunction());
        register.register(new CoWorkSwitchFunction());
        register.register(new FindAndMarkStorageFunction());
    }

    @Override
    public void addMaidTips(MaidTipsOverlay maidTipsOverlay) {
        maidTipsOverlay.addSpecialTips("tip.maid_storage_manager.interact_maid", (stack, maid, player) -> {
            if (!maid.getTask().getUid().equals(StorageManageTask.TASK_ID))
                return false;
            return stack.getItem() instanceof MaidInteractItem;
        });
        maidTipsOverlay.addSpecialTips("tip.maid_storage_manager.change_flag", (stack, maid, player) -> {
            if (!maid.getTask().getUid().equals(StorageManageTask.TASK_ID))
                return false;
            return stack.is(ItemRegistry.CHANGE_FLAG.get());
        });

        maidTipsOverlay.addTips("tip.maid_storage_manager.experience_bottle", Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public Collection<? extends Function<EntityMaid, List<DebugTarget>>> getMaidDebugTargets() {
        if (!Config.aiFunctions)
            return List.of();
        return List.of(
                DebugData::commonTargets,
                DebugData::placeSuppresses
        );
    }

    @Override
    public void registerChatBubble(ChatBubbleRegister register) {
        register.register(CraftingChatBubbleData.ID, new CraftingChatBubbleData.CraftingChatSerializer());
    }
}