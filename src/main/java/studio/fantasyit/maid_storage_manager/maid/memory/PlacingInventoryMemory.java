package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlacingInventoryMemory extends AbstractTargetMemory {
    public static final Codec<PlacingInventoryMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData").forGetter(AbstractTargetMemory::getTargetData),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf("arrangeItems")
                            .forGetter(PlacingInventoryMemory::getArrangeItems),
                    Codec.BOOL.fieldOf("anySuccess")
                            .forGetter(PlacingInventoryMemory::isAnySuccess)
            ).apply(instance, PlacingInventoryMemory::new)
    );

    public List<ItemStack> arrangeItems;
    private boolean anySuccess;

    public PlacingInventoryMemory(TargetData targetData, List<ItemStack> arrangeItems, boolean anySuccess) {
        super(targetData);
        this.arrangeItems = new ArrayList<>(arrangeItems);
        this.anySuccess = anySuccess;
    }

    public PlacingInventoryMemory() {
        super();
        arrangeItems = new ArrayList<>();
        anySuccess = false;
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
}
