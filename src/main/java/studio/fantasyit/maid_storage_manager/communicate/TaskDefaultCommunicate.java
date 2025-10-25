package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskAttack;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskDefaultCommunicate {
    private static final Map<ResourceLocation, ConfigurableCommunicateData> taskDefaultCommunicateData = new HashMap<>();

    public static void init() {
        taskDefaultCommunicateData.clear();
        CollectCommunicateDataEvent event = new CollectCommunicateDataEvent(taskDefaultCommunicateData);
        fireInternal(event);
        MinecraftForge.EVENT_BUS.post(event);
    }

    private static void fireInternal(CollectCommunicateDataEvent event) {
        ConfigurableCommunicateData commonPlaceAll = new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.ALL,
                                List.of()
                        )
                )
        );
        event.register(TaskIdle.UID, commonPlaceAll);
        event.register(TaskAttack.UID, new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(Items.CARROT.getDefaultInstance().copyWithCount(10)),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.MAIN_HAND,
                                List.of()
                        )
                )
        ));
    }

    public static ConfigurableCommunicateData get(ResourceLocation id) {
        return taskDefaultCommunicateData.get(id);
    }
}
