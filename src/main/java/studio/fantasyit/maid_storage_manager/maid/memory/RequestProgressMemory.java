package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Optional;
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
                    Codec.BOOL.optionalFieldOf("isCheckingStock", false)
                            .forGetter(RequestProgressMemory::isCheckingStock),
                    UUIDUtil.CODEC.fieldOf("workUUID")
                            .forGetter(RequestProgressMemory::getWorkUUID),
                    Codec.INT.fieldOf("tries")
                            .forGetter(RequestProgressMemory::getTries),
                    UUIDUtil.CODEC.optionalFieldOf("targetEntity")
                            .forGetter(RequestProgressMemory::getTargetEntityUUID)
            ).apply(instance, RequestProgressMemory::new)
    );
    public boolean tryCrafting;
    public UUID workUUID;
    public boolean isReturning;
    private boolean isCheckingStock;
    public CompoundTag context;
    private int tries;
    private UUID targetEntity;

    public RequestProgressMemory(TargetData targetData,
                                 CompoundTag context,
                                 boolean tryCrafting,
                                 boolean isReturning,
                                 boolean isCheckingStock,
                                 UUID workUUID,
                                 int tries,
                                 Optional<UUID> targetEntity
    ) {
        super(targetData);
        this.context = context;
        this.isReturning = isReturning;
        this.workUUID = workUUID;
        this.tries = tries;
        this.tryCrafting = tryCrafting;
        this.isCheckingStock = isCheckingStock;
        this.targetEntity = targetEntity.orElse(null);
    }

    public RequestProgressMemory() {
        super();
        this.context = new CompoundTag();
        this.isReturning = false;
        this.workUUID = UUID.randomUUID();
        this.tries = 0;
        this.tryCrafting = false;
        this.isCheckingStock = false;
        this.targetEntity = null;
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

    public boolean isCheckingStock() {
        return isCheckingStock;
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
        this.clearTargetEntity();
        this.resetVisitedPos();
        this.resetFailCount();
        this.tries = 0;
    }

    public void stopWork() {
        this.workUUID = UUID.randomUUID();
        this.clearTarget();
        this.clearTargetEntity();
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

    @Override
    public void setTarget(Target target) {
        this.clearTargetEntity();
        super.setTarget(target);
    }

    public Entity getTargetEntity(ServerLevel level) {
        return level.getEntity(this.targetEntity);
    }

    public Optional<UUID> getTargetEntityUUID() {
        return Optional.ofNullable(this.targetEntity);
    }

    public void setTargetEntity(UUID targetEntity) {
        this.targetEntity = targetEntity;
        clearTarget();
    }

    public void clearTargetEntity() {
        this.targetEntity = null;
    }

    public void addTries() {
        this.tries++;
    }

    public int getTries() {
        return this.tries;
    }

    public void setCheckingStock(boolean isCheckingStock) {
        this.isCheckingStock = isCheckingStock;
    }
}