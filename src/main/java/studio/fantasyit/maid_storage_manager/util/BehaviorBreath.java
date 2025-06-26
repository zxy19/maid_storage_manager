package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.Config;

/**
 * 女仆行为“呼吸”控制对象。用于降低女仆的工作效率
 */
public class BehaviorBreath {
    int breath = 5;

    public boolean breathTick(EntityMaid maid) {
        if (Config.realWorkSim)
            if (!maid.getNavigation().isDone() || maid.getDeltaMovement().length() > 0.1) {
                breath = 20;
                return false;
            }
        if (breath > 0) {
            breath--;
        }
        if (breath == 0) {
            breath = 5;
            return true;
        }
        return false;
    }

    public void reset() {
        breath = 0;
    }
}
