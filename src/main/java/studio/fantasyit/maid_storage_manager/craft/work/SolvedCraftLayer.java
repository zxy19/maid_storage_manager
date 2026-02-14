package studio.fantasyit.maid_storage_manager.craft.work;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.List;

public record SolvedCraftLayer(int index,
                               int group,
                               int slotInput,
                               int slotOutput,
                               List<Integer> nextIndex,
                               MutableInt nonFinishPrev,
                               MutableInt nonStartPrev,
                               MutableInt lastTouch,
                               MutableObject<Progress> progress,
                               List<ItemStack> prefetchable
) {
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
                    Codec.INT.fieldOf("nonFinishPrev")
                            .forGetter(t -> t.nonFinishPrev.getValue()),
                    Codec.INT.fieldOf("prefetchInDeg").orElse(0)
                            .forGetter(t -> t.nonStartPrev.getValue()),
                    Codec.INT.fieldOf("lastTouch").orElse(0)
                            .forGetter(t -> t.lastTouch.getValue()),
                    Codec.STRING.fieldOf("progress")
                            .forGetter(t -> t.progress.getValue().name()),
                    ItemStack.CODEC.listOf().fieldOf("prefetchable")
                            .forGetter(t -> t.prefetchable)
            ).apply(instance, SolvedCraftLayer::new)
    );

    public int slotConsume() {
        return slotInput + slotOutput;
    }

    public enum Progress {
        // 任务正在等待（前置任务未完成）
        WAITING,
        // 任务可以调度（前置任务已经全部开始）
        IDLE,
        // 任务开始预获取物品
        PREFETCH,
        // 任务等待前置任务结束
        STANDBY,
        // 获取物品中
        GATHERING,
        // 合成中
        WORKING,
        // 完成
        FINISHED,
        // 失败
        FAILED,
        // 分配到其他女仆进行
        DISPATCHED
    }

    public SolvedCraftLayer(int index,
                            int group,
                            int si,
                            int so,
                            List<Integer> nextIndex,
                            int nonFinishPrev,
                            int nonStartPrev,
                            int lastTouch,
                            String progress,
                            List<ItemStack> prefetchable) {
        this(index,
                group,
                si,
                so,
                new ArrayList<>(nextIndex),
                new MutableInt(nonFinishPrev),
                new MutableInt(nonStartPrev),
                new MutableInt(lastTouch),
                new MutableObject<>(Progress.valueOf(progress)),
                prefetchable
        );
    }
}