package studio.fantasyit.maid_storage_manager.craft.type;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

import java.util.List;

/**
 * 合成类型
 */
public interface ICraftType {
    /**
     * 合成类型ID
     *
     * @return 合成类型ID
     */
    @NotNull ResourceLocation getType();

    /**
     * 合成操作ID
     *
     * @return 合成操作ID。注意，该ID必须被注册！否则将在绑定该类型时产生错误。如果计划使用stepTransform，可以使用VirtualAction进行注册。
     */
    @NotNull ResourceLocation getActionType();

    /**
     * 类型代表物品。会被渲染在合成指南右下角。
     *
     * @return 物品
     */
    @NotNull ItemStack getIcon();

    /**
     * 开始合成，获取操作上下文。一般来说该方法不需要改动。
     *
     * @param maid               执行操作的女仆
     * @param craftGuideData     当前进行的合成指南
     * @param craftGuideStepData 当前进行的合成步骤
     * @param layer              当前进行的合成层
     * @return 操作上下文
     */
    @Nullable
    default AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        ResourceLocation type = craftGuideStepData.getActionType();
        return CraftManager.getInstance().start(type, maid, craftGuideData, craftGuideStepData, layer);
    }

    /**
     * 判断目标方块是否是当前类型对应的绑定方块
     *
     * @param level     世界
     * @param pos       方块位置
     * @param direction 方块点击面
     * @return 是否是当前类型对应的绑定方块
     */
    boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction);

    /**
     * 当使用了当前类型。发生在用户使用合成指南绑定方块并成功识别为当前类型时。可以设置初始值。
     *
     * @param player         玩家
     * @param itemStack      合成指南
     * @param craftGuideData 合成指南数据
     */
    default void onTypeUsing(ServerPlayer player, ItemStack itemStack, CraftGuideData craftGuideData) {

    }

    /**
     * 步骤转换。可以用于通过一些其他步骤来组合成为当前合成的完整步骤。
     *
     * @param steps 原始步骤。一般来说是长度为1的列表
     * @return 新的步骤
     * @see FurnaceType#transformSteps(List)
     */
    default @NotNull List<CraftGuideStepData> transformSteps(List<CraftGuideStepData> steps) {
        return steps;
    }

    /**
     * 创建GUI。
     *
     * @param containerId 容器ID
     * @param level       世界
     * @param player      玩家
     * @param data        合成数据
     * @return GUI。如果为空则不会打开GUI
     */
    @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data);

    /**
     * 检查合成是否可用。
     *
     * @param craftGuideData 合成数据
     */
    boolean available(CraftGuideData craftGuideData);
}
