package studio.fantasyit.maid_storage_manager.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.task.*;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.api.event.CollectCommunicateDataEvent;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TaskDefaultCommunicate {
    public static final ResourceLocation DUMMY_AUTO_DETECT_TASK = new ResourceLocation(MaidStorageManager.MODID, "auto_detect");
    public static final ResourceLocation DUMMY_USE_CURRENT_DATA = new ResourceLocation(MaidStorageManager.MODID, "current");

    private static final Map<ResourceLocation, Component> translations = new HashMap<>();
    private static final Map<ResourceLocation, ConfigurableCommunicateData> taskDefaultCommunicateData = new HashMap<>();

    public static void init() {
        taskDefaultCommunicateData.clear();
        translations.clear();
        translations.put(DUMMY_AUTO_DETECT_TASK, Component.translatable("maid_storage_manager.task.auto_detect"));
        translations.put(DUMMY_USE_CURRENT_DATA, Component.translatable("maid_storage_manager.task.use_current_data"));
        CollectCommunicateDataEvent event = new CollectCommunicateDataEvent(taskDefaultCommunicateData, translations);
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
                                0,
                                0,
                                -1
                        )
                )
        );
        event.register(TaskIdle.UID, commonPlaceAll);
        event.register(TaskFeedAnimal.UID, new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(
                                        Items.CARROT.getDefaultInstance(),
                                        Items.WHEAT.getDefaultInstance()
                                ),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.ALL,
                                20,
                                10,
                                64 * 100
                        )
                )
        ));
        event.register(TaskAttack.UID, new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.ETA,
                                0,
                                0,
                                -1
                        )
                )
        ));
        event.register(TaskBowAttack.UID, buildWithMainHandAndBackPack1AndPlaceAll(Items.BOW.getDefaultInstance(), 1, 1, Items.ARROW.getDefaultInstance(), 128, 32));
        event.register(TaskCocoa.UID, buildWithMainHandAndPlaceAll(Items.COCOA_BEANS.getDefaultInstance(), 64, 1));
        event.register(TaskCrossBowAttack.UID, buildWithMainHandAndBackPack1AndPlaceAll(Items.CROSSBOW.getDefaultInstance(), 1, 1, Items.ARROW.getDefaultInstance(), 128, 32));
        event.register(TaskFishing.UID, buildWithMainHandAndPlaceAll(Items.FISHING_ROD.getDefaultInstance(), 1, 1));
        event.register(TaskDanmakuAttack.UID, buildWithMainHandAndPlaceAll(InitItems.HAKUREI_GOHEI.get().getDefaultInstance(), 1, 1));
        event.register(TaskGrass.UID, commonPlaceAll);
        event.register(TaskMilk.UID, new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(
                                        Items.BUCKET.getDefaultInstance()
                                ),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.ALL,
                                5,
                                1,
                                64
                        )
                )
        ));
    }

    public static ConfigurableCommunicateData buildWithMainHandAndPlaceAll(ItemStack mainHand, int max, int min) {
        return new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(mainHand),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.MAIN_HAND,
                                max,
                                min,
                                -1
                        ),
                        new ConfigurableCommunicateData.Item(
                                List.of(),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.ETA,
                                0,
                                0,
                                -1
                        ),
                        new ConfigurableCommunicateData.Item(
                                List.of(),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.OFF_HAND,
                                0,
                                0,
                                -1
                        )
                )
        );
    }

    public static ConfigurableCommunicateData buildWithMainHandAndBackPack1AndPlaceAll(ItemStack mainHand, int max, int min, ItemStack backPack1, int max1, int min1) {
        return new ConfigurableCommunicateData(
                List.of(
                        new ConfigurableCommunicateData.Item(
                                List.of(
                                        mainHand
                                ),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.MAIN_HAND,
                                max,
                                min,
                                -1
                        ),
                        new ConfigurableCommunicateData.Item(
                                List.of(
                                        backPack1
                                ),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.ETA,
                                max1,
                                min1,
                                -1
                        ),
                        new ConfigurableCommunicateData.Item(
                                List.of(),
                                true,
                                ItemStackUtil.MATCH_TYPE.NOT_MATCHING,
                                SlotType.OFF_HAND,
                                0, 0, -1
                        )
                )
        );
    }

    public static ConfigurableCommunicateData get(ResourceLocation id) {
        return taskDefaultCommunicateData.get(id);
    }

    public static Component getTranslate(ResourceLocation id) {
        if (translations.containsKey(id))
            return translations.get(id);
        return Component.literal(id.toLanguageKey());
    }

    public static void each(Consumer<ResourceLocation> consumer) {
        taskDefaultCommunicateData.keySet().forEach(consumer);
    }
}
