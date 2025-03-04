package studio.fantasyit.maid_storage_manager.debug;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
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

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public final class DebugBoxRender {
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

                for (EntityMaid maid : entities) {
                    DebugData.getInstance().getData("finding_" + maid.getUUID()).ifPresent(data -> {
                        if (data.isEmpty()) return;
                        BlockPos pos = NbtUtils.readBlockPos(data);
                        AABB aabb = new AABB(pos).move(position);
                        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
                        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, 0.63F, 0.21F, 0.23F, 0.5F);
                    });

                    DebugData.getInstance().getData("viewing_" + maid.getUUID()).ifPresent(data -> {
                        if (data.isEmpty()) return;
                        BlockPos pos = NbtUtils.readBlockPos(data);
                        AABB aabb = new AABB(pos).move(position);
                        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
                        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, 0.55F, 0.24F, 0.63F, 0.5F);
                    });
                    DebugData.getInstance().getData("placing_" + maid.getUUID()).ifPresent(data -> {
                        if (data.isEmpty()) return;
                        BlockPos pos = NbtUtils.readBlockPos(data);
                        AABB aabb = new AABB(pos).move(position);
                        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
                        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, 0.37F, 0.43F, 0.63F, 0.5F);
                    });

                    DebugData.getInstance().getData("target_" + maid.getUUID()).ifPresent(data -> {
                        if (data.isEmpty()) return;
                        BlockPos pos = NbtUtils.readBlockPos(data);
                        AABB aabb = new AABB(pos).move(position);
                        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
                        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, 0, 1.0F, 0, 0.5F);
                    });
                }
            }
        }
    }
}
