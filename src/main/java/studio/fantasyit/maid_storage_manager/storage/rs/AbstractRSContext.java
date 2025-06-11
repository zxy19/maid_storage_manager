package studio.fantasyit.maid_storage_manager.storage.rs;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.network.node.INetworkNodeManager;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCache;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.context.special.RsCraftingAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.type.RSType;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageCraftDataProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract public class AbstractRSContext extends AbstractFilterableBlockStorage implements IStorageCraftDataProvider {

    protected IStackList<ItemStack> stackList;
    protected Collection<StackListEntry<ItemStack>> stackListStacks;

    protected IStackList<ItemStack> craftable;
    public INetwork network;

    ServerLevel level;
    Target target;
    protected INetworkNode node;
    boolean done = false;

    private void reinit() {
        INetworkNodeManager networkNodeManager = API.instance().getNetworkNodeManager(level);
        node = networkNodeManager.getNode(target.pos);
        if (node == null) {
            return;
        }
        network = node.getNetwork();
        if (network != null) {
            IStorageCache<ItemStack> itemStorageCache = network.getItemStorageCache();
            stackList = itemStorageCache.getList();
            stackListStacks = stackList.getStacks();
            craftable = itemStorageCache.getCraftablesList();
        }
    }

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        this.level = level;
        this.target = target;
        reinit();
    }

    @Override
    public void reset() {
        super.reset();
        reinit();
        done = false;
    }

    @Override
    public void finish() {
        super.finish();
        done = true;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public List<CraftGuideData> getCraftGuideData() {
        List<CraftGuideData> list = new ArrayList<>(craftable.getStacks().stream()
                .filter(entry -> entry.getStack().is(ItemRegistry.CRAFT_GUIDE.get()))
                .map(entry -> CraftGuideData.fromItemStack(entry.getStack()))
                .toList());
        Collection<StackListEntry<ItemStack>> craftableStacks = craftable.getStacks();
        craftableStacks.stream()
                .map(key -> {
                    List<CraftGuideStepData> steps = List.of(new CraftGuideStepData(
                            target,
                            List.of(),
                            List.of(key.getStack()),
                            RsCraftingAction.TYPE,
                            false,
                            new CompoundTag()
                    ));
                    return new CraftGuideData(
                            steps,
                            RSType.TYPE
                    );
                })
                .forEach(list::add);
        done = true;
        return list;
    }
}
