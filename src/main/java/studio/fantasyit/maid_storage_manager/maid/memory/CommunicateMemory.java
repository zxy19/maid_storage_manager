package studio.fantasyit.maid_storage_manager.maid.memory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;

import java.util.HashMap;
import java.util.UUID;

public class CommunicateMemory {
    final HashMap<UUID, Integer> cooldownUntil = new HashMap<>();

    public CommunicateMemory() {
        super();
    }

    EntityMaid targetMaid;
    int internalCooldown = 0;
    int delayedTimeout = 0;

    public EntityMaid getTargetMaid() {
        return targetMaid;
    }

    public void setTargetMaid(EntityMaid targetMaid) {
        this.targetMaid = targetMaid;
    }

    public boolean hasTargetMaid() {
        return targetMaid != null;
    }

    public boolean isInCooldown(UUID uuid, ServerLevel level) {
        if (!cooldownUntil.containsKey(uuid)) return false;
        return cooldownUntil.get(uuid) > level.getServer().getTickCount();
    }

    public void startCooldown(UUID uuid, ServerLevel level, int cooldown) {
        cooldownUntil.put(uuid, level.getServer().getTickCount() + cooldown);
    }

    public int getCooldown(UUID uuid) {
        return cooldownUntil.getOrDefault(uuid, 0);
    }

    public boolean checkAndUpdateInternalCooldown() {
        internalCooldown--;
        if (internalCooldown <= 0) {
            internalCooldown = 20;
            return true;
        }
        return false;
    }

    CommunicateRequest request;

    public void setRequest(CommunicateRequest request) {
        this.request = request;
    }

    public CommunicateRequest getRequest() {
        return request;
    }
}