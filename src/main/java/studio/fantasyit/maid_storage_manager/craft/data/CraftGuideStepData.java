package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CraftGuideStepData {
    public static final ResourceLocation SPECIAL_ACTION = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "special");
    public static Codec<CraftGuideStepData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Target.CODEC.fieldOf(CraftGuide.TAG_OP_STORAGE)
                            .forGetter(CraftGuideStepData::getStorage),
                    Codec.list(ItemStackUtil.OPTIONAL_CODEC_UNLIMITED)
                            .fieldOf(CraftGuide.TAG_OP_INPUT)
                            .forGetter(CraftGuideStepData::getInput),
                    Codec.list(ItemStackUtil.OPTIONAL_CODEC_UNLIMITED)
                            .fieldOf(CraftGuide.TAG_OP_OUTPUT)
                            .forGetter(CraftGuideStepData::getOutput),
                    ResourceLocation.CODEC.fieldOf(CraftGuide.TAG_OP_ACTION)
                            .forGetter(CraftGuideStepData::getActionType),
                    CompoundTag.CODEC.fieldOf(CraftGuide.TAG_OP_EXTRA)
                            .forGetter(CraftGuideStepData::getExtraData)
            ).apply(instance, CraftGuideStepData::new)
    );
    public static StreamCodec<RegistryFriendlyByteBuf, CraftGuideStepData> STREAM_CODEC = StreamCodec.of(
            (t, c) -> {
                Target.STREAM_CODEC.encode(t, c.storage);
                t.writeCollection(c.input, (StreamCodec) ItemStackUtil.OPTIONAL_STREAM_CODEC);
                t.writeCollection(c.output, (StreamCodec) ItemStackUtil.OPTIONAL_STREAM_CODEC);
                t.writeResourceLocation(c.action);
                t.writeBoolean(c.optional);
                t.writeNbt(c.extraData);
            },
            (c) -> new CraftGuideStepData(
                    Target.STREAM_CODEC.decode(c),
                    c.readCollection(ArrayList::new, (StreamCodec<FriendlyByteBuf, ItemStack>) (StreamCodec) ItemStackUtil.OPTIONAL_STREAM_CODEC),
                    c.readCollection(ArrayList::new, (StreamCodec<FriendlyByteBuf, ItemStack>) (StreamCodec) ItemStackUtil.OPTIONAL_STREAM_CODEC),
                    c.readResourceLocation(),
                    c.readBoolean(),
                    c.readNbt()
            )
    );
    public Target storage;
    public List<ItemStack> input;
    public List<ItemStack> output;
    public ResourceLocation action;
    public CraftAction actionType;
    public CompoundTag extraData;

    public CraftGuideStepData(Target storage,
                              List<ItemStack> input,
                              List<ItemStack> output,
                              ResourceLocation action,
                              CompoundTag extraData) {
        this.storage = storage;
        this.action = action;
        this.actionType = CraftManager.getInstance().getAction(action);
        if (this.actionType == null) {
            this.actionType = CraftManager.getInstance().getDefaultAction();
            this.action = actionType.type();
        }
        if (input.size() < this.actionType.inputCount())
            input = new ArrayList<>(input);
        while (input.size() < this.actionType.inputCount())
            input.add(ItemStack.EMPTY);
        if (output.size() < this.actionType.outputCount())
            output = new ArrayList<>(output);
        while (output.size() < this.actionType.outputCount())
            output.add(ItemStack.EMPTY);
        this.input = input;
        this.output = output;
        this.extraData = extraData;
    }

    public static CraftGuideStepData createFromTypeStorage(Target storage, ResourceLocation action) {
        CraftAction action1 = Objects.requireNonNull(CraftManager.getInstance().getAction(action));
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < action1.inputCount(); i++)
            inputs.add(ItemStack.EMPTY);
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < action1.outputCount(); i++)
            outputs.add(ItemStack.EMPTY);
        return new CraftGuideStepData(storage, inputs, outputs, action, new CompoundTag());
    }

    public static CraftGuideStepData fromCompound(RegistryAccess registryAccess, CompoundTag tag) {
        Target storage = null;
        ResourceLocation action = null;
        CompoundTag extraData = new CompoundTag();
        if (tag.contains(CraftGuide.TAG_OP_STORAGE))
            storage = Target.fromNbt(tag.getCompound(CraftGuide.TAG_OP_STORAGE));
        List<ItemStack> inputs = new ArrayList<>();
        if (storage != null && tag.contains(CraftGuide.TAG_OP_INPUT)) {
            ListTag list = tag.getList(CraftGuide.TAG_OP_INPUT, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                inputs.add(
                        ItemStackUtil.parseStack(registryAccess, list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
        }
        List<ItemStack> outputs = new ArrayList<>();
        if (storage != null && tag.contains(CraftGuide.TAG_OP_OUTPUT)) {
            ListTag list = tag.getList(CraftGuide.TAG_OP_OUTPUT, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                outputs.add(
                        ItemStackUtil.parseStack(registryAccess, list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
        }
        if (tag.contains(CraftGuide.TAG_OP_ACTION))
            action = ResourceLocation.tryParse(tag.getString(CraftGuide.TAG_OP_ACTION));
        if (tag.contains(CraftGuide.TAG_OP_EXTRA))
            extraData = tag.getCompound(CraftGuide.TAG_OP_EXTRA);
        if (tag.contains(CraftGuide.TAG_OP_OPTIONAL)) {
            extraData.put(ActionOption.OPTIONAL.id().toString(), new CompoundTag());
            extraData.getCompound(ActionOption.OPTIONAL.id().toString()).putInt(ActionOption.OPTIONAL.id().toString(), tag.getBoolean(CraftGuide.TAG_OP_OPTIONAL) ? 1 : 0);
            extraData.getCompound(ActionOption.OPTIONAL.id().toString()).putString(ActionOption.OPTIONAL.id().toString(), "type");
        }
        return new CraftGuideStepData(storage, inputs, outputs, action, extraData);
    }

    public CompoundTag toCompound(RegistryAccess registryAccess) {
        CompoundTag tag = new CompoundTag();
        tag.put(CraftGuide.TAG_OP_STORAGE, storage.toNbt());
        if (!input.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack itemStack : input) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.put(CraftGuide.TAG_ITEMS_ITEM, ItemStackUtil.saveStack(registryAccess, itemStack));
                itemTag.putInt(CraftGuide.TAG_ITEMS_COUNT, itemStack.getCount());
                list.add(itemTag);
            }
            tag.put(CraftGuide.TAG_OP_INPUT, list);
        }
        if (!output.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack itemStack : output) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.put(CraftGuide.TAG_ITEMS_ITEM, ItemStackUtil.saveStack(registryAccess, itemStack));
                itemTag.putInt(CraftGuide.TAG_ITEMS_COUNT, itemStack.getCount());
                list.add(itemTag);
            }
            tag.put(CraftGuide.TAG_OP_OUTPUT, list);
        }
        tag.putString(CraftGuide.TAG_OP_ACTION, action.toString());
        tag.put(CraftGuide.TAG_OP_EXTRA, extraData);
        return tag;
    }

    public Target getStorage() {
        return storage;
    }

    public ResourceLocation getActionType() {
        return action;
    }

    public boolean isOptional() {
        return this.actionType.getOptionSelection(ActionOption.OPTIONAL, this).orElse(false);
    }


    public List<ItemStack> getItems() {
        ArrayList<ItemStack> items = new ArrayList<>();
        items.addAll(getInput());
        items.addAll(getOutput());
        return items;
    }

    public List<ItemStack> getNonEmptyItems() {
        return getItems().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getInput() {
        return input.subList(0, actionType.inputCount());
    }

    public void setInput(int i, ItemStack itemStack) {
        if (!(input instanceof ArrayList<?>))
            input = new ArrayList<>(input);
        input.set(i, itemStack);
    }

    public void clearInput() {
        if (!(input instanceof ArrayList<?>))
            input = new ArrayList<>(input);
        input.clear();
    }

    public List<ItemStack> getNonEmptyInput() {
        return getInput().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getOutput() {
        return output.subList(0, actionType.outputCount());
    }

    public void setOutput(int i, ItemStack itemStack) {
        if (!(output instanceof ArrayList<?>))
            output = new ArrayList<>(output);
        output.set(i, itemStack);
    }

    public void clearOutput() {
        if (!(output instanceof ArrayList<?>))
            output = new ArrayList<>(output);
        output.clear();
    }

    public List<ItemStack> getNonEmptyOutput() {
        return getOutput().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideStepData craftGuideStepData &&
                craftGuideStepData.storage.equals(this.storage) &&
                craftGuideStepData.input.size() == this.input.size() &&
                craftGuideStepData.output.size() == this.output.size()
        ) {
            for (int i = 0; i < craftGuideStepData.output.size(); i++) {
                if (!ItemStack.isSameItem(craftGuideStepData.output.get(i), this.output.get(i))) return false;
            }
            for (int i = 0; i < craftGuideStepData.input.size(); i++) {
                if (!ItemStack.isSameItem(craftGuideStepData.input.get(i), this.input.get(i))) return false;
            }
            return true;
        }
        return false;
    }


    public void setAction(ResourceLocation action) {
        this.action = action;
        this.actionType = CraftManager.getInstance().getAction(action);
        if (input.size() < actionType.inputCount()) {
            if (!(input instanceof ArrayList<?>))
                input = new ArrayList<>(input);
            for (int i = input.size(); i < actionType.inputCount(); i++) {
                input.add(ItemStack.EMPTY);
            }
        }
        if (output.size() < actionType.outputCount()) {
            if (!(output instanceof ArrayList<?>))
                output = new ArrayList<>(output);
            for (int i = output.size(); i < actionType.outputCount(); i++) {
                output.add(ItemStack.EMPTY);
            }
        }
    }

    public CompoundTag getExtraData() {
        return extraData;
    }

    public void setExtraData(CompoundTag extraData) {
        this.extraData = extraData;
    }

    public CraftGuideStepData copy() {
        return new CraftGuideStepData(
                storage,
                input.stream().map(ItemStack::copy).toList(),
                output.stream().map(ItemStack::copy).toList(),
                action,
                optional,
                extraData.copy()
        );
    }

    @Override
    public int hashCode() {
        int hash = storage.hashCode() * 31 + action.hashCode() * 17 + extraData.hashCode();
        int tHash = 0;
        for (ItemStack itemStack : input) {
            tHash += itemStack.hashCode();
        }
        hash += tHash % 9941;
        tHash = 0;
        for (ItemStack itemStack : output) {
            tHash += itemStack.hashCode();
        }
        hash += tHash % 9473;
        return hash % 10000831;
    }
}