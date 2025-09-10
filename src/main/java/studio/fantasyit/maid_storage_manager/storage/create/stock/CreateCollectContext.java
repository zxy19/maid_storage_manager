package studio.fantasyit.maid_storage_manager.storage.create.stock;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRoutingTable;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.integration.create.CreateIntegration;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CreateCollectContext extends AbstractCreateContext implements IStorageExtractableContext {
    protected ChainConveyorBlockEntity chainConveyorBlockEntity;
    protected BlockPos connection;
    protected List<IItemHandler> itemHandlers = new ArrayList<>();
    protected String targetPackageName;
    protected List<ItemStack> itemList;
    protected List<ItemStack> sentRequests = new ArrayList<>();
    protected List<ItemStack> costedSlots = new ArrayList<>();
    protected ItemStackUtil.MATCH_TYPE matchNbt;
    int waitCircle = 0;
    int timeoutAt = Integer.MAX_VALUE;
    boolean reversed = false;
    BlockPos relativePos;
    double onChainPos;
    int nextStartUpdateAt = 0;
    private CombinedInvWrapper availableInv;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        targetPackageName = CreateIntegration.getAddress(maid, CreateIntegration.AddressType.REQUEST);
        rescanItemHandler();
        reselectChain();
        availableInv = maid.getAvailableInv(true);
        for (int i = 0; i < availableInv.getSlots(); i++) {
            if (!availableInv.getStackInSlot(i).isEmpty())
                isEnoughToAddItem(availableInv.getStackInSlot(i));
        }
    }

    public static TagKey<Block> CreatePackageContainer = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "create_package_container"));

    private void rescanItemHandler() {
        itemHandlers.clear();
        ServerLevel level = (ServerLevel) maid.level();
        BlockPos.betweenClosedStream(maid.getBoundingBox().inflate(5))
                .forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (state.is(CreatePackageContainer)) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be != null)
                            be.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandlers::add);
                    }
                });
    }

    public void reselectChain() {
        Level level = maid.level();
        MutableDouble minDistance = new MutableDouble(Double.MAX_VALUE);
        MutableDouble minPos = new MutableDouble(Double.MAX_VALUE);
        MutableObject<ChainConveyorBlockEntity> selected = new MutableObject<>(null);
        MutableObject<BlockPos> selectedConnection = new MutableObject<>(null);
        BlockPos.betweenClosedStream(maid.getBoundingBox().inflate(20)).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.is(AllBlocks.CHAIN_CONVEYOR.get())) {
                if (level.getBlockEntity(pos) instanceof ChainConveyorBlockEntity chainConveyor) {
                    if (chainConveyor.connections.isEmpty()) {
                        return;
                    }
                    chainConveyor.connections.forEach(connection -> {
                        Pair<Double, Double> dis = getDistanceToBeConnectionAndPosition(pos, connection, maid.getEyePosition());
                        if (dis.getA() < minDistance.doubleValue()) {
                            minDistance.setValue(dis.getA());
                            minPos.setValue(dis.getB());
                            selected.setValue(chainConveyor);
                            selectedConnection.setValue(connection);
                        }
                    });
                }
            }
        });
        this.chainConveyorBlockEntity = selected.getValue();
        this.connection = selectedConnection.getValue();
        if (chainConveyorBlockEntity != null && connection != null) {
            DebugData.sendDebug("[Create Chain]Select Chain %s connection %s(%s)",
                    selected.getValue().getBlockPos().toShortString(),
                    connection.toShortString(),
                    selected.getValue().getBlockPos().offset(connection).toShortString()
            );
            relativePos = selected.getValue().getBlockPos().subtract(maid.blockPosition());
            reversed = chainConveyorBlockEntity.reversed;
            onChainPos = minPos.getValue();
            if (ChainRouterManager.isChanged(targetPackageName, chainConveyorBlockEntity.getBlockPos().offset(connection), connection)) {
                nextStartUpdateAt = ChainRouterManager.getLastTick(targetPackageName) + ChainConveyorRoutingTable.ENTRY_TIMEOUT + 20;
                DebugData.sendDebug("[Create Chain]Delayed for router updates");
            }
            ChainRouterManager.set(targetPackageName, chainConveyorBlockEntity.getBlockPos().offset(connection), connection, maid.tickCount);
        }
    }

    public void clearCurrentChain() {
        if (chainConveyorBlockEntity != null && connection != null)
            if (maid.level().getBlockEntity(chainConveyorBlockEntity.getBlockPos().offset(connection)) instanceof ChainConveyorBlockEntity ccbe) {
                ccbe.travelPorts.remove(relativePos);
            }
        connection = null;
        chainConveyorBlockEntity = null;
    }

    private void updateChainRoute() {
        if (nextStartUpdateAt > maid.tickCount) {
            return;
        }
        if (maid.level().getBlockEntity(chainConveyorBlockEntity.getBlockPos().offset(connection)) instanceof ChainConveyorBlockEntity ccbe) {
            BlockPos rev = connection.multiply(-1);
            ccbe.routingTable.receivePortInfo(targetPackageName, rev);
            ccbe.travelPorts.put(relativePos, new ChainConveyorBlockEntity.ConnectedPort((float) onChainPos, rev, targetPackageName));
            ChainRouterManager.update(targetPackageName, maid.tickCount);
        }
    }

    @Override
    public boolean isDone() {
        if (itemList == null)
            return false;
        if (current < itemList.size())
            return false;
        if (sentRequests.isEmpty())
            return true;
        return maid.tickCount > timeoutAt;
    }

    @Override
    public boolean hasTask() {
        return itemList != null;
    }

    @Override
    public void clearTask() {
        itemList = null;
        sentRequests.clear();
    }

    public boolean isEnoughToAddItem(ItemStack itemStack) {
        ItemStackUtil.addToList(costedSlots, itemStack, false);
        int tot = 0;
        for (ItemStack ii : costedSlots)
            tot += (ii.getCount() + ii.getMaxStackSize() - 1) / ii.getMaxStackSize();
        return tot <= availableInv.getSlots();
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> callback) {
        if ((waitCircle++) % 4 == 0) {
            if (itemList != null && current < itemList.size()) {
                ItemStack itemStack = itemList.get(current);

                MutableInt toCollect = new MutableInt(0);
                stacks.forEach(bigItemStack -> {
                    if (ItemStackUtil.isSame(itemStack, bigItemStack.stack, matchNbt))
                        toCollect.add(bigItemStack.count);
                });
                if (toCollect.getValue() > itemStack.getMaxStackSize())
                    toCollect.setValue(itemStack.getMaxStackSize());
                if (isEnoughToAddItem(itemStack.copyWithCount(toCollect.getValue()))) {
                    if (toCollect.intValue() > 0) {
                        if (toCollect.intValue() > itemStack.getCount())
                            toCollect.setValue(itemStack.getCount());

                        be.broadcastPackageRequest(
                                LogisticallyLinkedBehaviour.RequestType.PLAYER,
                                new PackageOrder(List.of(new BigItemStack(itemStack, toCollect.getValue()))),
                                null,
                                targetPackageName
                        );
                        ItemStackUtil.addToList(sentRequests, itemStack.copyWithCount(toCollect.getValue()), matchNbt);
                        itemList.get(current).shrink(toCollect.getValue());
                    }
                    if (itemList.get(current).isEmpty() || toCollect.getValue() == 0)
                        current++;
                } else {
                    //背包容量不足。直接放弃
                    current = itemList.size();
                }
                if (current >= itemList.size()) {
                    timeoutAt = maid.tickCount + 600;
                }
            }
        }
        if (waitCircle % 30 == 0) {
            rescanItemHandler();
        }
        this.dealPackagesForChain(callback);

        itemHandlers.forEach(itemHandler -> {
            watchItemHandler(itemHandler, callback);
        });

        List<ItemStack> list = maid.level().getEntities(
                        EntityTypeTest.forClass(PackageEntity.class),
                        maid.getBoundingBox().inflate(7, 7, 7),
                        e -> PackageItem.getAddress(e.box).equals(targetPackageName)
                ).stream()
                .map(e -> {
                    e.remove(Entity.RemovalReason.DISCARDED);
                    return e.box;
                })
                .toList();
        if (!list.isEmpty())
            dealTakenPackages(callback, list);
    }

    @Override
    public void finish() {
        clearCurrentChain();
    }

    @Override
    public void reset() {
        clearCurrentChain();
        this.reselectChain();
        timeoutAt = Integer.MAX_VALUE;
        current = 0;
        sentRequests.clear();
    }

    private void watchItemHandler(IItemHandler itemHandler, Function<ItemStack, ItemStack> callback) {
        List<ItemStack> takenPackages = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            @NotNull ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (PackageItem.isPackage(stack) && PackageItem.getAddress(stack).equals(targetPackageName)) {
                ItemStack itemStack = itemHandler.extractItem(i, 1, false);
                if (!itemStack.isEmpty())
                    takenPackages.add(itemStack);
            }
        }
        if (takenPackages.isEmpty()) return;
        maid.swing(InteractionHand.MAIN_HAND);
        dealTakenPackages(callback, takenPackages);
    }

    private void dealTakenPackages(Function<ItemStack, ItemStack> callback, List<ItemStack> takenPackages) {
        takenPackages.forEach(itemStack -> {
            ItemStackHandler t = PackageItem.getContents(itemStack);
            for (int i = 0; i < t.getSlots(); i++) {
                ItemStack stack = t.getStackInSlot(i);
                ItemStackUtil.removeIsMatchInList(sentRequests, stack.copy(), matchNbt);
                ItemStack rest = callback.apply(stack);
                if (!rest.isEmpty()) {
                    InvUtil.throwItem(maid, rest);
                }
            }
        });
    }

    public void dealPackagesForChain(Function<ItemStack, ItemStack> callback) {
        if (chainConveyorBlockEntity == null) return;
        if (!chainConveyorBlockEntity.connections.contains(connection) || chainConveyorBlockEntity.reversed != reversed) {
            clearCurrentChain();
            reselectChain();
            return;
        }
        updateChainRoute();
        List<ItemStack> takenPackages = new ArrayList<>();
        chainConveyorBlockEntity.getTravellingPackages().forEach((conn, packages) -> {
            packages.removeIf(packageBox -> {
                if (!PackageItem.getAddress(packageBox.item).equals(targetPackageName)) {
                    return false;
                }
                if (packageBox.worldPosition == null || packageBox.worldPosition.distanceTo(maid.position()) > 7) {
                    return false;
                }
                takenPackages.add(packageBox.item);
                maid.swing(InteractionHand.MAIN_HAND);
                return true;
            });
        });
        chainConveyorBlockEntity.getLoopingPackages().removeIf((packageBox) -> {
            if (!PackageItem.getAddress(packageBox.item).equals(targetPackageName)) {
                return false;
            }
            if (packageBox.worldPosition != null && packageBox.worldPosition.distanceTo(maid.position()) > 8) {
                return false;
            }
            maid.swing(InteractionHand.MAIN_HAND);
            takenPackages.add(packageBox.item);
            return true;
        });
        if (!takenPackages.isEmpty())
            chainConveyorBlockEntity.sendData();
        dealTakenPackages(callback, takenPackages);
    }


    @Override
    public void setExtract(List<ItemStack> itemList, ItemStackUtil.MATCH_TYPE matchNbt) {
        this.itemList = new ArrayList<>(itemList.stream().map(ItemStack::copy).toList());
        this.matchNbt = matchNbt;
        this.current = 0;
    }

    @Override
    public void setExtractByExisting(Predicate<ItemStack> predicate) {
        if (this.stacks != null)
            setExtract(
                    stacks.stream()
                            .map(s -> s.stack)
                            .filter(predicate)
                            .toList(), ItemStackUtil.MATCH_TYPE.MATCHING);
        else
            setExtract(List.of(), ItemStackUtil.MATCH_TYPE.MATCHING);
    }

    private Pair<Double, Double> getDistanceToBeConnectionAndPosition(BlockPos pos, BlockPos connection, Vec3 origin) {
        Vec3 chainP1 = pos.getCenter();
        Vec3 chainDir = Vec3.atLowerCornerOf(connection);
        double onChainLen = chainDir.dot(origin.subtract(chainP1));
        if (onChainLen < 0) {
            return new Pair<>(origin.distanceTo(chainP1), 0.0);
        } else if (onChainLen > chainDir.length()) {
            return new Pair<>(origin.distanceTo(Vec3.atLowerCornerOf(pos.offset(connection))), chainDir.length());
        } else {
            return new Pair<>(Math.sqrt(origin.distanceToSqr(chainP1) + Math.pow(onChainLen, 2)), onChainLen);
        }
    }
}
