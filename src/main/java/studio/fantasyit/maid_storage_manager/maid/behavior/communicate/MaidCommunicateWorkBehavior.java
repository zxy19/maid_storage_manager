package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IActionStep;

import java.util.Map;

public class MaidCommunicateWorkBehavior extends Behavior<EntityMaid> {
    private IActionStep step;
    private boolean isEnd;
    private boolean isSuccess;

    public MaidCommunicateWorkBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
    }


    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (!(maid.getTask() instanceof ICommunicatable communicatable))
            return;
        CommunicateRequest currentCommunicateRequest = communicatable.getCurrentCommunicateRequest(maid);
        if (currentCommunicateRequest == null || currentCommunicateRequest.isFinished())
            return;
        step = currentCommunicateRequest.getCurrentStep();
        ActionResult startResult = step.start(currentCommunicateRequest.wisher(), currentCommunicateRequest.handler());
        isEnd = startResult.isEnd();
        isSuccess = startResult.isSuccess();
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {

    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {

    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
