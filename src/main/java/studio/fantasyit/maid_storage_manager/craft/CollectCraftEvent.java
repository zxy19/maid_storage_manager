package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class CollectCraftEvent extends Event implements IModBusEvent {
    private final List<ICraftType> craftTypes;
    private final List<CraftAction> actions;
    private final List<IAutoCraftGuideGenerator> autoCraftGuideGenerators;
    private final Map<ResourceLocation, List<BiPredicate<ItemStack, ItemStack>>> itemStackPredicates;

    public CollectCraftEvent(List<ICraftType> craftTypes, List<CraftAction> actions, List<IAutoCraftGuideGenerator> autoCraftGuideGenerators, Map<ResourceLocation, List<BiPredicate<ItemStack, ItemStack>>> itemStackPredicates) {
        this.craftTypes = craftTypes;
        this.actions = actions;
        this.autoCraftGuideGenerators = autoCraftGuideGenerators;
        this.itemStackPredicates = itemStackPredicates;
    }

    public List<ICraftType> getCraftTypes() {
        return craftTypes;
    }

    /**
     * 添加一个合成类型
     *
     * @param craftType 合成类型
     */
    public synchronized void addCraftType(ICraftType craftType) {
        craftTypes.add(craftType);
    }

    /**
     * 添加一个合成操作
     *
     * @param type                                 类型ID
     * @param craftActionProvider                  操作类型提供者。一般为类构造函数
     * @param craftActionPathFindingTargetProvider 寻路提供者。一般可以在PathTargetLocator中找到
     * @param closeEnoughThreshold                 距离阈值。判断到达目标的条件
     * @param isCommon                             是否可以在一般类型中选择
     * @param hasInput                             输入数量
     * @param hasOutput                            输出数量
     */
    public void addAction(ResourceLocation type,
                          CraftAction.CraftActionProvider craftActionProvider,
                          CraftAction.CraftActionPathFindingTargetProvider craftActionPathFindingTargetProvider,
                          double closeEnoughThreshold,
                          boolean isCommon,
                          int hasInput,
                          int hasOutput,
                          List<ActionOption<?>> options
    ) {
        addAction(type, craftActionProvider, craftActionPathFindingTargetProvider, closeEnoughThreshold, isCommon, false, hasInput, hasOutput, options);
    }


    /**
     * 添加一个合成操作
     *
     * @param type                                 类型ID
     * @param craftActionProvider                  操作类型提供者。一般为类构造函数
     * @param craftActionPathFindingTargetProvider 寻路提供者。一般可以在PathTargetLocator中找到
     * @param closeEnoughThreshold                 距离阈值。判断到达目标的条件
     * @param isCommon                             是否可以在一般类型中选择
     * @param hasInput                             输入数量
     * @param hasOutput                            输出数量
     * @param options                              选项，如果是
     */
    public synchronized void addAction(ResourceLocation type,
                          CraftAction.CraftActionProvider craftActionProvider,
                          CraftAction.CraftActionPathFindingTargetProvider craftActionPathFindingTargetProvider,
                          double closeEnoughThreshold,
                          boolean isCommon,
                          long marks,
                          int hasInput,
                          int hasOutput,
                          List<ActionOption<?>> options
    ) {
        this.actions.add(new CraftAction(type, craftActionProvider, craftActionPathFindingTargetProvider, closeEnoughThreshold, isCommon, marks, hasInput, hasOutput, options));
    }

    public void addAction(ResourceLocation type,
                          CraftAction.CraftActionProvider craftActionProvider,
                          CraftAction.CraftActionPathFindingTargetProvider craftActionPathFindingTargetProvider,
                          double closeEnoughThreshold,
                          boolean isCommon,
                          boolean noOccupy,
                          int hasInput,
                          int hasOutput,
                          List<ActionOption<?>> options
    ) {
        addAction(type, craftActionProvider, craftActionPathFindingTargetProvider, closeEnoughThreshold, isCommon, noOccupy ? CraftAction.MARK_NO_OCCUPATION : CraftAction.MARK_NO_MARKS, hasInput, hasOutput, options);
    }

    /**
     * 添加一个自动合成指南生成器
     *
     * @param autoCraftGuideGenerator 自动合成指南生成器
     */
    public synchronized void addAutoCraftGuideGenerator(IAutoCraftGuideGenerator autoCraftGuideGenerator) {
        this.autoCraftGuideGenerators.add(autoCraftGuideGenerator);
    }

    /**
     * 添加一个物品堆判断器。该判断将在合成过程中被用于比较物品是否相同。
     * @param type
     * @param predicate
     */
    public void addItemStackPredicate(Item type, BiPredicate<ItemStack, ItemStack> predicate) {
        addItemStackPredicate(BuiltInRegistries.ITEM.getKey(type), predicate);
    }

    /**
     * 添加一个物品堆判断器。该判断将在合成过程中被用于比较物品是否相同。
     * @param type
     * @param predicate
     */
    public synchronized void addItemStackPredicate(ResourceLocation type, BiPredicate<ItemStack, ItemStack> predicate) {
        if (!itemStackPredicates.containsKey(type))
            itemStackPredicates.put(type, new ArrayList<>());
        itemStackPredicates.get(type).add(predicate);
    }
}
