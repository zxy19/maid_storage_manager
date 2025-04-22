package studio.fantasyit.maid_storage_manager.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.context.special.CraftingRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.menu.craft.smithing.SmithingCraftMenu;

public class SmithingType implements ICraftType {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "smithing");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public ResourceLocation getActionType() {
        return CraftingRecipeAction.TYPE;
    }

    @Override
    public ItemStack getIcon() {
        return Items.SMITHING_TABLE.getDefaultInstance();
    }

    @Override
    public @Nullable AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        ResourceLocation type = craftGuideStepData.getActionType();
        return CraftManager.getInstance().start(type, maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos).is(Blocks.SMITHING_TABLE);
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return new SmithingCraftMenu(containerId, player);
    }

    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return craftGuideData.getOutput().stream().anyMatch(itemStack -> !itemStack.isEmpty());
    }
}
