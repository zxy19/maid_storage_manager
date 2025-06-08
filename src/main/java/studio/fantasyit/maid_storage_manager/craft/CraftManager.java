package studio.fantasyit.maid_storage_manager.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.autogen.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.context.VirtualAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.*;
import studio.fantasyit.maid_storage_manager.craft.context.special.*;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.type.*;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.tacz.TaczRecipe;

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
    protected Map<ResourceLocation, IAutoCraftGuideGenerator> autoCraftGuideGenerators;

    public void collect() {
        ArrayList<ICraftType> list = new ArrayList<>();
        ArrayList<CraftAction> actions = new ArrayList<>();
        ArrayList<IAutoCraftGuideGenerator> autoCraftGuideGenerators = new ArrayList<>();
        CollectCraftEvent event = new CollectCraftEvent(list, actions, autoCraftGuideGenerators);
        fireInternal(event);
        MinecraftForge.EVENT_BUS.post(event);

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
        for (IAutoCraftGuideGenerator autoCraftGuideGenerator : autoCraftGuideGenerators) {
            this.autoCraftGuideGenerators.put(autoCraftGuideGenerator.getType(), autoCraftGuideGenerator);
        }
    }

    private void fireInternal(CollectCraftEvent event) {
        event.addCraftType(new CommonType());
        event.addCraftType(new CraftingType());
        event.addCraftType(new AltarType());
        event.addCraftType(new FurnaceType());
        event.addCraftType(new BrewingType());
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
                PathTargetLocator::nearByNoLimitation,
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
                FurnaceType.TYPE,
                VirtualAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                2,
                1
        );
        event.addAction(
                BrewingType.TYPE,
                VirtualAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                3,
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

        if (ModList.get().isLoaded("ae2") && Config.enableAe2Sup) {
            event.addCraftType(new AE2Type());
            event.addAction(
                    AE2Type.TYPE,
                    AeCraftingAction::new,
                    PathTargetLocator::commonNearestAvailablePos,
                    CraftAction.PathEnoughLevel.NORMAL.value,
                    false,
                    0,
                    1
            );
        }
        if (ModList.get().isLoaded("refinedstorage") && Config.enableRsSup) {
            event.addCraftType(new RSType());
            event.addAction(
                    RSType.TYPE,
                    RsCraftingAction::new,
                    PathTargetLocator::commonNearestAvailablePos,
                    CraftAction.PathEnoughLevel.NORMAL.value,
                    false,
                    0,
                    1
            );
        }
        if (Integrations.tacz()) {
            TaczRecipe.addType(event);
        }
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

    public List<IAutoCraftGuideGenerator> getAutoCraftGuideGenerators() {
        return this.autoCraftGuideGenerators.values().stream().toList();
    }
}
