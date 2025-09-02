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
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOptionSet;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.craft.furnace.FurnaceCraftMenu;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class FurnaceType implements ICraftType {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "furnace");

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
        return Items.FURNACE.getDefaultInstance();
    }

    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos).is(WorkBlockTags.FURNACE);
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return new FurnaceCraftMenu(containerId, player);
    }

    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return craftGuideData.getOutput().stream().anyMatch(itemStack -> !itemStack.isEmpty());
    }

    @Override
    public @NotNull List<CraftGuideStepData> transformSteps(List<CraftGuideStepData> steps) {
        CraftGuideStepData craftGuideStepData = ICraftType.super.transformSteps(steps).get(0);
        List<CraftGuideStepData> ret = new ArrayList<>();
        Target target = new Target(ItemHandlerStorage.TYPE, craftGuideStepData.getStorage().pos);
        ret.add(new CraftGuideStepData(
                target.sameType(craftGuideStepData.getStorage().getPos(), Direction.UP),
                List.of(craftGuideStepData.getInput().get(0)),
                List.of(),
                CommonPlaceItemAction.TYPE
        ));
        ret.add(new CraftGuideStepData(
                target.sameType(craftGuideStepData.getStorage().getPos(), Direction.EAST),
                List.of(craftGuideStepData.getInput().get(1)),
                List.of(),
                CommonPlaceItemAction.TYPE,
                ActionOptionSet.with(ActionOption.OPTIONAL,true)
        ));
        ret.add(new CraftGuideStepData(
                target.sameType(craftGuideStepData.getStorage().getPos(), Direction.DOWN),
                List.of(),
                List.of(craftGuideStepData.getOutput().get(0)),
                CommonTakeItemAction.TYPE
        ));
        return ret;
    }
}
