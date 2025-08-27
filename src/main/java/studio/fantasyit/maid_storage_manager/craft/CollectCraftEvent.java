package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class CollectCraftEvent extends Event {
    private final List<ICraftType> craftTypes;
    private final List<CraftAction> actions;
    private final ArrayList<IAutoCraftGuideGenerator> autoCraftGuideGenerators;
    private final Map<ResourceLocation, List<BiPredicate<ItemStack, ItemStack>>> itemStackPredicates;

    public CollectCraftEvent(List<ICraftType> craftTypes, List<CraftAction> actions, ArrayList<IAutoCraftGuideGenerator> autoCraftGuideGenerators, Map<ResourceLocation, List<BiPredicate<ItemStack, ItemStack>>> itemStackPredicates) {
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
    public void addCraftType(ICraftType craftType) {
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
                          boolean noOccupation,
                          int hasInput,
                          int hasOutput) {
        this.actions.add(new CraftAction(type, craftActionProvider, craftActionPathFindingTargetProvider, closeEnoughThreshold, isCommon, noOccupation, hasInput, hasOutput));
    }

    /**
     * 添加一个自动合成指南生成器
     *
     * @param autoCraftGuideGenerator 自动合成指南生成器
     */
    public void addAutoCraftGuideGenerator(IAutoCraftGuideGenerator autoCraftGuideGenerator) {
        this.autoCraftGuideGenerators.add(autoCraftGuideGenerator);
    }

    public void addItemStackPredicate(Item type, BiPredicate<ItemStack, ItemStack> predicate) {
        addItemStackPredicate(BuiltInRegistries.ITEM.getKey(type), predicate);
    }

    public void addItemStackPredicate(ResourceLocation type, BiPredicate<ItemStack, ItemStack> predicate) {
        if (!itemStackPredicates.containsKey(type))
            itemStackPredicates.put(type, new ArrayList<>());
        itemStackPredicates.get(type).add(predicate);
    }
}
