package studio.fantasyit.maid_storage_manager.debug;

import com.github.tartaricacid.touhoulittlemaid.debug.target.DebugTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.Logger;
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
                .map(suppressed -> new DebugTarget(suppressed.pos,
                        0x4f00ff00,
                        "Suppress",
                        500)
                )
                .toList();
    }

    public enum InvChange {
        IN,
        OUT,
        CURRENT
    }

    public static void invChange(InvChange type, EntityMaid maid, ItemStack stack) {
        if (!Config.enableDebugInv) return;
        switch (type) {
            case IN -> Logger.debug("%s>[IN] %s", maid.getUUID(), stack.toString());
            case OUT -> Logger.debug("%s>[OUT] %s", maid.getUUID(), stack.toString());
        }
        StringBuilder sb = new StringBuilder();
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);
        for (int i = 0; i < availableInv.getSlots(); i++) {
            ItemStack itemStack = availableInv.getStackInSlot(i);
            if (!itemStack.isEmpty())
                sb.append(itemStack).append(" ");
        }
        Logger.debug("%s>[INV] %s", maid.getUUID(), sb.toString());
    }
}