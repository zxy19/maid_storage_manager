package studio.fantasyit.maid_storage_manager.debug;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

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
                    NbtUtils.writeBlockPos(pos.currentBlockPosition())
            );
        }, () -> DebugData.getInstance().setDataAndSync("target_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.PLACING_INVENTORY.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("placing_" + maid.getUUID(),
                    NbtUtils.writeBlockPos(pos.getTargetPos())
            );
        }, () -> DebugData.getInstance().setDataAndSync("placing_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.VIEWED_INVENTORY.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("viewing_" + maid.getUUID(),
                    NbtUtils.writeBlockPos(pos.getTargetPos())
            );
        }, () -> DebugData.getInstance().setDataAndSync("viewing_" + maid.getUUID(), new CompoundTag()));

        maid.getBrain().getMemory(MemoryModuleRegistry.REQUEST_PROGRESS.get()).ifPresentOrElse(pos -> {
            DebugData.getInstance().setDataAndSync("finding_" + maid.getUUID(),
                    NbtUtils.writeBlockPos(pos.getTargetPos())
            );
        }, () -> DebugData.getInstance().setDataAndSync("finding_" + maid.getUUID(), new CompoundTag()));
    }
}
