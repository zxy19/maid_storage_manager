package studio.fantasyit.maid_storage_manager.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftMenu;

import java.util.List;

public class CommonType implements ICraftType {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "common");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public ResourceLocation getActionType() {
        return null;
    }

    @Override
    public @Nullable AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        ResourceLocation type = craftGuideData.getType();
        return CraftManager.getInstance().start(type, maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return false;
    }

    @Override
    public void getTooltip(CraftGuideData data, List<Component> toolTip) {

    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return new CommonCraftMenu(containerId, player);
    }

    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return false;
    }
}
