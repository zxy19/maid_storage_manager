package studio.fantasyit.maid_storage_manager.debug;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.BoxRenderUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public final class DebugBoxRender {
    private static final Map<String, float[]> defs = Map.of(
            "finding", new float[]{0.55F, 0.24F, 0.63F, 0.5F},
            "viewing", new float[]{0.37F, 0.43F, 0.63F, 0.5F},
            "placing", new float[]{0.37F, 0.43F, 0.63F, 0.5F},
            "target", new float[]{0, 1.0F, 0, 0.5F},
            "crafting", new float[]{0.55F, 0.24F, 0.63F, 0.5F}
    );

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (!Config.enableDebug) return;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            Vec3 position = event.getCamera().getPosition().reverse();
            if (mc.level != null) {
                List<EntityMaid> entities = mc.level.getEntities(
                        EntityTypeTest.forClass(EntityMaid.class),
                        new AABB(
                                mc.player.position().subtract(10, 10, 10),
                                mc.player.position().add(10, 10, 10)
                        ),
                        EntitySelector.NO_SPECTATORS
                );


                Map<BlockPos, Integer> floating = new ConcurrentHashMap<>();
                for (EntityMaid maid : entities) {
                    for (Map.Entry<String, float[]> entry : defs.entrySet()) {
                        DebugData.getInstance().getData(entry.getKey() + "_" + maid.getUUID()).ifPresent(data -> {
                            BoxRenderUtil.renderStorage(Storage.fromNbt(data), entry.getValue(), event, entry.getKey(), floating);
                        });
                    }
//                    DebugData.getInstance().getData("target_" + maid.getUUID()).ifPresent(data -> {
//                        if (data.isEmpty()) return;
//                        BlockPos pos = NbtUtils.readBlockPos(data);
//                        AABB aabb = new AABB(pos).move(position);
//                        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
//                        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, 0, 1.0F, 0, 0.5F);
//                    });
                    DebugData.getInstance().getData("path_" + maid.getUUID())
                            .ifPresent(data -> {
                                ListTag nodes = data.getList("nodes", 10);
                                for (int i = 0; i < nodes.size(); i++) {
                                    BlockPos pos = NbtUtils.readBlockPos(nodes.getCompound(i));
                                    BoxRenderUtil.renderStorage(new Storage(new ResourceLocation("minecraft", "air"), pos),
                                            defs.get("target"),
                                            event,
                                            "path_node[" + i + "]",
                                            floating);
                                }
                            });
                }

            }
        }
    }
}
