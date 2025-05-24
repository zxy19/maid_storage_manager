package studio.fantasyit.maid_storage_manager.debug;

import com.github.tartaricacid.touhoulittlemaid.debug.target.DebugTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.network.DebugDataPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

public class DebugData {
    public static void sendDebug(String msg, Object... a) {
        Network.INSTANCE.send(PacketDistributor.ALL.noArg(), new DebugDataPacket(String.format(msg, a)));
    }

    public static List<DebugTarget> commonTargets(EntityMaid maid) {
        List<DebugTarget> targets = new ArrayList<>();
        targets.addAll(commonTarget(maid, MemoryUtil.getViewedInventory(maid), "View Target"));
        targets.addAll(commonTarget(maid, MemoryUtil.getPlacingInv(maid), "Place Target"));
        targets.addAll(commonTarget(maid, MemoryUtil.getCrafting(maid), "Craft Target"));
        targets.addAll(commonTarget(maid, MemoryUtil.getLogistics(maid), "Logistics Target"));
        targets.addAll(commonTarget(maid, MemoryUtil.getResorting(maid), "Resort Target"));
        targets.addAll(commonTarget(maid, MemoryUtil.getRequestProgress(maid), "Request Target"));
        return targets;
    }

    public static List<DebugTarget> commonTarget(EntityMaid maid, AbstractTargetMemory memory, String name) {
        if (memory.hasTarget())
            return List.of(new DebugTarget(memory.getTarget().pos,
                    0xffff0000,
                    name,
                    500));
        return List.of();
    }

    public static List<DebugTarget> placeSuppresses(EntityMaid maid) {
        return MemoryUtil
                .getPlacingInv(maid)
                .getSuppressedPos()
                .stream()
                .map(suppressed -> new DebugTarget(suppressed.target().pos,
                        0xff00ff00,
                        "Suppress_" + suppressed.type().name(),
                        500)
                )
                .toList();
    }
}