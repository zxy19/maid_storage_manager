package studio.fantasyit.maid_storage_manager.craft.type;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.context.special.CraftingRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.menu.craft.crafting_table.CraftingTableCraftMenu;

public class CraftingType implements ICraftType {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "craft");

    @Override
    public @NotNull ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public @NotNull ResourceLocation getActionType() {
        return CraftingRecipeAction.TYPE;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }


    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos).is(WorkBlockTags.CRAFTING_TABLE);
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return new CraftingTableCraftMenu(containerId, player);
    }

    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return craftGuideData.getOutput().stream().anyMatch(itemStack -> !itemStack.isEmpty());
    }
}
