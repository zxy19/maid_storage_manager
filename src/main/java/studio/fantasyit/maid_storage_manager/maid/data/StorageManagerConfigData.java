package studio.fantasyit.maid_storage_manager.maid.data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class StorageManagerConfigData implements TaskDataKey<StorageManagerConfigData.Data> {
    public static final class Data {
        private boolean coWorkMode;
        private MemoryAssistant memoryAssistant;
        private boolean noSortPlacement = false;
        private FastSort fastSort = FastSort.NORMAL;

        public Data(MemoryAssistant memoryAssistant, boolean noSortPlacement, boolean coWorkMode, FastSort fastSort) {
            this.memoryAssistant = memoryAssistant;
            this.noSortPlacement = noSortPlacement;
            this.coWorkMode = coWorkMode;
            this.fastSort = fastSort;
        }

        public static Data getDefault() {
            return new Data(MemoryAssistant.MEMORY_FIRST, false, false, FastSort.NORMAL);
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

        public FastSort fastSort() {
            return fastSort;
        }

        public void fastSort(FastSort fastSort) {
            this.fastSort = fastSort;
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
        tag.putString("fastSortMode", data.fastSort().name());
        tag.putBoolean("noSortPlacement", data.noSortPlacement());
        tag.putBoolean("coWorkMode", data.coWorkMode());
        return tag;
    }

    @Override
    public Data readSaveData(CompoundTag compound) {
        MemoryAssistant memoryAssistant = MemoryAssistant.valueOf(compound.getString("memoryAssistant"));
        FastSort fastSort = compound.contains("fastSortMode") ? FastSort.valueOf(compound.getString("fastSortMode")) : FastSort.NORMAL;
        boolean noSortPlacement = compound.getBoolean("noSortPlacement");
        boolean coWorkMode = compound.getBoolean("coWorkMode");
        return new Data(memoryAssistant, noSortPlacement, coWorkMode, fastSort);
    }

    public static String getTranslationKey(MemoryAssistant memoryAssistant) {
        return "gui.maid_storage_manager.config.memory_assistant." + switch (memoryAssistant) {
            case MEMORY_ONLY -> "memory_only";
            case MEMORY_FIRST -> "memory_first";
            case ALWAYS_SCAN -> "always_scan";
        };
    }

    public static String getTranslationKey(FastSort memoryAssistant) {
        return "gui.maid_storage_manager.config.fast_sort." + switch (memoryAssistant) {
            case NORMAL -> "normal";
            case KEEP_FILTER -> "enable";
            case KEEP_ALL -> "all";
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

    public enum FastSort {
        NORMAL,
        KEEP_FILTER,
        KEEP_ALL
    }
}