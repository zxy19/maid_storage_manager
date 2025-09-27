package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.entity.EntityTypeTest;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.CommunicateMemory;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.Comparator;
import java.util.List;

public class MaidCommunicateFindTargetBehavior extends Behavior<EntityMaid> {

    public MaidCommunicateFindTargetBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.VIEW && MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.NO_SCHEDULE && MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.COMMUNICATE)
            return false;
        if (MemoryUtil.getCommunicate(maid).hasDelayedComplete())
            return false;
        return MemoryUtil.getCommunicate(maid).checkAndUpdateInternalCooldown();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        List<EntityMaid> entities = level.getEntities(
                EntityTypeTest.forClass(EntityMaid.class),
                maid.getBoundingBox().inflate(maid.hasRestriction() ? maid.getRestrictRadius() : 5),
                e -> CommunicateUtil.getWillingCommunicatable(e).isPresent()
        );
        CommunicateMemory comm = MemoryUtil.getCommunicate(maid);
        List<EntityMaid> el = entities
                .stream()
                .filter(ee -> !comm.isInCooldown(ee.getUUID(), level))
                .sorted(Comparator.comparingInt(ee -> comm.getCooldown(ee.getUUID())))
                .toList();
        if (el.isEmpty()) return;
        float restrictRadiusOwner = maid.hasRestriction() ? maid.getRestrictRadius() : 5;
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid, restrictRadiusOwner + 2, (int) (restrictRadiusOwner + 2));
        for (EntityMaid e : el) {
            Pair<BlockPos, BlockPos> blockPosBlockPosPair = PosUtil.pickMeetingPosPair(maid, e, pathFinding);
            if (blockPosBlockPosPair == null) continue;
            MemoryUtil.setTarget(maid, blockPosBlockPosPair.getA(), 0.5f);
            MemoryUtil.setTarget(e, blockPosBlockPosPair.getB(), 0.5f);
            comm.setTargetMaid(e);
            break;
        }
    }
}