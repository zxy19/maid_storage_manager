package studio.fantasyit.maid_storage_manager.maid.behavior.logistics;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class LogisticsSwitchTask extends Behavior<EntityMaid> {
    public LogisticsSwitchTask() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        LogisticsMemory logistics = MemoryUtil.getLogistics(maid);
        if (logistics.getStage() == LogisticsMemory.Stage.FINISH) {
            if (Conditions.isNothingToPlace(maid))
                return true;
        }
        if (logistics.shouldWork()) {
            if (!logistics.isStillValid(maid)) {
                return true;
            }
        } else if (logistics.getStage() != LogisticsMemory.Stage.FINISH) {
            return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel p_22540_, EntityMaid maid, long p_22542_) {
        LogisticsMemory logistics = MemoryUtil.getLogistics(maid);
        if (logistics.getStage() != LogisticsMemory.Stage.FINISH)
            logistics.setStage(LogisticsMemory.Stage.FINISH);
        else {
            logistics.switchCurrentLogisticsGuideItem(maid);
            if (logistics.shouldWork()) {
                logistics.clearTarget();
                logistics.setStage(LogisticsMemory.Stage.INPUT);
            }else{
                logistics.clearTarget();
                logistics.setStage(LogisticsMemory.Stage.FINISH);
            }
        }
    }
}
