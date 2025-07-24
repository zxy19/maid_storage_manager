package studio.fantasyit.maid_storage_manager.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackList;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.UUID;

public class DataComponentRegistry {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MaidStorageManager.MODID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackList>> ITEMS = DATA_COMPONENTS
            .register("items", () -> DataComponentType.<ItemStackList>builder().persistent(ItemStackList.CODEC).networkSynchronized(ItemStackList.STREAM_CODEC).build());

    //region 请求列表数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RequestItemStackList>> REQUEST_ITEMS = DATA_COMPONENTS
            .register("request_items", () -> DataComponentType.<RequestItemStackList>builder().persistent(RequestItemStackList.CODEC).networkSynchronized(RequestItemStackList.STREAM_CODEC).build());
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

    //endregion

}
