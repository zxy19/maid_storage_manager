package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.entity.VirtualDisplayEntity;

public class HangUpItem extends Item {
    public HangUpItem(Properties p_41383_) {
        super(p_41383_);
    }

    public HangUpItem() {
        this(new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            if (Config.generateVirtualItemFrame)
                if (context.getPlayer().isShiftKeyDown()) {
                    BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
                    VirtualDisplayEntity e = VirtualDisplayEntity.create(context.getLevel(),
                            pos,
                            context.getClickedFace(),
                            context.getItemInHand().copyWithCount(1));
                    context.getItemInHand().shrink(1);
                    context.getLevel().addFreshEntity(e);
                    return InteractionResult.SUCCESS;
                }
        }
        return super.useOn(context);
    }

    public boolean allowClickThrough() {
        return true;
    }
}
