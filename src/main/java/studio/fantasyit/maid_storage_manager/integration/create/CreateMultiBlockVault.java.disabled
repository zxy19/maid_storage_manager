package studio.fantasyit.maid_storage_manager.integration.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.vault.ItemVaultBlock;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import studio.fantasyit.maid_storage_manager.storage.base.IMultiBlockProcessor;

import java.util.function.Consumer;

public class CreateMultiBlockVault implements IMultiBlockProcessor {

    @Override
    public boolean isValid(Level level, BlockPos pos, BlockState blockState) {
        return blockState.is(AllBlocks.ITEM_VAULT.get());
    }

    @Override
    public void process(Level level, BlockPos pos, Consumer<BlockPos> processor) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ItemVaultBlockEntity ivbe) {
            ItemVaultBlockEntity controlBe = ivbe.getControllerBE();
            BlockPos startPos = controlBe.getBlockPos();

            boolean alongZ = ItemVaultBlock.getVaultBlockAxis(controlBe.getBlockState()) == Direction.Axis.Z;
            for (int yOffset = 0; yOffset < controlBe.getHeight(); ++yOffset) {
                for (int xOffset = 0; xOffset < controlBe.getWidth(); ++xOffset) {
                    for (int zOffset = 0; zOffset < controlBe.getWidth(); ++zOffset) {
                        BlockPos vaultPos = alongZ ? startPos.offset(xOffset, zOffset, yOffset) : startPos.offset(yOffset, xOffset, zOffset);
                        if (vaultPos.equals(pos))
                            continue;
                        processor.accept(vaultPos);
                    }
                }
            }
        }
    }
}
