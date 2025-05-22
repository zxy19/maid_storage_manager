package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.items.ChangeFlag;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftAssets;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
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
    private static final float[][] colors = new float[][]{colors_b, colors_g, colors_y};

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
            renderForInv(event, mc, floating);
            renderForLogistics(event, mc, floating);
            renderForEntity(event, mc);
        }
    }

    private static void renderForLogistics(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.LOGISTICS_GUIDE.get()) {
            return;
        }
        Target input = LogisticsGuide.getInput(mainStack);
        if (input != null) {
            BoxRenderUtil.renderStorage(input,
                    colors_b,
                    event,
                    Component.translatable("maid_storage_manager.logistics_guide_binding_extract").getString(),
                    floating);
        }

        Target output = LogisticsGuide.getOutput(mainStack);
        if (output != null) {
            BoxRenderUtil.renderStorage(output,
                    colors_g,
                    event,
                    Component.translatable("maid_storage_manager.logistics_guide_binding_store").getString(),
                    floating);
        }
    }

    private static void renderForEntity(RenderLevelStageEvent event, Minecraft mc) {
        if (mc.level == null) return;
        BindingData.getEntityIds().forEach(id -> {
            Entity entity = mc.level.getEntity(id);
            if (entity == null) {
                return;
            }
            BoxRenderUtil.renderEntity(entity, colors_p, event,
                    Component.translatable("maid_storage_manager.request_list_binding_render").getString());
        });
    }

    private static void renderForRequest(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.REQUEST_LIST_ITEM.get()) {
            return;
        }
        Target storage = RequestListItem.getStorageBlock(mainStack);
        if (storage != null) {
            BoxRenderUtil.renderStorage(storage, colors_p, event, Component.translatable("maid_storage_manager.request_list_binding_render").getString(),
                    floating);
        }
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
        List<Target> storage = StorageDefineBauble.getStorages(mainStack);
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
        for (Target storage1 : storage) {
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
        List<Target> storage = ChangeFlag.getStorages(mainStack);
        if (storage == null || storage.isEmpty()) {
            return;
        }
        for (Target storage1 : storage) {
            BoxRenderUtil.renderStorage(storage1,
                    colors_r,
                    event,
                    Component.translatable("maid_storage_manager.changed_flag_binding_render.changed").getString(),
                    floating);
        }
    }

    private static void renderForCraftGuide(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        boolean noRenderSelecting = false;
        if (mainStack.getItem() != ItemRegistry.CRAFT_GUIDE.get()) {
            if (mainStack.getItem() == ItemRegistry.LOGISTICS_GUIDE.get()) {
                mainStack = LogisticsGuide.getCraftGuideItemStack(mainStack);
                noRenderSelecting = true;
                if (mainStack.isEmpty())
                    return;
            } else {
                return;
            }
        }
        CraftGuideRenderData data = CraftGuideRenderData.fromItemStack(mainStack);
        for (int i = 0; i < data.stepBindings.size(); i++) {
            Pair<Target, ResourceLocation> step = data.stepBindings.get(i);
            BoxRenderUtil.renderStorage(step.getA(),
                    colors[i % colors.length],
                    event,
                    "[" + (i + 1) + "]" + CommonCraftAssets.translationForAction(step.getB()).getString(),
                    floating,
                    (i == data.selecting && !noRenderSelecting ? 0xe91e63 : 0xffffff)
            );
        }
        if (!noRenderSelecting)
            if (data.selecting != -1 && data.selecting < data.stepBindings.size()) {
                Pair<Target, ResourceLocation> step = data.stepBindings.get(data.selecting);
                BoxRenderUtil.renderStorage(step.getA(),
                        colors_r,
                        event,
                        Component.translatable("interaction.craft_guide_selecting").getString(),
                        floating
                );
            }
    }

    private static void renderForInv(RenderLevelStageEvent event, Minecraft mc, Map<BlockPos, Integer> floating) {
        if (InventoryListDataClient.showingInv == null)
            return;
        for (int i = 0; i < InventoryListDataClient.showingInv.posAndSlot.size(); i++) {
            Pair<Target, Integer> storageIntegerPair = InventoryListDataClient.showingInv.posAndSlot.get(i);
            BoxRenderUtil.renderStorage(
                    storageIntegerPair.getA(),
                    colors_y,
                    event,
                    Component.translatable("maid_storage_manager.inventory_list_render.inv",
                                    InventoryListDataClient.showingInv.itemStack.getDisplayName().getString(),
                                    storageIntegerPair.getB()
                            )
                            .getString(),
                    floating
            );
        }
    }
}