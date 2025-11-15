package studio.fantasyit.maid_storage_manager.integration.tour_guide.tours;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.step.TourStepId;
import studio.fantasyit.tour_guide.trigger.TriggerKey;

public class InventoryListTour {
    public static final ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "inventory_list");
    public static final TourStepId<EntityMaid> STEP_MAID = new TourStepId<>(new ResourceLocation(MaidStorageManager.MODID, "inventory_list_maid"), EntityMaid.class);
    public static final TourStepId<ItemStack> STEP_TARGET_ITEM = new TourStepId<>(new ResourceLocation(MaidStorageManager.MODID, "inventory_list_itemstack"), ItemStack.class);

    public static final TriggerKey<Boolean> TRIGGER_WRITE_INV = new TriggerKey<>(new ResourceLocation(MaidStorageManager.MODID, "maid_write_inv_list"), Boolean.class);
    public static final TriggerKey<CompoundTag> TRIGGER_CLICK_INV = new TriggerKey<>(new ResourceLocation(MaidStorageManager.MODID, "player_click_inventory_list_item"), CompoundTag.class);

    public static final ResourceLocation GUI_INVENTORY_SCREEN = new ResourceLocation(MaidStorageManager.MODID, "gui/inventory_list");



}
