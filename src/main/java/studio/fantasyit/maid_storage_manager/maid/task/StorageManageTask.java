package studio.fantasyit.maid_storage_manager.maid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.*;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.chest.GoToChestMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.chest.PickItemBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.FillReturnStorageBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.StartStorageResultBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.terminal.GoToTerminalMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.terminal.PickItemFromMeBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.ViewChestMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.ViewStorageAndStoreBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.WriteInventoryListBehavior;

import java.util.ArrayList;
import java.util.List;

public class StorageManageTask implements IMaidTask {
    @Override
    public @NotNull ResourceLocation getUid() {
        return new ResourceLocation(MaidStorageManager.MODID, "storage_manage");
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.CHEST.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(@NotNull EntityMaid entityMaid) {
        return null;
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        return new ArrayList<>(List.of(
                Pair.of(20, new PickItemBehavior()),
                Pair.of(20, new FindListItemBehavior()),
                Pair.of(20, new GoToChestMoveBehavior()),
                Pair.of(20, new StartStorageResultBehavior()),
                Pair.of(20, new FillReturnStorageBehavior()),
                Pair.of(10, new ViewChestMoveBehavior()),
                Pair.of(10, new ViewStorageAndStoreBehavior()),
                Pair.of(10, new WriteInventoryListBehavior()),
                Pair.of(40, new GoToTerminalMoveBehavior()),
                Pair.of(40, new PickItemFromMeBehavior()),
                Pair.of(10, new ReturnWorkScheduleBehavior())
        ));
    }
}
