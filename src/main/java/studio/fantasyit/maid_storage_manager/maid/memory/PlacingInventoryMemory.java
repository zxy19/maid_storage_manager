package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class PlacingInventoryMemory extends AbstractTargetMemory {

    public record Suppressed(Target target, Type type) {
        public enum Type {
            FILTER,
            MATCH,
            NORMAL
        }

        public static final Codec<Suppressed> CODEC = RecordCodecBuilder.create(ii ->
                ii.group(
                        Target.CODEC.fieldOf("target")
                                .forGetter(Suppressed::target),
                        Codec.STRING.fieldOf("type")
                                .forGetter(s -> s.type.name())
                ).apply(ii, (a, b) -> new Suppressed(a, Type.valueOf(b)))
        );
    }

    public static final Codec<PlacingInventoryMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData").forGetter(AbstractTargetMemory::getTargetData),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf("arrangeItems")
                            .forGetter(PlacingInventoryMemory::getArrangeItems),
                    Codec.BOOL.fieldOf("anySuccess")
                            .forGetter(PlacingInventoryMemory::isAnySuccess),
                    Codec.INT.fieldOf("failCount")
                            .forGetter(PlacingInventoryMemory::getFailCount),
                    Suppressed.CODEC.listOf().fieldOf("suppressedPos")
                            .orElse(List.of())
                            .forGetter(PlacingInventoryMemory::getSuppressedPos),
                    Codec.STRING.fieldOf("targetSuppressType")
                            .orElse(Suppressed.Type.NORMAL.name())
                            .forGetter(PlacingInventoryMemory::getTargetSuppressTypeS)
            ).apply(instance, PlacingInventoryMemory::new)
    );

    private int failCount;

    public List<ItemStack> arrangeItems;
    private boolean anySuccess;
    private List<Suppressed> suppressedPos;
    private Suppressed.Type targetSuppressType;

    public PlacingInventoryMemory(TargetData targetData, List<ItemStack> arrangeItems, boolean anySuccess, int failCount, List<Suppressed> suppressedPos, String targetSuppressType) {
        super(targetData);
        this.arrangeItems = new ArrayList<>(arrangeItems);
        this.anySuccess = anySuccess;
        this.failCount = failCount;
        this.suppressedPos = new ArrayList<>(suppressedPos);
        this.targetSuppressType = Suppressed.Type.valueOf(targetSuppressType);
    }

    public PlacingInventoryMemory() {
        super();
        arrangeItems = new ArrayList<>();
        anySuccess = false;
        suppressedPos = new ArrayList<>();
        targetSuppressType = Suppressed.Type.NORMAL;
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

    public List<Suppressed> getSuppressedPos() {
        return suppressedPos;
    }

    public void addSuppressedPos(Target target, Suppressed.Type type) {
        suppressedPos.add(new Suppressed(target, type));
    }

    public boolean anySuppressed() {
        return !suppressedPos.isEmpty();
    }

    public boolean anySuppressed(Suppressed.Type type) {
        return suppressedPos.stream().anyMatch(s -> s.type == type);
    }


    public void removeSuppressed() {
        suppressedPos.clear();
    }

    public void removeSuppressed(Target target, Suppressed.Type type) {
        suppressedPos.removeIf(s -> s.target.equals(target) && s.type == type);
    }

    public void removeSuppressed(Suppressed.Type type) {
        suppressedPos.removeIf(s -> s.type == type);
    }

    public void setTargetSuppressType(Suppressed.Type suppressType) {
        this.targetSuppressType = suppressType;
    }

    public Suppressed.Type getTargetSuppressType() {
        return targetSuppressType;
    }

    public String getTargetSuppressTypeS() {
        return getTargetSuppressType().name();
    }

    @Override
    public boolean isVisitedPos(Target pos) {
        return super.isVisitedPos(pos) || suppressedPos.stream().anyMatch(s -> s.target.equals(pos));
    }
}
