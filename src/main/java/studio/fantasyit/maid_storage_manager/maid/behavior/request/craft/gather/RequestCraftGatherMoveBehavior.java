package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.gather;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.base.AbstractGatherMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Objects;

/**
 * 手上持有物品清单，尝试前往附近所有的箱子
 */
public class RequestCraftGatherMoveBehavior extends AbstractGatherMoveBehavior {
    public RequestCraftGatherMoveBehavior() {
        super((float) Config.collectSpeed);
    }
    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (MemoryUtil.getCrafting(owner).hasStartWorking()) return false;
        if (!MemoryUtil.getCrafting(owner).hasCurrent()) return false;
        return true;
    }

    @Override
    protected AbstractTargetMemory getMemory(EntityMaid maid) {
        return MemoryUtil.getCrafting(maid);
    }

    @Override
    protected boolean hasFinishedPre(ServerLevel level, EntityMaid maid) {
        if (MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) {
            MemoryUtil.getCrafting(maid).finishGathering(maid);
            return true;
        }
        return false;
    }

    @Override
    protected void findTarget(ServerLevel level, EntityMaid maid, Target target) {
        ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_GATHER);
    }

    @Override
    protected void noTarget(ServerLevel level, EntityMaid maid) {
        MemoryUtil.getCrafting(maid).finishGathering(maid);
    }

    @Override
    protected @NotNull List<ItemStack> getPriorityItems(ServerLevel level, EntityMaid maid) {
        return Objects.requireNonNull(MemoryUtil.getCrafting(maid).getCurrentLayer()).getUnCollectedItems();
    }
}
