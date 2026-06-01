package studio.fantasyit.maid_storage_manager.api;

import net.minecraft.world.item.ItemStack;

public interface IItemFrameRenderStateItemVisitor {
    public void maid_storage_manager$setItem(ItemStack item);
    public ItemStack maid_storage_manager$getItem();
    public boolean maid_storage_manager$virtualItemFrameRender();
    public void maid_storage_manager$virtualItemFrameRender(boolean render);
}
