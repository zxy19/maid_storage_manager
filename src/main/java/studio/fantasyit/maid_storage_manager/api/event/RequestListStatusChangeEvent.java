package studio.fantasyit.maid_storage_manager.api.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import java.util.UUID;

public class RequestListStatusChangeEvent extends Event {
    public enum Status {
        START,
        END
    }
    private final Status status;
    private final EntityMaid maid;
    private final UUID taskUUID;
    private final @Nullable ItemStack itemStack;
    public RequestListStatusChangeEvent(Status status, EntityMaid maid, UUID taskUUID, @Nullable ItemStack itemStack) {
        this.status = status;
        this.maid = maid;
        this.taskUUID = taskUUID;
        this.itemStack = itemStack;
    }
    public Status getStatus() {
        return status;
    }
    public EntityMaid getMaid() {
        return maid;
    }
    public UUID getTaskUUID() {
        return taskUUID;
    }
    public @Nullable ItemStack getItemStack() {
        return itemStack;
    }
    public String getVirtualSource(){
        if(itemStack == null)
            return "";
        return itemStack.get(DataComponentRegistry.REQUEST_VIRTUAL_SOURCE);
    }
}
