package studio.fantasyit.maid_storage_manager.maid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.cowork.CoWorkChestView;
import studio.fantasyit.maid_storage_manager.maid.behavior.cowork.FollowActionBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.cowork.FollowDisableBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.cowork.FollowEnableBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.LogisticsSwitchTask;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.craft.LogisticCraftWorkBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.craft.LogisticCraftWorkMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.input.LogisticsInputBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.input.LogisticsInputMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.output.LogisticsOutputBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.output.LogisticsOutputMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.recycle.LogisticsRecycleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.logistics.recycle.LogisticsRecycleMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.meal.MealBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.meal.MealMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.place.PlaceBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.place.PlaceMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.FindListItemBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.InteractAfterDone;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.CraftExitBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.CraftInitBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.gather.RequestCraftGatherBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.gather.RequestCraftGatherMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work.RequestCraftWorkBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work.RequestCraftWorkMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.find.RequestFindBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.find.RequestFindMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.RequestRetBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.RequestRetMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.stock.StockCheckBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.stock.StockCheckMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.resort.ResortBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.resort.ResortMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.ViewBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.ViewMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.view.WriteInventoryListBehavior;
import studio.fantasyit.maid_storage_manager.maid.config.StorageManagerMaidConfigGui;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

public class StorageManageTask implements IMaidTask {
    public static ResourceLocation TASK_ID = new ResourceLocation(MaidStorageManager.MODID, "storage_manage");

    @Override
    public boolean enableEating(@NotNull EntityMaid maid) {
        return switch (MemoryUtil.getCurrentlyWorking(maid)) {
            case VIEW, NO_SCHEDULE -> true;
            default -> false;
        };
    }

    @Override
    public boolean enableLookAndRandomWalk(@NotNull EntityMaid maid) {
        return (MemoryUtil.getCurrentlyWorking(maid) == ScheduleBehavior.Schedule.MEAL);
    }

    @Override
    public @NotNull ResourceLocation getUid() {
        return TASK_ID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.CHEST.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(@NotNull EntityMaid entityMaid) {
        return InitSounds.MAID_IDLE.get();
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
        list.add(Pair.of(10, new StockCheckMoveBehavior()));
        list.add(Pair.of(10, new StockCheckBehavior()));
        //合成
        list.add(Pair.of(9, new RequestCraftGatherBehavior()));
        list.add(Pair.of(9, new RequestCraftGatherMoveBehavior()));
        list.add(Pair.of(8, new RequestCraftWorkBehavior()));
        list.add(Pair.of(8, new RequestCraftWorkMoveBehavior()));
        list.add(Pair.of(5, new CraftExitBehavior()));
        list.add(Pair.of(5, new CraftInitBehavior()));
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
        //敲钟（？
        list.add(Pair.of(5, new InteractAfterDone()));
        //协同工作模式
        list.add(Pair.of(5, new CoWorkChestView()));
        list.add(Pair.of(5, new FollowActionBehavior()));
        list.add(Pair.of(5, new FollowEnableBehavior()));
        list.add(Pair.of(5, new FollowDisableBehavior()));
        //物流模式
        list.add(Pair.of(5, new LogisticsSwitchTask()));
        list.add(Pair.of(5, new LogisticsOutputMoveBehavior()));
        list.add(Pair.of(5, new LogisticsOutputBehavior()));
        list.add(Pair.of(5, new LogisticsInputMoveBehavior()));
        list.add(Pair.of(5, new LogisticsInputBehavior()));
        list.add(Pair.of(5, new LogisticCraftWorkMoveBehavior()));
        list.add(Pair.of(5, new LogisticCraftWorkBehavior()));
        list.add(Pair.of(5, new LogisticsRecycleBehavior()));
        list.add(Pair.of(5, new LogisticsRecycleMoveBehavior()));
        //吃吃吃
        list.add(Pair.of(5, new MealBehavior()));
        list.add(Pair.of(5, new MealMoveBehavior()));
        return list;
    }

    @Override
    public @NotNull MenuProvider getTaskConfigGuiProvider(@NotNull EntityMaid maid) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int index, Inventory playerInventory, Player player) {
                return new StorageManagerMaidConfigGui.Container(index, playerInventory, maid.getId());
            }
        };
    }
}
