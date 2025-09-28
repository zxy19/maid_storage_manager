package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class CraftGuideRenderData {
    static Codec<Pair<Target, ResourceLocation>> STEP_BINDING_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Target.CODEC.fieldOf("target").forGetter(Pair::getA),
                    ResourceLocation.CODEC.fieldOf("recipe").forGetter(Pair::getB)
            ).apply(instance, Pair::new)
    );
    static StreamCodec<RegistryFriendlyByteBuf, Pair<Target, ResourceLocation>> STEP_BINDING_STREAM_CODEC = StreamCodec.composite(
            Target.STREAM_CODEC,
            Pair::getA,
            ResourceLocation.STREAM_CODEC,
            Pair::getB,
            Pair::new
    );
    public static Codec<CraftGuideRenderData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    STEP_BINDING_CODEC.listOf().fieldOf("stepBindings").forGetter(t -> t.stepBindings),
                    ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.listOf().fieldOf("inputs").forGetter(t -> t.inputs),
                    ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.listOf().fieldOf("outputs").forGetter(t -> t.outputs),
                    ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.fieldOf("icon").forGetter(t -> t.icon)
            ).apply(instance, CraftGuideRenderData::new)
    );
    public static StreamCodec<RegistryFriendlyByteBuf, CraftGuideRenderData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, STEP_BINDING_STREAM_CODEC),
            t -> t.stepBindings,
            ByteBufCodecs.collection(ArrayList::new, ItemStackUtil.OPTIONAL_STREAM_CODEC),
            t -> t.inputs,
            ByteBufCodecs.collection(ArrayList::new, ItemStackUtil.OPTIONAL_STREAM_CODEC),
            t -> t.outputs,
            ItemStackUtil.OPTIONAL_STREAM_CODEC,
            t -> t.icon,
            CraftGuideRenderData::new
    );

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
        CraftGuideData craftGuideData = CraftGuide.getCraftGuideReadOnly(itemStack);
        craftGuideData.buildInputAndOutputs();
        ItemStack icon1 = CraftManager.getInstance().getType(craftGuideData.getType()).getIcon();
        itemStack.set(DataComponentRegistry.CRAFT_GUIDE_RENDER, new CraftGuideRenderData(
                craftGuideData.steps.stream().map(t -> new Pair<>(t.getStorage(), t.getActionType())).toList(),
                craftGuideData.getInput(),
                craftGuideData.getOutput(),
                icon1
        ));
    }

    @Override
    public int hashCode() {
        int hash = stepBindings.hashCode() * 31 + selecting * 17;
        int tHash = 0;
        for (ItemStack itemStack : inputs) {
            tHash += itemStack.hashCode();
        }
        hash += tHash % 9941;
        tHash = 0;
        for (ItemStack itemStack : outputs) {
            tHash += itemStack.hashCode();
        }
        hash += tHash % 1091;
        return hash + ItemStack.hashItemAndComponents(icon) * 197;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideRenderData craftGuideRenderData) {
            boolean eq = craftGuideRenderData.selecting == this.selecting &&
                    ItemStackUtil.isSame(craftGuideRenderData.icon, this.icon, true);
            if (eq && craftGuideRenderData.stepBindings.size() == this.stepBindings.size()) {
                for (int i = 0; i < craftGuideRenderData.stepBindings.size(); i++) {
                    eq = eq & craftGuideRenderData.stepBindings.get(i).getA().equals(this.stepBindings.get(i).getA()) &&
                            craftGuideRenderData.stepBindings.get(i).getB().equals(this.stepBindings.get(i).getB());
                }
            }
            if (eq && craftGuideRenderData.inputs.size() == this.inputs.size())
                for (int i = 0; i < craftGuideRenderData.inputs.size(); i++) {
                    eq = eq & ItemStackUtil.isSame(craftGuideRenderData.inputs.get(i), this.inputs.get(i), true);
                    eq = eq & craftGuideRenderData.inputs.get(i).getCount() == this.inputs.get(i).getCount();
                }
            else
                eq = false;
            if (eq && craftGuideRenderData.outputs.size() == this.outputs.size())
                for (int i = 0; i < craftGuideRenderData.outputs.size(); i++) {
                    eq = eq & ItemStackUtil.isSame(craftGuideRenderData.outputs.get(i), this.outputs.get(i), true);
                    eq = eq & craftGuideRenderData.outputs.get(i).getCount() == this.outputs.get(i).getCount();
                }
            else
                eq = false;
            return eq;
        }
        return false;
    }
}
