package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.NativeObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CraftGuideOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.util.FunctionUtil;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.AbstractObjectWrapped;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KJSCraftType extends AbstractObjectWrapped implements IKJSCraftType.Full, ICraftType {
    public KJSCraftType(NativeObject obj, Context context) {
        super(obj, context);
    }

    @Override
    public ResourceLocation getType() {
        return get("type", this::resourceLocationParser).orElseThrow();
    }

    @Override
    public ResourceLocation getActionType() {
        return get("actionType", this::resourceLocationParser).orElseThrow();
    }

    @Override
    public ItemStack getIcon() {
        return get("icon", this.classTest(ItemStack.class)).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean isSpecialType(ServerLevel level, BlockPos pos, Direction direction) {
        return get("isSpecialType", this::booleanParser, level, pos, direction).orElse(false);
    }

    @Override
    public List<CraftGuideStepData> transformSteps(List<CraftGuideStepData> steps) {
        return get("transformSteps", t -> {
            if (t instanceof NativeArray array) {
                List<CraftGuideStepData> result = new ArrayList<>();
                for (int i = 0; i < array.getLength(); i++) {
                    if (array.get(i) instanceof CraftGuideStepData data)
                        result.add(data);
                    else
                        throw new RuntimeException("step is not CraftGuideStepData");
                }
                return result;
            } else if (t instanceof List<?> list) {
                if (list.isEmpty() || list.get(0) instanceof CraftGuideStepData) {
                    return (List<CraftGuideStepData>) list;
                } else {
                    throw new RuntimeException("step is not CraftGuideStepData");
                }
            }
            return null;
        }, steps.toArray(new CraftGuideStepData[0]), CraftGuideOperator.INSTANCE).orElseGet(() -> ICraftType.super.transformSteps(steps));
    }

    @Override
    public @Nullable AbstractContainerMenu createGui(int containerId, Level level, Player player, CraftGuideData data) {
        return get("createGui", this.classTest(AbstractContainerMenu.class),
                containerId, level, player, data
        ).orElse(null);
    }


    @Override
    public boolean available(CraftGuideData craftGuideData) {
        return get("available", this::booleanParser, craftGuideData).orElse(true);
    }

    @Override
    public @Nullable AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        Supplier<AbstractCraftActionContext> def = FunctionUtil.wrap(() -> {
            ResourceLocation type = craftGuideStepData.getActionType();
            return CraftManager.getInstance().start(type, maid, craftGuideData, craftGuideStepData, layer);
        });
        return get("start", this.classTest(AbstractCraftActionContext.class),
                def, maid, craftGuideData, craftGuideStepData, layer
        ).orElseGet(def);
    }

    @Override
    public void onTypeUsing(ServerPlayer player, ItemStack itemStack, CraftGuideData craftGuideData) {
        get("onTypeUsing", this::any, player, itemStack, craftGuideData, CraftGuideOperator.INSTANCE);
    }

    @ApiStatus.Internal
    @Override
    public ResourceLocation type() {
        return null;
    }

    @ApiStatus.Internal
    @Override
    public ResourceLocation actionType() {
        return null;
    }

    @ApiStatus.Internal
    @Override
    public ItemStack icon() {
        return null;
    }

    @ApiStatus.Internal
    @Override
    public @Nullable AbstractCraftActionContext start(Supplier<AbstractCraftActionContext> parent, EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        return null;
    }

    @ApiStatus.Internal
    @Override
    public CraftGuideStepData[] transformSteps(CraftGuideStepData[] steps, CraftGuideOperator operator) {
        return null;
    }

    @ApiStatus.Internal
    @Override
    public void onTypeUsing(ServerPlayer player, ItemStack itemStack, CraftGuideData craftGuideData, CraftGuideOperator operator) {
    }
}
