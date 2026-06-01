package studio.fantasyit.maid_storage_manager.mixin;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import studio.fantasyit.maid_storage_manager.api.IItemFrameRenderStateItemVisitor;

@Mixin(ItemFrameRenderState.class)
public abstract class ItemFrameRenderStateMixin extends EntityRenderState implements IItemFrameRenderStateItemVisitor {
    @Unique
    public ItemStack maid_storage_manager$rendering_item = null;
    @Unique
    public boolean maid_storage_manager$virtual = false;

    @Override
    public ItemStack maid_storage_manager$getItem() {
        return maid_storage_manager$rendering_item;
    }

    @Override
    public void maid_storage_manager$setItem(ItemStack item) {
        maid_storage_manager$rendering_item = item;
    }

    @Override
    public void maid_storage_manager$virtualItemFrameRender(boolean render) {
        maid_storage_manager$virtual = render;
    }

    @Override
    public boolean maid_storage_manager$virtualItemFrameRender() {
        return maid_storage_manager$virtual;
    }
}
