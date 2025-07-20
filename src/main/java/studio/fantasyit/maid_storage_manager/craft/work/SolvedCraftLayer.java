package studio.fantasyit.maid_storage_manager.craft.work;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.List;

public record SolvedCraftLayer(int index, int group, int slotInput, int slotOutput, List<Integer> nextIndex,
                               MutableInt inDegree,
                               MutableInt lastTouch,
                               MutableObject<Progress> progress) {
    public static Codec<SolvedCraftLayer> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("index")
                            .forGetter(SolvedCraftLayer::index),
                    Codec.INT.fieldOf("group")
                            .forGetter(SolvedCraftLayer::group),
                    Codec.INT.fieldOf("slotInput")
                            .forGetter(SolvedCraftLayer::slotInput),
                    Codec.INT.fieldOf("slotOutput")
                            .forGetter(SolvedCraftLayer::slotOutput),
                    Codec.list(Codec.INT)
                            .fieldOf("nextIndex")
                            .forGetter(SolvedCraftLayer::nextIndex),
                    Codec.INT.fieldOf("inDegree")
                            .forGetter(t -> t.inDegree.getValue()),
                    Codec.STRING.fieldOf("progress")
                            .forGetter(t -> t.progress.getValue().name()),
                    Codec.INT.fieldOf("lastTouch").orElse(0)
                            .forGetter(t -> t.lastTouch.getValue())
            ).apply(instance, SolvedCraftLayer::new)
    );

    public int slotConsume() {
        return slotInput + slotOutput;
    }

    enum Progress {
        WAITING,
        IDLE,
        GATHERING,
        WORKING,
        FINISHED,
        FAILED,
        DISPATCHED
    }

    public SolvedCraftLayer(int index, int group, int si, int so, List<Integer> nextIndex, int inDegree, String progress, int lastTouch) {
        this(index, group, si, so, new ArrayList<>(nextIndex), new MutableInt(inDegree), new MutableInt(lastTouch), new MutableObject<>(Progress.valueOf(progress)));
    }
}