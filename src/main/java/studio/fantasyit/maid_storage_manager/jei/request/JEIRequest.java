package studio.fantasyit.maid_storage_manager.jei.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.network.JEIRequestResultPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.List;

public class JEIRequest {
    public static void onRequest(ServerPlayer actor, List<ItemStack> data, int maidId) {
        Level level = actor.level();
        if (level.getEntity(maidId) instanceof EntityMaid maid && maid.isOwnedBy(actor)) {
            if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.CO_WORK) {
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> actor),
                        new JEIRequestResultPacket(Component.translatable("gui.maid_storage_manager.jei_request.invalid", maid.getDisplayName())));
                return;
            }
            ItemStack listItem = RequestItemUtil.makeVirtualItemStack(data, null, actor, "JEI");
            if (!InvUtil.tryPlace(maid.getAvailableInv(true), listItem).isEmpty()) {
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> actor),
                        new JEIRequestResultPacket(Component.translatable("gui.maid_storage_manager.jei_request.full", maid.getDisplayName())));
                return;
            }
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> actor),
                    new JEIRequestResultPacket(Component.translatable("gui.maid_storage_manager.jei_request.start", maid.getDisplayName())));
        } else {
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> actor),
                    new JEIRequestResultPacket(Component.translatable("gui.maid_storage_manager.jei_request.not_found")));
            return;
        }
    }
}
