package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

public class MaidCommunicateCheckBehavior extends Behavior<EntityMaid> {
    public MaidCommunicateCheckBehavior() {
        super(ImmutableMap.of(
                MemoryModuleRegistry.COMMUNICATE_REQUEST.get(),
                MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected void start(ServerLevel p_22540_, EntityMaid p_22541_, long p_22542_) {
        CommunicateRequest request = CommunicateUtil.getCommunicateRequest(p_22541_);
        if (request == null)
            return;

        if (!request.isValid() || request.isFinished()) {
            request.stopAndClear();
        } else {
            request.tick();
        }
    }
}
