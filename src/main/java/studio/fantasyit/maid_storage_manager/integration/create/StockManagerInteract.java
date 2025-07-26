package studio.fantasyit.maid_storage_manager.integration.create;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.api.ICreateStockKeeperMaidChecker;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.network.CreateStockManagerPacket;

public class StockManagerInteract {
    public static int lastInteractedMaidId = -1;

    public static void setInteractedMaidId(int id) {
        lastInteractedMaidId = id;
    }

    protected static @Nullable BlockPos getStockTickerAround(EntityMaid maid) {
        return BlockPos.betweenClosedStream(
                        maid.getBoundingBox()
                                .inflate(Config.createStockKeeperRangeV, Config.createStockKeeperRangeH, Config.createStockKeeperRangeV)
                )
                .filter(pos -> maid.level().getBlockState(pos).is(AllBlocks.STOCK_TICKER.get()))
                .findFirst()
                .orElse(null);
    }

    public static boolean onPlayerInteract(Player player, EntityMaid maid) {
        if (!maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
            return false;
        }
        if (player.getMainHandItem().is(AllItems.SHOPPING_LIST.get())) {
            BlockPos pos = getStockTickerAround(maid);
            if (pos == null) {
                player.sendSystemMessage(Component.translatable("maid_storage_manager.no_stock_ticker"));
                return false;
            }
            if (player.level().isClientSide)
                PacketDistributor.sendToServer(new CreateStockManagerPacket(CreateStockManagerPacket.Type.SHOP_LIST, pos, maid.getId()));
            else
                onHandleShoppingList((ServerPlayer) player, maid, pos);
            return true;
        }
        return false;
    }

    public static void onHandleShoppingList(ServerPlayer player, EntityMaid maid, BlockPos ticker) {
        if (player.getMainHandItem().is(AllItems.SHOPPING_LIST.get()))
            StockTickerInteractionHandler.interactWithLogisticsManagerAt(player, player.level(), ticker);
    }

    public static void onHandleStockManager(ServerPlayer player, EntityMaid maid, BlockPos ticker) {
        if (!maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
            return;
        }
        StockTickerInteractionHandler.interactWithLogisticsManagerAt(player, player.level(), ticker);
        if (player.containerMenu instanceof ICreateStockKeeperMaidChecker icsk) {
            icsk.maid_storage_manager$setMaid(maid);
        }
    }
}
