package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.data.BoxTip;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.items.ChangeFlag;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.items.WorkCardItem;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackData;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftAssets;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.BoxRenderUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public final class BindingRender {
    private static final float[] colors_g = new float[]{0.40f, 0.73f, 0.42f, 1};
    private static final float[] colors_r = new float[]{0.91f, 0.12f, 0.39f, 1};
    private static final float[] colors_b = new float[]{0.10f, 0.46f, 0.82f, 1};
    private static final float[] colors_y = new float[]{0.91f, 0.73f, 0.0f, 1};
    private static final float[] colors_p = new float[]{0.37f, 0.21f, 0.69f, 1};
    private static final float[][] colors = new float[][]{colors_b, colors_g, colors_y};

    @SubscribeEvent
    public static void onRender(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        var poseStack = event.getPoseStack();
        var submitNodeCollector = event.getSubmitNodeCollector();
        var camera = event.getLevelRenderState().cameraRenderState;
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        Map<BlockPos, Integer> floating = new ConcurrentHashMap<>();
        renderForRequest(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
        renderForStorage(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
        renderForCraftGuide(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
        renderForFlag(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
        renderForInv(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
        renderForLogistics(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
        renderForEntity(poseStack, submitNodeCollector, camera, partialTick, mc);
        renderForWorkCard(poseStack, submitNodeCollector, camera, mc);
    }

    private static void renderForLogistics(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.LOGISTICS_GUIDE.get()) {
            return;
        }
        Target input = LogisticsGuide.getInput(mainStack);
        if (input != null) {
            BoxRenderUtil.renderStorage(input,
                    colors_b,
                    poseStack,
                    submitNodeCollector,
                    camera,
                    Component.translatable("maid_storage_manager.logistics_guide_binding_extract").getString(),
                    floating);
        }

        Target output = LogisticsGuide.getOutput(mainStack);
        if (output != null) {
            BoxRenderUtil.renderStorage(output,
                    colors_g,
                    poseStack,
                    submitNodeCollector,
                    camera,
                    Component.translatable("maid_storage_manager.logistics_guide_binding_store").getString(),
                    floating);
        }
    }

    private static void renderForEntity(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc) {
        if (mc.level == null) return;
        BindingData.getEntityIds().forEach(id -> {
            Entity entity = mc.level.getEntity(id);
            if (entity == null) {
                return;
            }
            BoxRenderUtil.renderEntity(entity, colors_p,
                    poseStack,
                    submitNodeCollector,
                    camera,
                    partialTick,
                    Component.translatable("maid_storage_manager.request_list_binding_render").getString());
        });
    }

    private static void renderForRequest(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.REQUEST_LIST_ITEM.get()) {
            return;
        }
        Target storage = IRequestTaskHandler.of(mainStack) != null ? IRequestTaskHandler.of(mainStack).getStorageBlock(mainStack) : null;
        if (storage != null) {
            BoxRenderUtil.renderStorage(storage, colors_p,
                    poseStack,
                    submitNodeCollector,
                    camera,
                    Component.translatable("maid_storage_manager.request_list_binding_render").getString(),
                    floating);
        }
    }

    private static void renderForStorage(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) {
            if (mainStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                mainStack = mainStack.getOrDefault(DataComponentRegistry.CONTAIN_ITEM, ItemStackData.EMPTY).itemStack();
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
                    poseStack,
                    submitNodeCollector,
                    camera,
                    Component.translatable("maid_storage_manager.storage_define_bauble_binding_render." + mode).getString(),
                    floating);
        }
    }

    private static void renderForFlag(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
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
                    poseStack,
                    submitNodeCollector,
                    camera,
                    Component.translatable("maid_storage_manager.changed_flag_binding_render.changed").getString(),
                    floating);
        }
    }

    private static void renderForCraftGuide(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
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
        CraftGuideRenderData data = mainStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_RENDER, CraftGuideRenderData.EMPTY);
        for (int i = 0; i < data.stepBindings.size(); i++) {
            Pair<Target, Identifier> step = data.stepBindings.get(i);
            BoxRenderUtil.renderStorage(step.getA(),
                    colors[i % colors.length],
                    poseStack,
                    submitNodeCollector,
                    camera,
                    "[" + (i + 1) + "]" + CommonCraftAssets.translationForAction(step.getB()).getString(),
                    floating,
                    (i == data.selecting && !noRenderSelecting ? 0xe91e63 : 0xffffff)
            );
        }
        if (!noRenderSelecting)
            if (data.selecting != -1 && data.selecting < data.stepBindings.size()) {
                Pair<Target, Identifier> step = data.stepBindings.get(data.selecting);
                BoxRenderUtil.renderStorage(step.getA(),
                        colors_r,
                        poseStack,
                        submitNodeCollector,
                        camera,
                        Component.translatable("interaction.craft_guide_selecting").getString(),
                        floating
                );
            }
    }

    private static void renderForInv(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        if (InventoryListDataClient.showingInv.isEmpty() && InventoryListDataClient.commonTips.isEmpty())
            return;
        for (Pair<InventoryItem, MutableInt> pair : InventoryListDataClient.showingInv) {
            InventoryItem inv = pair.getA();
            for (int i = 0; i < inv.posAndSlot.size(); i++) {
                InventoryItem.PositionCount storageIntegerPair = inv.posAndSlot.get(i);
                if (storageIntegerPair.isCraftGuide())
                    BoxRenderUtil.renderStorage(
                            storageIntegerPair.pos(),
                            colors_y,
                            poseStack,
                            submitNodeCollector,
                            camera,
                            Component.translatable("maid_storage_manager.inventory_list_render.inv_craft_guide",
                                            inv.itemStack.getDisplayName().getString()
                                    )
                                    .getString(),
                            floating
                    );
                else
                    BoxRenderUtil.renderStorage(
                            storageIntegerPair.pos(),
                            colors_y,
                            poseStack,
                            submitNodeCollector,
                            camera,
                            Component.translatable("maid_storage_manager.inventory_list_render.inv",
                                            inv.itemStack.getDisplayName().getString(),
                                            storageIntegerPair.count()
                                    )
                                    .getString(),
                            floating
                    );
            }
        }

        for (Pair<BoxTip, MutableInt> pair : InventoryListDataClient.commonTips) {
            BoxRenderUtil.renderStorage(
                    pair.getA().target(),
                    pair.getA().argb(),
                    poseStack,
                    submitNodeCollector,
                    camera,
                    pair.getA().tip().getString(),
                    floating
            );
        }
    }

    private static void renderForWorkCard(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Minecraft mc) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.WORK_CARD.get()) return;

        List<EntityMaid> entities = mc.level.getEntities(
                EntityTypeTest.forClass(EntityMaid.class),
                mc.player.getBoundingBox().inflate(32),
                t -> true
        );

        for (EntityMaid maid : entities) {
            BaubleItemHandler baubleItemHandler = maid.getMaidBauble();
            float oh = (float) (maid.getBoundingBox().maxY - maid.getBoundingBox().minY) + 0.5f;
            for (int i = 0; i < baubleItemHandler.size(); i++) {
                ItemStack slotStack = ItemUtil.getStack(baubleItemHandler, i);
                if (slotStack.is(ItemRegistry.WORK_CARD.get())) {
                    if (WorkCardItem.matches(slotStack, mainStack)) {
                        BoxRenderUtil.drawText(
                                poseStack,
                                mc,
                                camera,
                                submitNodeCollector,
                                maid.getPosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)),
                                slotStack.getHoverName().getString(),
                                0xFFFFFFFF,
                                oh
                        );
                        oh += 0.3f;
                    }
                }
            }
        }
    }
}
