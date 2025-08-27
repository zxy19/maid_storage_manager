package studio.fantasyit.maid_storage_manager.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.CollectCraftEvent;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.context.VirtualAction;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context.KJSWrapCraftContext;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.contextSupplier.IKJSCraftContextSupplier;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.generator.IKJSAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.type.IKJSCraftType;

import java.util.function.BiPredicate;

public class KJSCraftEvent extends EventJS {

    private final CollectCraftEvent event;

    public KJSCraftEvent(CollectCraftEvent event) {
        this.event = event;
    }

    public void addCraftGuideGenerator(IKJSAutoCraftGuideGenerator a) {
        if (a instanceof IAutoCraftGuideGenerator generator) {
            event.addAutoCraftGuideGenerator(generator);
        }
    }

    public void addCraftGuideGeneratorFull(IKJSAutoCraftGuideGenerator.Full a) {
        if (a instanceof IAutoCraftGuideGenerator generator) {
            event.addAutoCraftGuideGenerator(generator);
        }
    }

    public void addCraftType(IKJSCraftType a) {
        if (a instanceof ICraftType type) {
            event.addCraftType(type);
        }
    }

    public void addCraftTypeFull(IKJSCraftType.Full a) {
        if (a instanceof ICraftType type) {
            event.addCraftType(type);
        }
    }


    public void addActionSimple(ResourceLocation type, IKJSCraftContextSupplier craftActionProvider, boolean isCommon, int input, int output) {
        event.addAction(type,
                (a, b, c, d) -> new KJSWrapCraftContext(a, b, c, d, craftActionProvider.get()),
                PathTargetLocator::commonNearestAvailablePos,
                2,
                isCommon,
                false,
                input,
                output);
    }

    public void addActionSimpleNoOccupation(ResourceLocation type, IKJSCraftContextSupplier craftActionProvider, boolean isCommon, int input, int output) {
        event.addAction(type,
                (a, b, c, d) -> new KJSWrapCraftContext(a, b, c, d, craftActionProvider.get()),
                PathTargetLocator::commonNearestAvailablePos,
                2,
                isCommon,
                true,
                input,
                output);
    }

    public void addAction(ResourceLocation type, IKJSCraftContextSupplier craftActionProvider, CraftAction.CraftActionPathFindingTargetProvider craftActionPathFindingTargetProvider, double closeEnoughThreshold, boolean isCommon, int hasInput, int hasOutput) {
        event.addAction(type,
                (a, b, c, d) -> new KJSWrapCraftContext(a, b, c, d, craftActionProvider.get()),
                craftActionPathFindingTargetProvider,
                closeEnoughThreshold,
                isCommon,
                false,
                hasInput,
                hasOutput);
    }


    public void addActionNoOccupation(ResourceLocation type, IKJSCraftContextSupplier craftActionProvider, CraftAction.CraftActionPathFindingTargetProvider craftActionPathFindingTargetProvider, double closeEnoughThreshold, boolean isCommon, int hasInput, int hasOutput) {
        event.addAction(type,
                (a, b, c, d) -> new KJSWrapCraftContext(a, b, c, d, craftActionProvider.get()),
                craftActionPathFindingTargetProvider,
                closeEnoughThreshold,
                isCommon,
                true,
                hasInput,
                hasOutput);
    }

    public void addActionVirtual(ResourceLocation type, int hasInput, int hasOutput) {
        event.addAction(type,
                VirtualAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                2,
                false,
                true,
                hasInput,
                hasOutput);
    }

    public void addItemStackPredicateRaw(ResourceLocation type, BiPredicate<ItemStack, ItemStack> predicate) {
        event.addItemStackPredicate(type, predicate);
    }

    public void addItemStackPredicate(Item type, BiPredicate<ItemStack, ItemStack> predicate) {
        event.addItemStackPredicate(type, predicate);
    }
}
