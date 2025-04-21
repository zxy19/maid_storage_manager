package studio.fantasyit.maid_storage_manager.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

import java.util.List;

public interface ICraftType {
    ResourceLocation getType();

    ResourceLocation getActionType();

    ItemStack getIcon();

    @Nullable AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer);

    /**
     * 识别特殊类型方块
     */
    boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction);

    default List<CraftGuideStepData> transformSteps(List<CraftGuideStepData> steps) {
        return steps;
    }

    @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data);

    boolean available(CraftGuideData craftGuideData);
}
