package studio.fantasyit.maid_storage_manager.maid.memory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.Optional;

public class LogisticsMemory extends AbstractTargetMemory {
    public static Codec<LogisticsMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(AbstractTargetMemory::getTargetData),
                    CraftLayer.CODEC.optionalFieldOf("craftGuide")
                            .forGetter(LogisticsMemory::getCraftLayerO),
                    CraftLayer.CODEC.optionalFieldOf("result")
                            .forGetter(LogisticsMemory::getResultLayerO),
                    ItemStack.CODEC.fieldOf("currentLogisticsGuideItem").orElse(ItemStack.EMPTY)
                            .forGetter(LogisticsMemory::getCurrentLogisticsGuideItem),
                    Codec.INT.fieldOf("next")
                            .forGetter(LogisticsMemory::getNext),
                    Codec.STRING.fieldOf("stage")
                            .forGetter(LogisticsMemory::getStageName)
            ).apply(instance, LogisticsMemory::new)
    );

    public enum Stage {
        INPUT,
        CRAFT,
        OUTPUT,
        RECYCLE, FINISH
    }

    CraftLayer craftLayer;
    CraftLayer resultLayer;
    ItemStack currentLogisticsGuideItem;
    int next;
    Stage stage;

    public LogisticsMemory(TargetData targetData, Optional<CraftLayer> craftLayer, Optional<CraftLayer> resultLayer, ItemStack currentLogisticsGuideItem, int next, String stage) {
        super(targetData);
        this.craftLayer = craftLayer.orElse(null);
        this.resultLayer = resultLayer.orElse(null);
        this.currentLogisticsGuideItem = currentLogisticsGuideItem;
        this.next = next;
        this.stage = Stage.valueOf(stage);
    }

    public LogisticsMemory() {
        super();
        this.craftLayer = null;
        this.resultLayer = null;
        this.currentLogisticsGuideItem = ItemStack.EMPTY;
        this.next = 0;
        this.stage = Stage.FINISH;
    }

    public Optional<CraftLayer> getCraftLayerO() {
        return Optional.ofNullable(craftLayer);
    }

    public Optional<CraftLayer> getResultLayerO() {
        return Optional.ofNullable(resultLayer);
    }

    public CraftLayer getCraftLayer() {
        return craftLayer;
    }

    public CraftLayer getResultLayer() {
        return resultLayer;
    }

    public void setCraftAndResultLayer(CraftLayer craftLayer, CraftLayer resultLayer) {
        this.craftLayer = craftLayer;
        this.resultLayer = resultLayer;
    }

    public ItemStack getCurrentLogisticsGuideItem() {
        return currentLogisticsGuideItem;
    }

    public void setCurrentLogisticsGuideItem(ItemStack itemStack) {
        this.currentLogisticsGuideItem = itemStack;
    }

    public void switchCurrentLogisticsGuideItem(EntityMaid maid) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            next = (next + 1) % maidBauble.getSlots();
            ItemStack stack = maidBauble.getStackInSlot(next);
            if (stack.is(ItemRegistry.LOGISTICS_GUIDE.get())) {
                setCurrentLogisticsGuideItem(stack);
                return;
            }
        }
        setCurrentLogisticsGuideItem(ItemStack.EMPTY);
    }

    public boolean isStillValid(EntityMaid maid) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        if (next >= maidBauble.getSlots()) return false;
        ItemStack stackInSlot = maidBauble.getStackInSlot(next);
        return ItemStackUtil.isSame(stackInSlot, currentLogisticsGuideItem, true);
    }

    public int getNext() {
        return next;
    }

    public boolean shouldWork() {
        return !currentLogisticsGuideItem.isEmpty();
    }

    public String getStageName() {
        return stage.name();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean hasMultipleGuide(EntityMaid maid) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        int count = 0;
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            ItemStack stack = maidBauble.getStackInSlot(i);
            if (stack.is(ItemRegistry.LOGISTICS_GUIDE.get())) {
                count++;
            }
        }
        return count > 1;
    }
}