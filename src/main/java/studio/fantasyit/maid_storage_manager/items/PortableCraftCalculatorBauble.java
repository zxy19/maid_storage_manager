package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;

public class PortableCraftCalculatorBauble extends MaidInteractItem implements IMaidBauble {
    public static final String TAG_STORED_RECIPE = "recipes";
    public static final String TAG_STORED_LAYERS = "layers";
    public static final String TAG_PROGRESS = "progress";

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
        CompoundTag tag = calculator.getOrCreateTag();
        if (Conditions.takingRequestList(maid))
            tag.putInt(TAG_STORED_RECIPE, MemoryUtil.getCrafting(maid).craftGuides.size());
        else
            tag.remove(TAG_STORED_RECIPE);
        tag.putInt(TAG_STORED_LAYERS, MemoryUtil.getCrafting(maid).layers.size());
        tag.putInt(TAG_PROGRESS, MemoryUtil.getCrafting(maid).getCurrentLayerIndex());
        calculator.setTag(tag);
    }

    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if (maid.level().isClientSide) return;
        update(maid, baubleItem);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack,
                                @Nullable Level p_41422_,
                                @NotNull List<Component> toolTip,
                                @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.desc").withStyle(ChatFormatting.GRAY));
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getOrCreateTag();
            if (tag.contains(TAG_STORED_RECIPE)) {
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.stored_recipe", tag.getInt(TAG_STORED_RECIPE)));
            } else {
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.waiting_for_request"));
            }

            if (tag.contains(TAG_STORED_LAYERS) && tag.getInt(TAG_STORED_LAYERS) != 0) {
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.stored_layers", tag.getInt(TAG_STORED_LAYERS)));
                if (tag.contains(TAG_PROGRESS)) {
                    toolTip.add(Component.translatable("tooltip.maid_storage_manager.portable_craft_calculator.progress", tag.getInt(TAG_PROGRESS)));
                }
            }
        }

    }

    public PortableCraftCalculatorBauble() {
        super(new Properties().stacksTo(1));
    }
}
