package studio.fantasyit.maid_storage_manager.integration.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.network.IngredientRequestResultS2CPacket;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.List;

public class IngredientRequest {
    public static void onRequest(ServerPlayer actor, List<ItemStack> data, int maidId) {
        Level level = actor.level();
        if (level.getEntity(maidId) instanceof EntityMaid maid && maid.isOwnedBy(actor)) {
            if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.CO_WORK) {
                PacketDistributor.sendToPlayer(actor,
                        new IngredientRequestResultS2CPacket(Component.translatable("gui.maid_storage_manager.jei_request.invalid", maid.getDisplayName())));
                return;
            }
            ItemStack listItem = RequestItemUtil.makeVirtualItemStack(data, null, actor, "JEI");
            if (!InvUtil.tryPlace(maid.getAvailableInv(true), listItem).isEmpty()) {
                PacketDistributor.sendToPlayer(actor,
                        new IngredientRequestResultS2CPacket(Component.translatable("gui.maid_storage_manager.jei_request.full", maid.getDisplayName())));
                return;
            }
            PacketDistributor.sendToPlayer(actor,
                    new IngredientRequestResultS2CPacket(Component.translatable("gui.maid_storage_manager.jei_request.start", maid.getDisplayName())));
        } else {
            PacketDistributor.sendToPlayer(actor,
                    new IngredientRequestResultS2CPacket(Component.translatable("gui.maid_storage_manager.jei_request.not_found")));
            return;
        }
    }
}
