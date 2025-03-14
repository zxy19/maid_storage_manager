package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

public class MaidInteractItem extends Item {

    public MaidInteractItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player p_41399_, LivingEntity entity, InteractionHand p_41401_) {
        if (p_41399_.level().isClientSide && p_41401_ == InteractionHand.MAIN_HAND && p_41399_.isShiftKeyDown() && entity instanceof EntityMaid maid) {
            IItemHandler inv;
            if (BaubleManager.getBauble(itemStack) != null) {
                inv = maid.getMaidBauble();
            } else {
                inv = maid.getMaidInv();
            }
            if (InvUtil.maxCanPlace(inv, itemStack) > 0) {
                itemStack.setCount(InvUtil.tryPlace(maid.getMaidInv(), itemStack).getCount());
            }
        }
        return super.interactLivingEntity(itemStack, p_41399_, entity, p_41401_);
    }
}
