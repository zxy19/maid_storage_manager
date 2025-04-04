package studio.fantasyit.maid_storage_manager.debug;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityTickingServer {
    @SubscribeEvent
    public static void onEntityTick(MaidTickEvent event) {
        if (!Config.enableDebug) return;
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide)
            return;
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("target_" + maid.getUUID(),
                    (new Storage(new ResourceLocation("tlm", "target"), pos.currentBlockPosition(), Optional.empty()).toNbt())
            );
        }, () -> DebugData.getInstance().setDataAndSync("target_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.PLACING_INVENTORY.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("placing_" + maid.getUUID(),
                    pos.getTarget().toNbt()
            );
        }, () -> DebugData.getInstance().setDataAndSync("placing_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.VIEWED_INVENTORY.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("viewing_" + maid.getUUID(),
                    pos.getTarget().toNbt()
            );
        }, () -> DebugData.getInstance().setDataAndSync("viewing_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.REQUEST_PROGRESS.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("finding_" + maid.getUUID(),
                    pos.getTarget().toNbt()
            );
        }, () -> DebugData.getInstance().setDataAndSync("finding_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.CRAFTING.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("crafting_" + maid.getUUID(),
                    pos.getTarget().toNbt()
            );
        }, () -> DebugData.getInstance().setDataAndSync("crafting_" + maid.getUUID(), new CompoundTag()));
        maid.getBrain().getMemory(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("co_work_" + maid.getUUID(),
                    pos.toNbt()
            );
        }, () -> DebugData.getInstance().setDataAndSync("co_work_" + maid.getUUID(), new CompoundTag()));

        Path path = maid.getNavigation().getPath();
        if (path != null) {
            int nodeCount = path.getNodeCount();
            int current = path.getNextNodeIndex();
            ListTag nodes = new ListTag();
            for (int i = current; i < nodeCount; i++) {
                nodes.add(NbtUtils.writeBlockPos(path.getNode(i).asBlockPos()));
            }
            CompoundTag tag = new CompoundTag();
            tag.put("nodes", nodes);
            DebugData.getInstance().setDataAndSync("path_" + maid.getUUID(), tag);
        }
    }
}
