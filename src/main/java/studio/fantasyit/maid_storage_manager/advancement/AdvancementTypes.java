package studio.fantasyit.maid_storage_manager.advancement;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class AdvancementTypes {
    public static final String REQUEST_LIST_GOT = "request_list";
    public static final String REQUEST_LIST_REPEAT_GOT = "request_list_repeat";
    public static final String RESORT = "resort";
    public static final String STORAGE_LIST = "storage_list";
    public static final String VIEW = "view";
    public static final String COWORK = "cowork";
    public static final String STORAGE_MANAGER = "storage_manager";
    public static final String LOGISTICS = "logistics_guide";

    public static void triggerForMaid(EntityMaid maid, String key) {
        LivingEntity player = maid.getOwner();
        if (player instanceof ServerPlayer sp) {
            InitTrigger.MAID_EVENT.trigger(sp, key);
        }
    }
}
