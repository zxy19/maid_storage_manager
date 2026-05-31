package studio.fantasyit.maid_storage_manager.storage.rs;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class RSCollectContext extends AbstractRSContext implements IStorageExtractableContext {
    int current = 0;
    List<ItemStack> task = null;
    ItemStackUtil.MATCH_TYPE matchNbt = ItemStackUtil.MATCH_TYPE.AUTO;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
    }

    @Override
    public boolean isDone() {
        return super.isDone() || itemStorage == null || (task != null && current >= task.size());
    }

    @Override
    public boolean hasTask() {
        return task != null;
    }

    @Override
    public void clearTask() {
        task = null;
        current = 0;
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> callback) {
        if (itemStorage == null || task == null || current >= task.size()) return;
        int toCollect = task.get(current).getCount();

        List<ResourceAmount> itemStack = itemStorage
                .getAll()
                .stream()
                .filter(key -> key.resource() instanceof ItemResource)
                .filter(stack -> ItemStackUtil.isSame(((ItemResource) (stack.resource())).toItemStack(), task.get(current), matchNbt))
                .toList();
        

        for (ResourceAmount stack : itemStack) {
            int currentToCollect = Math.min(toCollect, MathUtil.toIntOrMax(stack.amount()));
            int extractedSim = Math.toIntExact(itemStorage.extract(stack.resource(), currentToCollect, Action.SIMULATE, Actor.EMPTY));
            if (extractedSim == 0) continue;
            ItemStack extracted = ((ItemResource) (stack.resource())).toItemStack().copyWithCount(extractedSim);
            ItemStack rested = callback.apply(extracted);
            currentToCollect -= rested.getCount();
            if (currentToCollect > 0) {
                itemStorage.extract(stack.resource(), currentToCollect, Action.EXECUTE, Actor.EMPTY);
                toCollect -= currentToCollect;
                if (toCollect <= 0)
                    break;
            }
        }
        current++;
    }

    @Override
    public void setExtract(List<ItemStack> itemList, ItemStackUtil.MATCH_TYPE matchNbt) {
        this.task = itemList;
        this.matchNbt = matchNbt;
        current = 0;
    }

    @Override
    public void setExtractByExisting(Predicate<ItemStack> predicate) {
        if (itemStorage != null)
            setExtract(
                    itemStorage
                            .getAll()
                            .stream()
                            .filter(stack -> stack.resource() instanceof ItemResource)
                            .map(stack -> ((ItemResource) stack.resource()).toItemStack())
                            .filter(predicate)
                            .toList(), ItemStackUtil.MATCH_TYPE.MATCHING);
        else
            setExtract(List.of(), ItemStackUtil.MATCH_TYPE.MATCHING);
    }

    @Override
    public void reset() {
        super.reset();
        this.current = 0;
    }

    @Override
    public void finish() {
        super.finish();
        if (task != null)
            current = task.size();
    }
}
