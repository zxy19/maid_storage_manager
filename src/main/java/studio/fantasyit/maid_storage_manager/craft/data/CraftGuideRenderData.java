package studio.fantasyit.maid_storage_manager.craft.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class CraftGuideRenderData {
    public final List<Pair<Target, ResourceLocation>> stepBindings;
    public final List<ItemStack> outputs;
    public final ItemStack icon;
    public final List<ItemStack> inputs;
    public int selecting = -1;

    public CraftGuideRenderData(List<Pair<Target, ResourceLocation>> stepBindings, List<ItemStack> inputs, List<ItemStack> outputs, ItemStack icon) {
        this(stepBindings, inputs, outputs, icon, -1);
    }

    public CraftGuideRenderData(List<Pair<Target, ResourceLocation>> stepBindings, List<ItemStack> inputs, List<ItemStack> outputs, ItemStack icon, int selecting) {
        this.stepBindings = stepBindings;
        this.outputs = outputs;
        this.inputs = inputs;
        this.icon = icon;
        this.selecting = selecting;
    }

    public static final CraftGuideRenderData EMPTY = new CraftGuideRenderData(List.of(), List.of(), List.of(), ItemStack.EMPTY);

    public static void recalculateItemStack(ItemStack itemStack) {
        CraftGuideData craftGuideData = CraftGuideData.fromItemStack(itemStack);
        CompoundTag data = new CompoundTag();
        ItemStack icon1 = CraftManager.getInstance().getType(craftGuideData.getType()).getIcon();
        data.put("icon", ItemStackUtil.saveStack(icon1));
        ListTag inputs = new ListTag();
        for (ItemStack input : craftGuideData.getInput()) {
            if (input.isEmpty())
                continue;
            inputs.add(ItemStackUtil.saveStack(input));
        }
        data.put("inputs", inputs);
        ListTag outputs = new ListTag();
        for (ItemStack output : craftGuideData.getOutput()) {
            if (output.isEmpty())
                continue;
            outputs.add(ItemStackUtil.saveStack(output));
        }
        data.put("outputs", outputs);
        ListTag stepBindings = new ListTag();
        for (CraftGuideStepData stepBinding : craftGuideData.getSteps()) {
            CompoundTag stepBindingTag = new CompoundTag();
            stepBindingTag.put("pos", stepBinding.getStorage().toNbt());
            stepBindingTag.putString("type", stepBinding.action.toString());
            stepBindings.add(stepBindingTag);
        }
        data.put("stepBindings", stepBindings);
        itemStack.getOrCreateTag().put("renderData", data);
    }

    public static CraftGuideRenderData fromItemStack(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains("input1")) {
            CraftGuideData.fromItemStack(itemStack);
            return fromItemStack(itemStack);
        }
        if (tag.contains("renderData")) {
            CompoundTag renderData = tag.getCompound("renderData");
            ItemStack icon = ItemStackUtil.parseStack(renderData.getCompound("icon"));
            ListTag outputs = renderData.getList("outputs", Tag.TAG_COMPOUND);
            List<ItemStack> outputs1 = new ArrayList<>();
            for (int i = 0; i < outputs.size(); i++) {
                outputs1.add(ItemStackUtil.parseStack(outputs.getCompound(i)));
            }
            ListTag inputs = renderData.getList("inputs", Tag.TAG_COMPOUND);
            List<ItemStack> inputs1 = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                inputs1.add(ItemStackUtil.parseStack(inputs.getCompound(i)));
            }
            ListTag stepBindings = renderData.getList("stepBindings", Tag.TAG_COMPOUND);
            List<Pair<Target, ResourceLocation>> stepBindings1 = new ArrayList<>();
            for (int i = 0; i < stepBindings.size(); i++) {
                CompoundTag stepBinding = stepBindings.getCompound(i);
                stepBindings1.add(new Pair<>(Target.fromNbt(stepBinding.getCompound("pos")), ResourceLocation.tryParse(stepBinding.getString("type"))));
            }
            int selecting = -1;
            if (tag.contains("selecting"))
                selecting = tag.getInt("selecting");
            return new CraftGuideRenderData(stepBindings1, inputs1, outputs1, icon, selecting);
        } else {
            return EMPTY;
        }
    }
}
