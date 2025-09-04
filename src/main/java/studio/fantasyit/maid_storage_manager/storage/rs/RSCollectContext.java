package studio.fantasyit.maid_storage_manager.storage.rs;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

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
        return super.isDone() || network == null || (task != null && current >= task.size());
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
        if (network == null || task == null || current >= task.size()) return;
        List<ItemStack> itemStack = List.of(task.get(current));
        int toCollect = task.get(current).getCount();
        if (matchNbt != ItemStackUtil.MATCH_TYPE.NOT_MATCHING) {
            itemStack = stackListStacks
                    .stream()
                    .filter(stack -> ItemStackUtil.isSame(stack.getStack(), task.get(current), matchNbt))
                    .map(StackListEntry::getStack)
                    .toList();
        }

        for (ItemStack stack : itemStack) {
            int currentToCollect = Math.min(toCollect, stack.getCount());
            ItemStack extractedSim = network.extractItem(stack, currentToCollect, Action.SIMULATE);
            if (extractedSim.isEmpty()) continue;
            ItemStack rested = callback.apply(extractedSim);
            currentToCollect -= rested.getCount();
            if (currentToCollect > 0) {
                network.extractItem(stack, currentToCollect, Action.PERFORM);
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
        if (stackListStacks != null)
            setExtract(
                    stackListStacks.stream()
                            .map(StackListEntry::getStack)
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
