package studio.fantasyit.maid_storage_manager.integration.create;


import com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

public class AddCreateStockButtonForMaid {
    public static void addStockButton(MaidContainerGuiEvent.Init t) {
        if (!t.getGui().getMaid().getTask().getUid().equals(StorageManageTask.TASK_ID)) {
            return;
        }
        BlockPos.betweenClosedStream(
                        t.getGui().getMaid().getBoundingBox().inflate(Config.createStockKeeperRangeV, Config.createStockKeeperRangeH, Config.createStockKeeperRangeV)
                )
                .filter(pos -> t.getGui().getMaid().level().getBlockState(pos).is(AllBlocks.STOCK_TICKER.get()))
                .findFirst()
                .ifPresent(pos -> {
                    t.addButton("create_stock", new CreateStockButton(t.getLeftPos() + 253,
                                    t.getTopPos() + 87,
                                    pos,
                                    t.getGui().getMaid().getId()
                            )
                    );
                });
    }
}
