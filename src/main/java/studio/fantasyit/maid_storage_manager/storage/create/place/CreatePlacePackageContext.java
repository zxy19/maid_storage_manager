package studio.fantasyit.maid_storage_manager.storage.create.place;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import studio.fantasyit.maid_storage_manager.integration.create.CreateIntegration;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class CreatePlacePackageContext implements IStorageInsertableContext {
    ChainConveyorBlockEntity chainConveyor;
    private String targetPackageName;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        BlockState blockState = level.getBlockState(target.pos);
        if (!blockState.is(AllBlocks.CHAIN_CONVEYOR.get()))
            return;
        BlockEntity be = level.getBlockEntity(target.pos);
        if (be instanceof ChainConveyorBlockEntity tChainConveyor) {
            this.chainConveyor = tChainConveyor;
        }
        targetPackageName = CreateIntegration.getAddress(maid, CreateIntegration.AddressType.PLACED);
    }

    @Override
    public ItemStack insert(ItemStack item) {
        if (item.isEmpty())
            return item;
        if (this.chainConveyor == null)
            return item;
        if (!this.chainConveyor.canAcceptMorePackages())
            return item;
        ItemStackHandler newInv = new ItemStackHandler(9);
        newInv.setStackInSlot(0, item.copy());
        ItemStack p = PackageItem.containing(newInv);
        PackageItem.addAddress(p, targetPackageName);
        ChainConveyorPackage newPackage = new ChainConveyorPackage(0, p);
        this.chainConveyor.addLoopingPackage(newPackage);
        return ItemStack.EMPTY;
    }
}
