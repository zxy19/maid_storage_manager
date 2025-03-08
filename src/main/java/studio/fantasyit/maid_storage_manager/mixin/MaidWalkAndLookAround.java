package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidRunOne;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

@Mixin(value = MaidRunOne.class, remap = false)
public class MaidWalkAndLookAround {
    @Inject(method = "tryStart(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)Z", at = @At("HEAD"), cancellable = true, remap = false)
    public void tryStart(ServerLevel pLevel, EntityMaid maid, long pGameTime, CallbackInfoReturnable<Boolean> cir) {
        if (maid.getTask().getUid().equals(StorageManageTask.TASK_ID) && maid.getScheduleDetail() == Activity.WORK) {
            if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.NO_SCHEDULE && MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.VIEW) {
                cir.setReturnValue(false);
            }
        }
    }
}
