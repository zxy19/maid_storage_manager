package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

public class MaidInteractItem extends Item {

    public MaidInteractItem(Identifier id, Properties p_41383_) {
        super(p_41383_.setId(ResourceKey.create(Registries.ITEM, id)));
    }

    public MaidInteractItem(Identifier id) {
        this(id, new Item.Properties());
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player p_41399_, LivingEntity entity, InteractionHand p_41401_) {
        if (!p_41399_.level().isClientSide() && p_41401_ == InteractionHand.MAIN_HAND && entity instanceof EntityMaid maid) {
            if (p_41399_.getUUID().equals(maid.getOwner().getUUID())) {
                ResourceHandler<ItemResource> inv;
                if (BaubleManager.getBauble(itemStack) != null) {
                    inv = maid.getMaidBauble();
                } else {
                    inv = maid.getAvailableInv(false);
                }
                if (InvUtil.maxCanPlace(inv, itemStack) > 0) {
                    int count = InvUtil.tryPlace(inv, itemStack.copyWithCount(1)).getCount();

                    if (count == 0)
                        p_41399_.getMainHandItem().shrink(1);

                    if (inv instanceof BaubleItemHandler bh) {
                        CompoundTag tag = new CompoundTag();
                        PacketDistributor.sendToPlayer((ServerPlayer) p_41399_,
                                new MaidDataSyncToClientPacket(
                                        MaidDataSyncToClientPacket.Type.BAUBLE,
                                        maid.getId(),
                                        tag
                                )
                        );
                    }

                    return InteractionResult.SUCCESS;
                }

            }
        }
        return super.interactLivingEntity(itemStack, p_41399_, entity, p_41401_);
    }
}
