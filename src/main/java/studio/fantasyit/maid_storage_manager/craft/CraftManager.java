package studio.fantasyit.maid_storage_manager.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.context.VirtualAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.*;
import studio.fantasyit.maid_storage_manager.craft.context.special.*;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftManager {

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
            this.actionsMap.put(action.type(), action);
        }
    }

    private void fireInternal(CollectCraftEvent event) {
        event.addCraftType(new CommonType());
        event.addCraftType(new CraftingType());
        event.addCraftType(new AltarType());
        event.addCraftType(new FurnaceType());
        event.addCraftType(new SmithingType());
        event.addCraftType(new AnvilType());
        event.addCraftType(new StoneCuttingType());
        event.addAction(
                CommonPlaceItemAction.TYPE,
                CommonPlaceItemAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                true,
                3,
                0
        );
        event.addAction(
                CommonTakeItemAction.TYPE,
                CommonTakeItemAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                true,
                0,
                3
        );
        event.addAction(
                CommonThrowItemAction.TYPE,
                CommonThrowItemAction::new,
                PathTargetLocator::throwItemPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                3,
                0
        );
        event.addAction(
                CommonPickupItemAction.TYPE,
                CommonPickupItemAction::new,
                PathTargetLocator::besidePosOrExactlyPos,
                CraftAction.PathEnoughLevel.VERY_CLOSE.value,
                true,
                0,
                3
        );
        event.addAction(
                CommonUseAction.TYPE_R,
                CommonUseAction::new,
                PathTargetLocator::touchPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                1,
                1
        );
        event.addAction(
                CommonAttackAction.TYPE_L,
                CommonAttackAction::new,
                PathTargetLocator::touchPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                1,
                1
        );
        event.addAction(
                CommonIdleAction.TYPE,
                CommonIdleAction::new,
                PathTargetLocator::touchPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                0,
                3
        );
        event.addAction(
                CraftingRecipeAction.TYPE,
                CraftingRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                9,
                10
        );
        event.addAction(
                AltarType.TYPE,
                AltarRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                6,
                1
        );
        event.addAction(
                AltarUseAction.TYPE,
                AltarUseAction::new,
                PathTargetLocator::touchPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                false,
                6,
                1
        );
        event.addAction(
                FurnaceType.TYPE,
                VirtualAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                2,
                1
        );
        event.addAction(
                SmithingType.TYPE,
                SmithingRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                3,
                1
        );
        event.addAction(
                AnvilType.TYPE,
                AnvilRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                2,
                1
        );
        event.addAction(
                StoneCuttingType.TYPE,
                StoneCuttingRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                1,
                1
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
        return action.provider().create(maid, craftGuideData, craftGuideStepData, layer);
    }

    public List<CraftAction> getCommonActions() {
        return this.actions.stream().filter(CraftAction::canBeCommon).toList();
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

    public CraftAction getNextAction(CraftAction action) {
        boolean found = false;
        for (CraftAction craftAction : this.getCommonActions()) {
            if (found) return craftAction;
            if (craftAction.type().equals(action.type())) found = true;
        }
        return this.getCommonActions().get(0);
    }
}
