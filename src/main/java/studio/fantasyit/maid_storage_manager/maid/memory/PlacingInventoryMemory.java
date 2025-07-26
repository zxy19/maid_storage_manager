package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class PlacingInventoryMemory extends AbstractTargetMemory {

    public static final Codec<PlacingInventoryMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData").forGetter(AbstractTargetMemory::getTargetData),
                    Codec.list(ItemStackUtil.OPTIONAL_CODEC_UNLIMITED)
                            .fieldOf("arrangeItems")
                            .forGetter(PlacingInventoryMemory::getArrangeItems),
                    Codec.BOOL.fieldOf("anySuccess")
                            .forGetter(PlacingInventoryMemory::isAnySuccess),
                    Codec.INT.fieldOf("failCount")
                            .forGetter(PlacingInventoryMemory::getFailCount),
                    Target.CODEC.listOf().fieldOf("suppressedTarget")
                            .orElse(List.of())
                            .forGetter(PlacingInventoryMemory::getSuppressedPos)
            ).apply(instance, PlacingInventoryMemory::new)
    );

    private int failCount;

    public List<ItemStack> arrangeItems;
    private boolean anySuccess;
    private List<Target> suppressedPos;

    public PlacingInventoryMemory(TargetData targetData, List<ItemStack> arrangeItems, boolean anySuccess, int failCount, List<Target> suppressedPos) {
        super(targetData);
        this.arrangeItems = new ArrayList<>(arrangeItems);
        this.anySuccess = anySuccess;
        this.failCount = failCount;
        this.suppressedPos = new ArrayList<>(suppressedPos);
    }

    public PlacingInventoryMemory() {
        super();
        arrangeItems = new ArrayList<>();
        anySuccess = false;
        suppressedPos = new ArrayList<>();
    }

    public List<ItemStack> getArrangeItems() {
        return arrangeItems;
    }

    public void setArrangeItems(List<ItemStack> arrangeItems) {
        this.arrangeItems = arrangeItems;
    }

    public void clearArrangeItems() {
        arrangeItems.clear();
    }

    public void resetAnySuccess() {
        this.anySuccess = false;
    }

    public void anySuccess() {
        this.anySuccess = true;
    }

    public boolean isAnySuccess() {
        return anySuccess;
    }

    public int getFailCount() {
        return failCount;
    }

    public void addFailCount() {
        failCount++;
    }

    public void resetFailCount() {
        failCount = 0;
    }

    public List<Target> getSuppressedPos() {
        return suppressedPos;
    }

    public void addSuppressedPos(Target target) {
        suppressedPos.add(target);
    }

    public boolean anySuppressed() {
        return !suppressedPos.isEmpty();
    }
    public void removeSuppressed() {
        suppressedPos.clear();
    }
    public void removeSuppressed(Target target) {
        suppressedPos.remove(target);
    }
    public void removeSuppressed(List<Target> suppressedFilterTarget) {
        suppressedPos.removeIf(suppressedFilterTarget::contains);
    }
    public boolean isSuppressed(Target target){
        return suppressedPos.contains(target);
    }
}
