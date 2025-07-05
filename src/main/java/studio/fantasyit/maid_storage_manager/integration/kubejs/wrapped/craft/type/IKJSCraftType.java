package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CraftGuideOperator;

import java.util.function.Supplier;

public interface IKJSCraftType {
    ResourceLocation type();

    ResourceLocation actionType();

    boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction);

    boolean available(CraftGuideData craftGuideData);

    static interface Full extends IKJSCraftType {
        ItemStack icon();

        @Nullable AbstractCraftActionContext start(Supplier<AbstractCraftActionContext> parent, EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer);

        @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data);

        CraftGuideStepData[] transformSteps(CraftGuideStepData[] steps, CraftGuideOperator operator);

        void onTypeUsing(ServerPlayer player, ItemStack itemStack, CraftGuideData craftGuideData, CraftGuideOperator operator);
    }
}
