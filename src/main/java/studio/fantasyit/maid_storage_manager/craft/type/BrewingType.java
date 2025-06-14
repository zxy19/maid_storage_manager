package studio.fantasyit.maid_storage_manager.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
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
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.menu.craft.brewing.BrewingCraftMenu;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class BrewingType implements ICraftType {

    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "brewing");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public ResourceLocation getActionType() {
        return TYPE;
    }

    @Override
    public ItemStack getIcon() {
        return Items.BREWING_STAND.getDefaultInstance();
    }

    @Override
    public @Nullable AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        ResourceLocation type = craftGuideStepData.getActionType();
        return CraftManager.getInstance().start(type, maid, craftGuideData, craftGuideStepData, layer);
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
    public List<CraftGuideStepData> transformSteps(List<CraftGuideStepData> steps) {
        CraftGuideStepData craftGuideStepData = ICraftType.super.transformSteps(steps).get(0);
        List<CraftGuideStepData> ret = new ArrayList<>();
        Target target = new Target(ItemHandlerStorage.TYPE, craftGuideStepData.getStorage().pos);
        //烈焰粉
        ret.add(new CraftGuideStepData(
                target,
                List.of(craftGuideStepData.getInput().get(0)),
                List.of(),
                CommonPlaceItemAction.TYPE,
                true,
                new CompoundTag()
        ));
        ret.add(new CraftGuideStepData(
                target,
                List.of(craftGuideStepData.getInput().get(1),craftGuideStepData.getInput().get(2)),
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
