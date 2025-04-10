package studio.fantasyit.maid_storage_manager.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.action.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.action.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.action.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.action.CraftingRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftManager {

    public record CraftAction(ResourceLocation type, CraftActionProvider provider, boolean canBeCommon,
                              int inputCount, int outputCount) {
    }

    @FunctionalInterface
    public interface CraftActionProvider {
        AbstractCraftActionContext create(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer);
    }

    public static final CraftManager INSTANCE = new CraftManager();

    public static CraftManager getInstance() {
        return INSTANCE;
    }

    protected List<ICraftType> types;
    protected Map<ResourceLocation, ICraftType> typesMap;
    protected List<CraftAction> actions;
    protected Map<ResourceLocation, CraftAction> actionsMap;


    public void collect() {
        ArrayList<ICraftType> list = new ArrayList<>();
        ArrayList<CraftAction> actions = new ArrayList<>();
        CollectCraftEvent event = new CollectCraftEvent(list, actions);
        MinecraftForge.EVENT_BUS.post(event);
        fireInternal(event);

        this.types = event.getCraftTypes();
        this.typesMap = new HashMap<>();
        for (ICraftType type : this.types) {
            this.typesMap.put(type.getType(), type);
        }

        this.actions = actions;
        this.actionsMap = new HashMap<>();
        for (CraftAction action : actions) {
            this.actionsMap.put(action.type, action);
        }
    }

    private void fireInternal(CollectCraftEvent event) {
        event.addCraftType(new CommonType());
        event.addAction(
                CommonPlaceItemAction.TYPE,
                CommonPlaceItemAction::new,
                true,
                3,
                0
        );
        event.addAction(
                CommonTakeItemAction.TYPE,
                CommonTakeItemAction::new,
                true,
                0,
                3
        );
        event.addAction(
                CraftingRecipeAction.TYPE,
                CraftingRecipeAction::new,
                false,
                9,
                3
        );
    }

    public @Nullable ICraftType getType(ResourceLocation type) {
        return this.typesMap.get(type);
    }

    public @Nullable AbstractCraftActionContext startCurrentStep(CraftLayer layer, EntityMaid maid) {
        @Nullable CraftGuideData data = layer.getCraftData().orElse(null);
        if (data == null) return null;
        @Nullable ICraftType type = getType(data.getType());
        if (type == null) return null;
        return type.start(
                maid,
                data,
                layer.getStepData(),
                layer
        );
    }

    public @Nullable AbstractCraftActionContext start(ResourceLocation type, EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        @Nullable CraftAction action = this.actionsMap.get(type);
        if (action == null) return null;
        return action.provider.create(maid, craftGuideData, craftGuideStepData, layer);
    }

    public List<CraftAction> getCommonActions() {
        return this.actions.stream().filter(action -> action.canBeCommon).toList();
    }

    public @Nullable CraftAction getAction(ResourceLocation type) {
        return this.actionsMap.get(type);
    }

    public @Nullable ResourceLocation getTargetType(ServerLevel level, BlockPos pos, Direction direction) {
        for (ICraftType type : this.types) {
            if (type.isSpecialType(level, pos, direction)) {
                return type.getType();
            }
        }
        return null;
    }

    public CraftAction getDefaultAction() {
        return this.getCommonActions().get(0);
    }
}
