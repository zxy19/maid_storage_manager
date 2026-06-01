package studio.fantasyit.maid_storage_manager.maid.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry;

public class StorageManagerConfigData {
    public static final class Data {
        private boolean useMemorizedCraftGuide;
        private boolean coWorkMode;
        private MemoryAssistant memoryAssistant;
        private boolean noSortPlacement = false;
        private SuppressStrategy suppressStrategy = SuppressStrategy.AFTER_ALL;
        private boolean allowSeekWorkMeal = false;
        private int maxParallel;
        private int maxCraftingLayerRepeatCount;
        private boolean autoSorting = true;
        private int itemTypeLimit = -1;
        private boolean doCommunicate = false;

        public Data(MemoryAssistant memoryAssistant,
                    boolean noSortPlacement,
                    boolean coWorkMode,
                    SuppressStrategy suppressStrategy,
                    boolean allowSeekWorkMeal,
                    boolean useMemorizedCraftGuide,
                    int maxParallel,
                    int maxCraftingLayerRepeatCount,
                    boolean autoSorting,
                    int itemTypeLimit,
                    boolean doCommunicate
        ) {
            this.memoryAssistant = memoryAssistant;
            this.noSortPlacement = noSortPlacement;
            this.coWorkMode = coWorkMode;
            this.suppressStrategy = suppressStrategy;
            this.allowSeekWorkMeal = allowSeekWorkMeal;
            this.useMemorizedCraftGuide = useMemorizedCraftGuide;
            this.maxParallel = maxParallel;
            this.maxCraftingLayerRepeatCount = maxCraftingLayerRepeatCount;
            this.autoSorting = autoSorting;
            this.itemTypeLimit = itemTypeLimit;
            this.doCommunicate = doCommunicate;
        }

        public static Data getDefault() {
            return new Data(MemoryAssistant.MEMORY_FIRST,
                    false,
                    false,
                    SuppressStrategy.AFTER_EACH,
                    false,
                    false,
                    5,
                    8,
                    true,
                    -1,
                    true
            );
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

        public boolean allowSeekWorkMeal() {
            return allowSeekWorkMeal;
        }

        public void allowSeekWorkMeal(boolean allowSeekWorkMeal) {
            this.allowSeekWorkMeal = allowSeekWorkMeal;
        }

        public boolean useMemorizedCraftGuide() {
            return useMemorizedCraftGuide;
        }

        public void useMemorizedCraftGuide(boolean useMemorizedCraftGuide) {
            this.useMemorizedCraftGuide = useMemorizedCraftGuide;
        }

        public int maxParallel() {
            return this.maxParallel;
        }

        public void maxParallel(int maxParallel) {
            this.maxParallel = Math.max(0, Math.min(10, maxParallel));
        }

        public int maxCraftingLayerRepeatCount() {
            return this.maxCraftingLayerRepeatCount;
        }

        public void maxCraftingLayerRepeatCount(int maxCraftingLayerRepeatCount) {
            if (maxCraftingLayerRepeatCount < 1)
                maxCraftingLayerRepeatCount = 1;
            for (int i = 1; i <= 64; i *= 2) {
                if (i == maxCraftingLayerRepeatCount)
                    this.maxCraftingLayerRepeatCount = i;
                else if (i > maxCraftingLayerRepeatCount)
                    this.maxCraftingLayerRepeatCount = i / 2;
                else continue;
                return;
            }
            this.maxCraftingLayerRepeatCount = 32;
        }

        public boolean autoSorting() {
            return autoSorting;
        }

        public void autoSorting(boolean autoSorting) {
            this.autoSorting = autoSorting;
        }

        public void itemTypeLimit(int itemTypeLimit) {
            this.itemTypeLimit = itemTypeLimit;
            if (itemTypeLimit < 0)
                this.itemTypeLimit = -1;
            if (itemTypeLimit > 1024)
                this.itemTypeLimit = 1024;
        }

        public int itemTypeLimit() {
            return itemTypeLimit;
        }

        public boolean doCommunicate() {
            return doCommunicate;
        }

        public void doCommunicate(boolean doCommunicate) {
            this.doCommunicate = doCommunicate;
        }
    }

    public static final Identifier LOCATION = Identifier.fromNamespaceAndPath("maid_storage_manager", "storage_manager_config");

    private static final StorageManagerConfigData INSTANCE = new StorageManagerConfigData();

    public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("memoryAssistant")
                    .xmap(MemoryAssistant::valueOf, MemoryAssistant::name)
                    .forGetter(Data::memoryAssistant),
            Codec.BOOL.fieldOf("noSortPlacement")
                    .forGetter(Data::noSortPlacement),
            Codec.BOOL.fieldOf("coWorkMode")
                    .forGetter(Data::coWorkMode),
            Codec.STRING.fieldOf("suppressStrategy")
                    .xmap(SuppressStrategy::valueOf, SuppressStrategy::name)
                    .forGetter(Data::suppressStrategy),
            Codec.BOOL.fieldOf("allowSeekWorkMeal")
                    .forGetter(Data::allowSeekWorkMeal),
            Codec.BOOL.fieldOf("useMemorizedCraftGuide")
                    .forGetter(Data::useMemorizedCraftGuide),
            Codec.INT.fieldOf("maxParallel")
                    .forGetter(Data::maxParallel),
            Codec.INT.fieldOf("maxCraftingLayerRepeatCount")
                    .forGetter(Data::maxCraftingLayerRepeatCount),
            Codec.BOOL.fieldOf("autoSorting")
                    .forGetter(Data::autoSorting),
            Codec.INT.fieldOf("itemTypeLimit")
                    .forGetter(Data::itemTypeLimit),
            Codec.BOOL.fieldOf("doCommunicate")
                    .forGetter(Data::doCommunicate)
    ).apply(instance, Data::new));

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
        return maid.getData(DataAttachmentRegistry.MAID_TASK_DATA);
    }

    public static StorageManagerConfigData.Data set(EntityMaid maid, StorageManagerConfigData.Data data) {
        maid.setData(DataAttachmentRegistry.MAID_TASK_DATA, data);
        return data;
    }
}