package studio.fantasyit.maid_storage_manager.craft.type;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

public class AE2Type implements ICraftType {

    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "ae2_craft");

    @Override
    public @NotNull Identifier getType() {
        return TYPE;
    }

    @Override
    public @NotNull Identifier getActionType() {
        return TYPE;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.STONE.getDefaultInstance();
    }

    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return false;
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return null;
    }

    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return craftGuideData.getOutput().stream().anyMatch(itemStack -> !itemStack.isEmpty());
    }
}
