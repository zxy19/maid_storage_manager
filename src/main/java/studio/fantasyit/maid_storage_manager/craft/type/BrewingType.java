package studio.fantasyit.maid_storage_manager.craft.type;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.craft.brewing.BrewingCraftMenu;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class BrewingType implements ICraftType {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "brewing");

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
        return Items.BREWING_STAND.getDefaultInstance();
    }

    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos).is(WorkBlockTags.BREWING_STAND);
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return new BrewingCraftMenu(containerId, player);
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
        //烈焰粉/药水
        ret.add(new CraftGuideStepData(
                target.sameType(target.pos, Direction.WEST),
                List.of(craftGuideStepData.getInput().get(0), craftGuideStepData.getInput().get(1)),
                List.of(),
                CommonPlaceItemAction.TYPE,
                true,
                new CompoundTag()
        ));
        ret.add(new CraftGuideStepData(
                target.sameType(target.pos, Direction.UP),
                List.of(craftGuideStepData.getInput().get(2)),
                List.of(),
                CommonPlaceItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        ret.add(new CraftGuideStepData(
                target,
                List.of(),
                List.of(craftGuideStepData.getOutput().get(0)),
                CommonTakeItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        return ret;
    }
}
