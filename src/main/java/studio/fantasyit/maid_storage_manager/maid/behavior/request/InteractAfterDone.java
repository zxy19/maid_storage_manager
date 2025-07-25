package studio.fantasyit.maid_storage_manager.maid.behavior.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;
import java.util.UUID;

public class InteractAfterDone extends Behavior<EntityMaid> {
    public InteractAfterDone() {
        super(Map.of(
                MemoryModuleRegistry.INTERACTION_RESULT.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    int ticks = 0;

    @Override
    protected void start(ServerLevel p_22540_, EntityMaid p_22541_, long p_22542_) {
        ticks = 0;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid p_22552_, long p_22553_) {
        ticks++;
        if (ticks % 4 == 0) {
            MemoryUtil.setTarget(p_22552_, MemoryUtil.getInteractPos(p_22552_), (float) Config.collectSpeed);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        return ticks < 20;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        tryInteract(level, maid);
        MemoryUtil.clearInteractPos(maid);
        MemoryUtil.clearTarget(maid);
    }

    private void tryInteract(ServerLevel level, EntityMaid maid) {
        BlockPos interactPos = MemoryUtil.getInteractPos(maid);
        if (interactPos == null) return;
        BlockPos target = null;
        if (!level.getBlockState(interactPos).getShape(level, interactPos).isEmpty())
            target = interactPos;
        else if (!level.getBlockState(interactPos.above()).getShape(level, interactPos.above()).isEmpty())
            target = interactPos.above();
        if (target == null) return;
        FakePlayer fakePlayer = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), maid.getName().getString()));
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ClipContext rayTraceContext =
                new ClipContext(maid.getPosition(0).add(0, maid.getEyeHeight(), 0),
                        target.getCenter(),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        fakePlayer);
        BlockHitResult result = level.clip(rayTraceContext);
        if (!result.getBlockPos().equals(target)) return;
        PlayerInteractEvent.RightClickBlock event = CommonHooks.onRightClickBlock(fakePlayer,
                InteractionHand.MAIN_HAND,
                target,
                result
        );

        if (event.getUseBlock() != TriState.FALSE) {
            level.getBlockState(target)
                    .useWithoutItem(level, fakePlayer, result);
        }
        Containers.dropContents(level, target, fakePlayer.getInventory());
        fakePlayer.getInventory().clearContent();
        fakePlayer.remove(Entity.RemovalReason.DISCARDED);
    }
}