package studio.fantasyit.maid_storage_manager.craft.type;

import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.menu.craft.tacz.TaczCraftMenu;

public class TaczType implements ICraftType {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "tacz");

    @Override
    public @NotNull ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public @NotNull ResourceLocation getActionType() {
        return TYPE;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return ModItems.GUN_SMITH_TABLE.get().getDefaultInstance();
    }


    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof GunSmithTableBlockEntity;
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return new TaczCraftMenu(containerId, player);
    }

    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return !craftGuideData.getOutput().isEmpty();
    }
}
