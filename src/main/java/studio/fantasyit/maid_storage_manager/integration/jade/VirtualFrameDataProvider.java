package studio.fantasyit.maid_storage_manager.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.entity.VirtualDisplayEntity;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.ArrayList;

public class VirtualFrameDataProvider implements IEntityComponentProvider {
    public static final ResourceLocation VANILLA_ID = ResourceLocation.tryParse("minecraft:item_frame");
    protected static final int PER_ROW = 6;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        VirtualDisplayEntity entity = (VirtualDisplayEntity) entityAccessor.getEntity();
        ItemStack itemStack = entity.getItem();
        IElementHelper elements = IElementHelper.get();
        ArrayList<IElement> elList = new ArrayList<>();
        if (itemStack.is(ItemRegistry.FILTER_LIST.get())) {
            ListTag list = itemStack.getOrCreateTag().getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
            int c = 0;
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tmp = list.getCompound(i);
                ItemStack item = ItemStack.of(tmp.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                if (item.isEmpty())
                    continue;
                IElement tmpDisplay = elements.item(item);
                tmpDisplay.translate(new Vec2(0, 0));
                tmpDisplay.size(new Vec2(18, 18));
                elList.add(tmpDisplay);
                if (elList.size() >= PER_ROW) {
                    tooltip.add(elList);
                    elList = new ArrayList<>();
                }
                c++;
            }
            if (!elList.isEmpty())
                tooltip.add(elList);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(MaidStorageManager.MODID, "virtual_frame");
    }
}
