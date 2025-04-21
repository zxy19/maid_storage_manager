package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Objects;

/**
 * 合成工作1
 */
public class RequestCraftWorkMoveBehavior extends Behavior<EntityMaid> {
    public RequestCraftWorkMoveBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    Target target;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (!MemoryUtil.getCrafting(owner).hasStartWorking()) return false;
        if (!MemoryUtil.getCrafting(owner).hasCurrent()) return false;
        if (!MemoryUtil.getCrafting(owner).getCurrentLayer().hasCollectedAll()) return false;
        return true;
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        for (int i = 0; i < 3; i++) {
            if (targeting(level, maid))
                return;
        }
    }

    private boolean targeting(ServerLevel level, EntityMaid maid) {
        CraftLayer layer = Objects.requireNonNull(MemoryUtil.getCrafting(maid).getCurrentLayer());
        CraftGuideStepData step = layer.getStepData();
        if (step == null) {
            if (!layer.getItems().isEmpty())
                ChatTexts.send(maid, ChatTexts.CHAT_CRAFTING_SUCCESS,
                        ChatTexts.fromComponent(layer.getItems().get(0).getHoverName())
                );
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK] Step Done. Set Success.");
            //根层
            for (int i = 0; i < layer.getItems().size(); i++) {
                ItemStack itemStack = layer.getItems().get(i);
                if (itemStack.isEmpty()) continue;
                RequestListItem.updateCollectedItem(maid.getMainHandItem(),
                        itemStack,
                        itemStack.getCount()
                );
            }
            MemoryUtil.getCrafting(maid).lastSuccess();
            MemoryUtil.getCrafting(maid).nextLayer();
            MemoryUtil.getCrafting(maid).resetAndMarkVisForRequest(level, maid);
            //立刻安排返回存储
            MemoryUtil.getRequestProgress(maid).setReturn();
            MemoryUtil.getRequestProgress(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            return true;
        }
        Target storage = step.getStorage();
        if (storage == null || step.actionType == null) {
            //当前合成不存在，直接进行下一步
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]No current step. Next.");
            layer.nextStep();
            if (layer.isDone()) {
                MemoryUtil.getCrafting(maid).nextLayer();
                MemoryUtil.getCrafting(maid).resetAndMarkVisForRequest(level, maid);
            }
            //遇到这种情况需要重新选择
            return false;
        } else {
            DebugData.getInstance().sendMessage(
                    String.format("[REQUEST_CRAFT_WORK]Step %d [%d/%d], %s",
                            layer.getStep(),
                            layer.getDoneCount(),
                            layer.getCount(),
                            storage
                    )
            );
            BlockPos blockPos = step.actionType.pathFindingTargetProvider().find(maid, layer.getCraftData().get(), step, layer);
            if (blockPos != null) {
                MemoryUtil.setTarget(maid, blockPos, (float) Config.craftWorkSpeed);
                MemoryUtil.getCrafting(maid).setTarget(storage);
                MemoryUtil.setLookAt(maid, storage.getPos());
            }
        }
        return true;
    }
}