package studio.fantasyit.maid_storage_manager.storage.create.stock;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.List;

public class AbstractCreateContext implements IStorageContext {
    protected StockTickerBlockEntity be;
    protected InventorySummary inventory;
    protected int current;
    protected List<BigItemStack> stacks;
    protected EntityMaid maid;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        this.maid = maid;
        BlockState blockState = level.getBlockState(target.pos);
        if (!blockState.is(AllBlocks.STOCK_TICKER.get()))
            return;
        BlockEntity be = level.getBlockEntity(target.pos);
        if (be instanceof StockTickerBlockEntity stockTickerBlockEntity) {
            this.be = stockTickerBlockEntity;
            inventory = stockTickerBlockEntity.getRecentSummary();
            stacks = inventory.getStacks();
        }
    }

    @Override
    public boolean isDone() {
        return be == null || current >= stacks.size();
    }
}
