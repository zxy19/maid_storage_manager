package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftGuideData;
import studio.fantasyit.maid_storage_manager.items.ChangeFlag;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.BoxRenderUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public final class BindingRender {
    private static final float[] colors_g = new float[]{0.40f, 0.73f, 0.42f, 1};
    private static final float[] colors_r = new float[]{0.91f, 0.12f, 0.39f, 1};
    private static final float[] colors_b = new float[]{0.10f, 0.46f, 0.82f, 1};
    private static final float[] colors_y = new float[]{0.91f, 0.73f, 0.0f, 1};
    private static final float[] colors_p = new float[]{0.37f, 0.21f, 0.69f, 1};

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            Map<BlockPos, Integer> floating = new ConcurrentHashMap<>();
            renderForRequest(event, mc, floating);
            renderForStorage(event, mc, floating);
            renderForCraftGuide(event, mc, floating);
            renderForFlag(event, mc, floating);
        }
    }

    private static void renderForRequest(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.REQUEST_LIST_ITEM.get()) {
            return;
        }
        Storage storage = RequestListItem.getStorageBlock(mainStack);
        if (storage == null) {
            return;
        }
        BoxRenderUtil.renderStorage(storage, colors_p, event, Component.translatable("maid_storage_manager.request_list_binding_render").getString(),
                floating);
    }

    private static void renderForStorage(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) {
            if (mainStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                mainStack = ItemStack.of(mainStack.getOrCreateTag().getCompound(StorageDefineBauble.TAG_STORAGE_DEFINE));
                if (mainStack.isEmpty())
                    return;
            } else {
                return;
            }
        }
        List<Storage> storage = StorageDefineBauble.getStorages(mainStack);
        String mode = switch (StorageDefineBauble.getMode(mainStack)) {
            case APPEND -> "append";
            case REMOVE -> "remove";
            case REPLACE -> "replace";
            case REPLACE_SPEC -> "replace_spec";
        };
        float[] color = switch (mode) {
            case "append" -> colors_g;
            case "remove" -> colors_r;
            case "replace" -> colors_b;
            case "replace_spec" -> colors_y;
            default -> colors_g;
        };
        if (storage == null || storage.isEmpty()) {
            return;
        }
        for (Storage storage1 : storage) {
            BoxRenderUtil.renderStorage(storage1,
                    color,
                    event,
                    Component.translatable("maid_storage_manager.storage_define_bauble_binding_render." + mode).getString(),
                    floating);
        }
    }

    private static void renderForFlag(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.CHANGE_FLAG.get()) {
            return;
        }
        List<Storage> storage = ChangeFlag.getStorages(mainStack);
        if (storage == null || storage.isEmpty()) {
            return;
        }
        for (Storage storage1 : storage) {
            BoxRenderUtil.renderStorage(storage1,
                    colors_r,
                    event,
                    Component.translatable("maid_storage_manager.changed_flag_binding_render.changed").getString(),
                    floating);
        }
    }

    private static void renderForCraftGuide(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.CRAFT_GUIDE.get()) {
            return;
        }
        CraftGuideData craftGuideData = CraftGuideData.fromItemStack(mainStack);
        if (craftGuideData.getInput1().available()) {
            BoxRenderUtil.renderStorage(craftGuideData.getInput1().getStorage(),
                    colors_g,
                    event,
                    Component.translatable("maid_storage_manager.craft_guide_render.input1").getString(),
                    floating);
        }
        if (craftGuideData.getInput2().available()) {
            BoxRenderUtil.renderStorage(craftGuideData.getInput2().getStorage(),
                    colors_b,
                    event,
                    Component.translatable("maid_storage_manager.craft_guide_render.input2").getString(),
                    floating);
        }
        if (craftGuideData.getOutput().available()) {
            BoxRenderUtil.renderStorage(craftGuideData.getOutput().getStorage(),
                    colors_r,
                    event,
                    Component.translatable("maid_storage_manager.craft_guide_render.output").getString(),
                    floating);
        }
    }


}