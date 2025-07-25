package studio.fantasyit.maid_storage_manager.storage.rs;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.craft.context.special.RsCraftingAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.type.RSType;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageCraftDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

abstract public class AbstractRSContext extends AbstractFilterableBlockStorage implements IStorageCraftDataProvider {


    ServerLevel level;
    Target target;
    boolean done = false;
    protected Set<PlatformResourceKey> craftable;
    protected Storage itemStorage;
    public AbstractGridBlockEntity be;

    private void reinit() {
        if (level.getBlockEntity(target.pos) instanceof AbstractGridBlockEntity gbe) {
            be = gbe;
            itemStorage = gbe.getItemStorage();
            craftable = gbe.getAutocraftableResources();
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
        List<CraftGuideData> list = new ArrayList<>(
                itemStorage
                        .getAll()
                        .stream()
                        .filter(key -> key.resource() instanceof ItemResource)
                        .map(k -> (ItemResource) k.resource())
                        .filter(k -> k.toItemStack().is(ItemRegistry.CRAFT_GUIDE.get()))
                        .map(k -> k.toItemStack().get(DataComponentRegistry.CRAFT_GUIDE_DATA))
                        .filter(Objects::nonNull)
                        .filter(CraftGuideData::available)
                        .toList()
        );
        craftable.stream()
                .filter(key -> key instanceof ItemResource)
                .map(k -> (ItemResource) k)
                .map(key -> {
                    List<CraftGuideStepData> steps = List.of(new CraftGuideStepData(
                            target,
                            List.of(),
                            List.of(key.toItemStack()),
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
