package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOptionSet;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CraftGuideOperator extends TargetOperator {
    public static CraftGuideOperator INSTANCE = new CraftGuideOperator();

    public <T> ActionOptionSet makeActionOptionSet(ActionOption<T> option, T selection) {
        return ActionOptionSet.with(option, selection);
    }

    public <T> ActionOptionSet makeActionOptionSetWithValue(ActionOption<T> option, T selection, String value) {
        return ActionOptionSet.with(option, selection, value);
    }

    public ActionOptionSet makeActionOptionSetOptional(boolean optional){
        return makeActionOptionSet(ActionOption.OPTIONAL, optional);
    }

    @Deprecated
    public CraftGuideStepData makeCraftGuideStepData(
            Target target,
            ItemStack[] input,
            ItemStack[] output,
            ResourceLocation action,
            boolean optional
    ) {
        return makeCraftGuideStepDataWithExtra(target, input, output, action, optional, new CompoundTag());
    }

    @Deprecated
    public CraftGuideStepData makeCraftGuideStepDataWithExtra(
            Target target,
            ItemStack[] input,
            ItemStack[] output,
            ResourceLocation action,
            boolean optional,
            CompoundTag extra
    ) {
        return new CraftGuideStepData(target, List.of(input), List.of(output), action, optional, extra);
    }

    public CraftGuideStepData makeCraftGuideStepDataWithOptions(Target target, ItemStack[] input, ItemStack[] output, ResourceLocation action, ActionOptionSet options) {
        return new CraftGuideStepData(target, List.of(input), List.of(output), action, options);
    }

    public CraftGuideData makeCraftGuideData(CraftGuideStepData[] steps, ResourceLocation id) {
        return new CraftGuideData(List.of(steps), id);
    }

    public CraftGuideData simpleCommonItemHandlerIO(BlockPos pos, ItemStack[] input, ItemStack[] output) {
        return new CraftGuideData(
                List.of(
                        new CraftGuideStepData(
                                new Target(ItemHandlerStorage.TYPE, pos),
                                List.of(input),
                                List.of(),
                                CommonPlaceItemAction.TYPE
                        ), new CraftGuideStepData(
                                new Target(ItemHandlerStorage.TYPE, pos),
                                List.of(),
                                List.of(output),
                                CommonTakeItemAction.TYPE
                        )
                ),
                CommonType.TYPE);
    }

    public void forEach3Items(ItemStack[] input, Consumer<ItemStack[]> consumer) {
        for (int i = 0; i < input.length; i += 3) {
            ItemStack[] toDeal = new ItemStack[Math.min(3, input.length - i)];
            for (int j = 0; j < toDeal.length; j++) toDeal[j] = input[i + j];
            consumer.accept(toDeal);
        }
    }

    public <T> List<T> mapEach3Items(ItemStack[] input, Function<ItemStack[], T> consumer) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < input.length; i += 3) {
            ItemStack[] toDeal = new ItemStack[Math.min(3, input.length - i)];
            for (int j = 0; j < toDeal.length; j++) toDeal[j] = input[i + j];
            result.add(consumer.apply(toDeal));
        }
        return result;
    }
}