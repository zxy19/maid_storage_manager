package studio.fantasyit.maid_storage_manager.maid.data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class StorageManagerConfigData implements TaskDataKey<StorageManagerConfigData.Data> {
    public static final class Data {
        private boolean coWorkMode;
        private MemoryAssistant memoryAssistant;
        private boolean noSortPlacement = false;
        private SuppressStrategy suppressStrategy = SuppressStrategy.AFTER_ALL;

        public Data(MemoryAssistant memoryAssistant, boolean noSortPlacement, boolean coWorkMode, SuppressStrategy suppressStrategy) {
            this.memoryAssistant = memoryAssistant;
            this.noSortPlacement = noSortPlacement;
            this.coWorkMode = coWorkMode;
            this.suppressStrategy = suppressStrategy;
        }

        public static Data getDefault() {
            return new Data(MemoryAssistant.MEMORY_FIRST, false, false, SuppressStrategy.AFTER_EACH);
        }

        public MemoryAssistant memoryAssistant() {
            return memoryAssistant;
        }

        public boolean noSortPlacement() {
            return noSortPlacement;
        }

        public void memoryAssistant(MemoryAssistant memoryAssistant) {
            this.memoryAssistant = memoryAssistant;
        }

        public void noSortPlacement(boolean noSortPlacement) {
            this.noSortPlacement = noSortPlacement;
        }

        public boolean coWorkMode() {
            return coWorkMode;
        }

        public void coWorkMode(boolean coWorkMode) {
            this.coWorkMode = coWorkMode;
        }

        public SuppressStrategy suppressStrategy() {
            return suppressStrategy;
        }

        public void suppressStrategy(SuppressStrategy suppressStrategy) {
            this.suppressStrategy = suppressStrategy;
        }
    }

    public static TaskDataKey<Data> KEY = null;
    public static final ResourceLocation LOCATION = new ResourceLocation("maid_storage_manager", "storage_manager_config");

    @Override
    public ResourceLocation getKey() {
        return LOCATION;
    }

    @Override
    public CompoundTag writeSaveData(Data data) {
        CompoundTag tag = new CompoundTag();
        tag.putString("memoryAssistant", data.memoryAssistant().name());
        tag.putString("suppressStrategy", data.suppressStrategy().name());
        tag.putBoolean("noSortPlacement", data.noSortPlacement());
        tag.putBoolean("coWorkMode", data.coWorkMode());
        return tag;
    }

    @Override
    public Data readSaveData(CompoundTag compound) {
        MemoryAssistant memoryAssistant = MemoryAssistant.valueOf(compound.getString("memoryAssistant"));
        SuppressStrategy suppressStrategy = compound.contains("suppressStrategy")
                ? SuppressStrategy.valueOf(compound.getString("suppressStrategy"))
                : SuppressStrategy.AFTER_ALL;
        boolean noSortPlacement = compound.getBoolean("noSortPlacement");
        boolean coWorkMode = compound.getBoolean("coWorkMode");
        return new Data(memoryAssistant, noSortPlacement, coWorkMode, suppressStrategy);
    }

    public static String getTranslationKey(MemoryAssistant memoryAssistant) {
        return "gui.maid_storage_manager.config.memory_assistant." + switch (memoryAssistant) {
            case MEMORY_ONLY -> "memory_only";
            case MEMORY_FIRST -> "memory_first";
            case ALWAYS_SCAN -> "always_scan";
        };
    }

    public static String getTranslationKey(SuppressStrategy memoryAssistant) {
        return "gui.maid_storage_manager.config.fast_sort." + switch (memoryAssistant) {
            case AFTER_ALL -> "normal";//强效率
            case AFTER_PRIORITY -> "filter";//优先分类
            case AFTER_EACH -> "all";//强分类
        };
    }


    public static String getTranslationKey(boolean enable) {
        return "gui.maid_storage_manager.config.bool." + (enable ? "enable" : "disable");
    }

    public enum MemoryAssistant {
        MEMORY_ONLY,
        MEMORY_FIRST,
        ALWAYS_SCAN,
    }

    public enum SuppressStrategy {
        AFTER_EACH,
        AFTER_PRIORITY,
        AFTER_ALL
    }

    public static StorageManagerConfigData.Data get(EntityMaid maid) {
        return maid.getOrCreateData(KEY, Data.getDefault());
    }
}