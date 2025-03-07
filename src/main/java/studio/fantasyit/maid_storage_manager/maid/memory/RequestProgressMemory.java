package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.Stack;
import java.util.UUID;

public class RequestProgressMemory extends AbstractTargetMemory {
    public static final Codec<RequestProgressMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(AbstractTargetMemory::getTargetData),
                    CompoundTag.CODEC.fieldOf("context")
                            .forGetter(RequestProgressMemory::getContext),
                    Codec.BOOL.fieldOf("tryCrafting")
                            .forGetter(RequestProgressMemory::isTryCrafting),
                    Codec.BOOL.fieldOf("isReturning")
                            .forGetter(RequestProgressMemory::isReturning),
                    UUIDUtil.CODEC.fieldOf("workUUID")
                            .forGetter(RequestProgressMemory::getWorkUUID),
                    Codec.INT.fieldOf("tries")
                            .forGetter(RequestProgressMemory::getTries)
            ).apply(instance, RequestProgressMemory::new)
    );
    public boolean tryCrafting;
    public UUID workUUID;
    public boolean isReturning;
    public CompoundTag context;
    private int tries;

    public RequestProgressMemory(TargetData targetData, CompoundTag context, boolean tryCrafting, boolean isReturning, UUID workUUID, int tries) {
        super(targetData);
        this.context = context;
        this.isReturning = isReturning;
        this.workUUID = workUUID;
        this.tries = tries;
        this.tryCrafting = tryCrafting;
    }

    public RequestProgressMemory() {
        super();
        this.context = new CompoundTag();
        this.isReturning = false;
        this.workUUID = UUID.randomUUID();
        this.tries = 0;
        this.tryCrafting = false;
    }

    public CompoundTag getContext() {
        return context;
    }

    public boolean isReturning() {
        return isReturning;
    }

    public boolean isTryCrafting() {
        return tryCrafting;
    }

    public UUID getWorkUUID() {
        return workUUID;
    }


    //工作辅助函数
    public void newWork(UUID workUUID) {
        this.workUUID = workUUID;
        this.isReturning = false;
        this.tryCrafting = false;
        this.clearTarget();
        this.resetVisitedPos();
        this.tries = 0;
    }

    public void stopWork() {
        this.workUUID = UUID.randomUUID();
        this.clearTarget();
    }

    public void setReturn() {
        this.setReturn(true);
    }

    public void setReturn(boolean returning) {
        this.isReturning = returning;
    }

    public void setTryCrafting(boolean tryCrafting) {
        this.tryCrafting = tryCrafting;
    }

    public void addTries() {
        this.tries++;
    }

    public int getTries() {
        return this.tries;
    }
}
