package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class SortingMemory extends AbstractTargetMemory {

    public static final Codec<SortingMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(SortingMemory::getTargetData),
                    Codec.list(Target.CODEC)
                            .fieldOf("needToSorting")
                            .forGetter(SortingMemory::getNeedToSorting)
            ).apply(instance, SortingMemory::new)
    );

    private final List<Target> needToSorting;

    private List<Target> getNeedToSorting() {
        return needToSorting;
    }

    public SortingMemory() {
        super();
        this.needToSorting = new ArrayList<>();
    }

    public SortingMemory(TargetData targetData, List<Target> needToSorting) {
        super(targetData);
        this.needToSorting = new ArrayList<>(needToSorting);
    }

    public void addNeedToSorting(Target target) {
        if (this.needToSorting.contains(target))
            return;
        this.needToSorting.add(target);
    }

    public Target getNextNeedToSorting() {
        if (this.needToSorting.isEmpty())
            return null;
        return this.needToSorting.get(0);
    }

    public void removeFirst() {
        if (this.needToSorting.isEmpty())
            return;
        this.needToSorting.remove(0);
    }

    public void removeNeedToSorting(Target target) {
        this.needToSorting.remove(target);
    }

    public boolean hasAny() {
        return !this.needToSorting.isEmpty();
    }
}