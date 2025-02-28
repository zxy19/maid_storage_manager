package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResortingMemory extends AbstractTargetMemory {
    public static final Codec<ResortingMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(ResortingMemory::getTargetData),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf("needToResort")
                            .forGetter(ResortingMemory::getNeedToResort)
            ).apply(instance, ResortingMemory::new)
    );
    public List<ItemStack> needToResort;

    public ResortingMemory(TargetData targetData, List<ItemStack> needToResort) {
        super(targetData);
        this.needToResort = new ArrayList<>(needToResort);
    }

    public ResortingMemory() {
        super();
        needToResort = new ArrayList<>();
    }
    public List<ItemStack> getNeedToResort() {
        return needToResort;
    }

    public void clearNeedToResort() {
        needToResort.clear();
    }

    public void setNeedToResort(Collection<ItemStack> items) {
        needToResort.clear();
        needToResort.addAll(items);
    }
}
