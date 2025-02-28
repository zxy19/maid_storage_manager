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
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.place.PlaceBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.place.PlaceMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.FindListItemBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.find.RequestFindBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.find.RequestFindMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.RequestRetBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.RequestRetMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.resort.ResortBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.resort.ResortMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.ViewBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.ViewMoveBehavior;
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
        ArrayList<Pair<Integer, BehaviorControl<? super EntityMaid>>> list = new ArrayList<>();
        //找到列表的任务
        list.add(Pair.of(10, new FindListItemBehavior()));
        //寻找/存放
        list.add(Pair.of(10, new RequestFindBehavior()));
        list.add(Pair.of(10, new RequestFindMoveBehavior()));
        list.add(Pair.of(10, new RequestRetMoveBehavior()));
        list.add(Pair.of(10, new RequestRetBehavior()));
        //游走查看箱子
        list.add(Pair.of(5, new ViewMoveBehavior()));
        list.add(Pair.of(5, new ViewBehavior()));
        list.add(Pair.of(5, new WriteInventoryListBehavior()));
        //整理
        list.add(Pair.of(6, new ResortBehavior()));
        list.add(Pair.of(6, new ResortMoveBehavior()));
        //存放物品
        list.add(Pair.of(7, new PlaceBehavior()));
        list.add(Pair.of(7, new PlaceMoveBehavior()));
        //工作冷却
        list.add(Pair.of(10, new ScheduleBehavior()));
        return list;
    }
}
