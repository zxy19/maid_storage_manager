package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;

public class PortableCraftCalculatorBauble extends MaidInteractItem implements IMaidBauble {
    public static ItemStack getCalculator(EntityMaid maid) {
        BaubleItemHandler inv = maid.getMaidBauble();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.is(ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void update(EntityMaid maid) {
        ItemStack calculator = getCalculator(maid);
        if (calculator.isEmpty()) return;
        update(maid, calculator);
    }

    public static void update(EntityMaid maid, ItemStack calculator) {
        if (Conditions.takingRequestList(maid))
            calculator.set(DataComponentRegistry.PCC_RECIPES, MemoryUtil.getCrafting(maid).craftGuides.size());
        else
            calculator.remove(DataComponentRegistry.PCC_RECIPES);
    }

    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if (maid.level().isClientSide) return;
        update(maid, baubleItem);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.desc").withStyle(ChatFormatting.GRAY));

        if (itemStack.has(DataComponentRegistry.PCC_RECIPES)) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.stored_recipe", itemStack.get(DataComponentRegistry.PCC_RECIPES)));
        } else {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.waiting_for_request"));
        }

        if (itemStack.has(DataComponentRegistry.PCC_LAYERS) && itemStack.getOrDefault(DataComponentRegistry.PCC_LAYERS, 0) != 0) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.stored_layers", itemStack.get(DataComponentRegistry.PCC_LAYERS)));
            if (itemStack.has(DataComponentRegistry.PCC_PROGRESS)) {
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.progress", itemStack.get(DataComponentRegistry.PCC_PROGRESS)));
            }
        }


    }

    public PortableCraftCalculatorBauble() {
        super(new Properties().stacksTo(1));
    }
}
