package studio.fantasyit.maid_storage_manager.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackData;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.items.data.TargetList;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.UUID;

public class DataComponentRegistry {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MaidStorageManager.MODID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackData>> CONTAIN_ITEM = DATA_COMPONENTS
            .register("contain_itemstack", () -> DataComponentType.<ItemStackData>builder().persistent(ItemStackData.CODEC).networkSynchronized(ItemStackData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TargetList.Immutable>> TARGETS = DATA_COMPONENTS
            .register("targets", () -> DataComponentType.<TargetList.Immutable>builder().persistent(TargetList.Immutable.CODEC).networkSynchronized(TargetList.Immutable.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SELECTING = DATA_COMPONENTS
            .register("selecting", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> MARK = DATA_COMPONENTS
            .register("mark", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> TO_SPAWN_ITEMS = DATA_COMPONENTS
            .register("to_spawn_items", () -> DataComponentType.<List<ItemStack>>builder().persistent(ItemStack.CODEC.listOf()).networkSynchronized(ItemStack.LIST_STREAM_CODEC).build());


    //region RequestListItem
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RequestItemStackList.Immutable>> REQUEST_ITEMS = DATA_COMPONENTS
            .register("request_items", () -> DataComponentType.<RequestItemStackList.Immutable>builder().persistent(RequestItemStackList.Immutable.CODEC).networkSynchronized(RequestItemStackList.Immutable.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> REQUEST_STORAGE_ENTITY = DATA_COMPONENTS
            .register("request_storage_entity", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Target>> REQUEST_STORAGE_BLOCK = DATA_COMPONENTS
            .register("request_storage_block", () -> DataComponentType.<Target>builder().persistent(Target.CODEC).networkSynchronized(Target.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> REQUEST_FAIL_ADDITION = DATA_COMPONENTS
            .register("request_fail_addition", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> REQUEST_WORK_UUID = DATA_COMPONENTS
            .register("request_work_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REQUEST_IGNORE = DATA_COMPONENTS
            .register("request_ignore", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REQUEST_CD_UNIT = DATA_COMPONENTS
            .register("request_cd_unit", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REQUEST_CD = DATA_COMPONENTS
            .register("request_cd", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REQUEST_INTERVAL = DATA_COMPONENTS
            .register("request_interval", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REQUEST_VIRTUAL = DATA_COMPONENTS
            .register("request_virtual", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> REQUEST_VIRTUAL_SOURCE = DATA_COMPONENTS
            .register("request_virtual_source", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> REQUEST_VIRTUAL_DATA = DATA_COMPONENTS
            .register("request_virtual_data", () -> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REQUEST_MATCHING = DATA_COMPONENTS
            .register("request_matching", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    //endregion
    //region WrittenInventoryList
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> INVENTORY_TIME = DATA_COMPONENTS
            .register("inv_time", () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Component>> INVENTORY_AUTHOR = DATA_COMPONENTS
            .register("inv_author", () -> DataComponentType.<Component>builder().persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> INVENTORY_UUID = DATA_COMPONENTS
            .register("inv_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    //endregion

    //region StorageDefineBauble
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> STORAGE_DEFINE_MODE = DATA_COMPONENTS
            .register("storage_mode", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    //endregion
    //region PortableCraftCalculatorBauble
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PCC_RECIPES = DATA_COMPONENTS
            .register("pcc_recipes", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PCC_PROGRESS = DATA_COMPONENTS
            .register("pcc_progress", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PCC_LAYERS = DATA_COMPONENTS
            .register("pcc_layers", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    //endregion
    //region LogisticsGuide
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Target>> LOGISTICS_INPUT = DATA_COMPONENTS
            .register("logistics_input", () -> DataComponentType.<Target>builder().persistent(Target.CODEC).networkSynchronized(Target.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Target>> LOGISTICS_OUTPUT = DATA_COMPONENTS
            .register("logistics_output", () -> DataComponentType.<Target>builder().persistent(Target.CODEC).networkSynchronized(Target.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LOGISTICS_SINGLE = DATA_COMPONENTS
            .register("logistics_single", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    //endregion
    //region FilterListItem
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_MATCH_TAG = DATA_COMPONENTS
            .register("filter_match_tag", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_BLACK_MODE = DATA_COMPONENTS
            .register("filter_black_mode", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterItemStackList.Immutable>> FILTER_ITEMS = DATA_COMPONENTS
            .register("filter_items", () -> DataComponentType.<FilterItemStackList.Immutable>builder().persistent(FilterItemStackList.Immutable.CODEC).networkSynchronized(FilterItemStackList.Immutable.STREAM_CODEC).build());
    //endregion
    //region CraftGuide
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CRAFT_GUIDE_SPECIAL = DATA_COMPONENTS
            .register("craft_special_op", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CraftGuideData>> CRAFT_GUIDE_DATA = DATA_COMPONENTS
            .register("craft_data", () -> DataComponentType.<CraftGuideData>builder().persistent(CraftGuideData.CODEC).networkSynchronized(CraftGuideData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CraftGuideRenderData>> CRAFT_GUIDE_RENDER = DATA_COMPONENTS
            .register("craft_render", () -> DataComponentType.<CraftGuideRenderData>builder().persistent(CraftGuideRenderData.CODEC).networkSynchronized(CraftGuideRenderData.STREAM_CODEC).build());
    //endregion
    //region ProgressPad
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> PROGRESS_PAD_BINDING = DATA_COMPONENTS
            .register("progress_pad_binding", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PROGRESS_PAD_VIEWING = DATA_COMPONENTS
            .register("progress_pad_viewing", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PROGRESS_PAD_STYLE = DATA_COMPONENTS
            .register("progress_pad_style", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PROGRESS_PAD_MERGE = DATA_COMPONENTS
            .register("progress_pad_merge", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PROGRESS_PAD_SELECTING = DATA_COMPONENTS
            .register("progress_pad_selecting", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    //endregion
    //region Communicate
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> COMMUNICATE_LAST_TASK = DATA_COMPONENTS
            .register("communicate_last_task", () -> DataComponentType.<ResourceLocation>builder().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMMUNICATE_CD = DATA_COMPONENTS
            .register("communicate_cd", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConfigurableCommunicateData>> COMMUNICATE_DATA = DATA_COMPONENTS
            .register("communicate_data", () -> DataComponentType.<ConfigurableCommunicateData>builder().persistent(ConfigurableCommunicateData.CODEC).networkSynchronized(ConfigurableCommunicateData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> COMMUNICATE_MANUAL = DATA_COMPONENTS
            .register("communicate_manual", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> COMMUNICATE_WORK_CARD = DATA_COMPONENTS
            .register("communicate_work_card", () -> DataComponentType.<ItemStack>builder().persistent(ItemStack.OPTIONAL_CODEC).networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> COMMUNICATE_LAST_WORK_UUID = DATA_COMPONENTS
            .register("communicate_last_work_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());

    //endregion
    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
