package studio.fantasyit.maid_storage_manager.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.context.VirtualAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.*;
import studio.fantasyit.maid_storage_manager.craft.context.special.*;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.config.GeneratingConfig;
import studio.fantasyit.maid_storage_manager.craft.generator.type.ae2.GeneratorAE2Charger;
import studio.fantasyit.maid_storage_manager.craft.generator.type.ae2.GeneratorAE2Inscriber;
import studio.fantasyit.maid_storage_manager.craft.generator.type.ae2.GeneratorAE2ItemTransform;
import studio.fantasyit.maid_storage_manager.craft.generator.type.ars.GeneratorArsNouveauApparatus;
import studio.fantasyit.maid_storage_manager.craft.generator.type.ars.GeneratorArsNouveauEnchanting;
import studio.fantasyit.maid_storage_manager.craft.generator.type.ars.GeneratorArsNouveauImbuement;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.type.botania.*;
import studio.fantasyit.maid_storage_manager.craft.generator.type.create.*;
import studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism.*;
import studio.fantasyit.maid_storage_manager.craft.generator.type.misc.GeneratorAltar;
import studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla.*;
import studio.fantasyit.maid_storage_manager.craft.type.*;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.kubejs.KJSEventPort;
import studio.fantasyit.maid_storage_manager.integration.tacz.TaczRecipe;

import java.util.*;
import java.util.function.BiPredicate;

public class CraftManager {

    public static final CraftManager INSTANCE = new CraftManager();

    public static CraftManager getInstance() {
        return INSTANCE;
    }

    protected List<ICraftType> types;
    protected Map<ResourceLocation, ICraftType> typesMap;
    protected List<CraftAction> actions;
    protected Map<ResourceLocation, CraftAction> actionsMap;
    protected List<IAutoCraftGuideGenerator> autoCraftGuideGenerators;
    protected Map<ResourceLocation, List<BiPredicate<ItemStack, ItemStack>>> itemStackPredicates;

    public void collect() {
        ArrayList<ICraftType> list = new ArrayList<>();
        ArrayList<CraftAction> actions = new ArrayList<>();
        ArrayList<IAutoCraftGuideGenerator> autoCraftGuideGenerators = new ArrayList<>();
        Map<ResourceLocation, List<BiPredicate<ItemStack, ItemStack>>> itemStackPredicates = new HashMap<>();
        CollectCraftEvent event = new CollectCraftEvent(list, actions, autoCraftGuideGenerators, itemStackPredicates);
        fireInternal(event);
        ModLoader.get().postEvent(event);
        if (Integrations.kjs())
            KJSEventPort.postCraftCollect(event);

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
        this.autoCraftGuideGenerators = autoCraftGuideGenerators;
        this.itemStackPredicates = itemStackPredicates;
        GeneratingConfig.load();
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
                4,
                0,
                List.of(ActionOption.OPTIONAL, CommonPlaceItemAction.OPTION_SPLIT)
        );
        event.addAction(
                CommonTakeItemAction.TYPE,
                CommonTakeItemAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                true,
                0,
                4,
                List.of(ActionOption.OPTIONAL)
        );
        event.addAction(
                CommonThrowItemAction.TYPE,
                CommonThrowItemAction::new,
                PathTargetLocator::throwItemPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                4,
                0,
                List.of(ActionOption.OPTIONAL)
        );
        event.addAction(
                CommonPickupItemAction.TYPE,
                CommonPickupItemAction::new,
                PathTargetLocator::besidePosOrExactlyPos,
                CraftAction.PathEnoughLevel.VERY_CLOSE.value,
                true,
                0,
                4,
                List.of(ActionOption.OPTIONAL)
        );
        event.addAction(
                CommonUseAction.TYPE,
                CommonUseAction::new,
                PathTargetLocator::touchPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                CraftAction.MARK_HAND_RELATED,
                2,
                2,
                List.of(ActionOption.OPTIONAL, CommonUseAction.OPTION_USE_METHOD)
        );
        event.addAction(
                CommonAttackAction.TYPE,
                CommonAttackAction::new,
                PathTargetLocator::touchPos,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                CraftAction.MARK_HAND_RELATED,
                2,
                2,
                List.of(ActionOption.OPTIONAL, CommonAttackAction.OPTION_USE_METHOD)
        );
        event.addAction(
                CommonIdleAction.TYPE,
                CommonIdleAction::new,
                PathTargetLocator::nearByNoLimitation,
                CraftAction.PathEnoughLevel.CLOSER.value,
                true,
                0,
                4,
                List.of(CommonIdleAction.OPTION_WAIT)
        );
        event.addAction(
                CraftingRecipeAction.TYPE,
                CraftingRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, true,
                9,
                10,
                List.of()
        );
        event.addAction(
                AltarType.TYPE,
                AltarRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, false,
                6,
                1,
                List.of()
        );
        event.addAction(
                FurnaceType.TYPE,
                VirtualAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, false,
                2,
                1,
                List.of()
        );
        event.addAction(
                BrewingType.TYPE,
                VirtualAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, false,
                3,
                1,
                List.of()
        );
        event.addAction(
                SmithingType.TYPE,
                SmithingRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, true,
                3,
                1,
                List.of()
        );
        event.addAction(
                AnvilType.TYPE,
                AnvilRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, false,
                2,
                1,
                List.of()
        );
        event.addAction(
                StoneCuttingType.TYPE,
                StoneCuttingRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false, true,
                1,
                1,
                List.of()
        );

        event.addAutoCraftGuideGenerator(new GeneratorCraftingTable());
        event.addAutoCraftGuideGenerator(new GeneratorSmithingTable());
        event.addAutoCraftGuideGenerator(new GeneratorFurnace());
        event.addAutoCraftGuideGenerator(new GeneratorStoneCutter());
        event.addAutoCraftGuideGenerator(new GeneratorAltar());
        event.addAutoCraftGuideGenerator(new GeneratorBrewing());
        event.addAutoCraftGuideGenerator(new GeneratorWatering());
        event.addAutoCraftGuideGenerator(new GeneratorStripping());

        if (ModList.get().isLoaded("ae2") && Config.enableAe2Sup) {
            event.addCraftType(new AE2Type());
            event.addAction(
                    AE2Type.TYPE,
                    AeCraftingAction::new,
                    PathTargetLocator::commonNearestAvailablePos,
                    CraftAction.PathEnoughLevel.NORMAL.value,
                    false, true,
                    0,
                    1,
                    List.of()
            );
        }
        if (ModList.get().isLoaded("refinedstorage") && Config.enableRsSup) {
            event.addCraftType(new RSType());
            event.addAction(
                    RSType.TYPE,
                    RsCraftingAction::new,
                    PathTargetLocator::commonNearestAvailablePos,
                    CraftAction.PathEnoughLevel.NORMAL.value,
                    false, true,
                    0,
                    1,
                    List.of()
            );
        }
        if (Integrations.taczRecipe()) {
            TaczRecipe.addType(event);
        }
        if (Integrations.create()) {
            event.addAutoCraftGuideGenerator(new GeneratorCreatePress());
            event.addAutoCraftGuideGenerator(new GeneratorCreateCompact());
            event.addAutoCraftGuideGenerator(new GeneratorCreateMix());
            event.addAutoCraftGuideGenerator(new GeneratorCreateMilling());
            event.addAutoCraftGuideGenerator(new GeneratorCreateCrushing());
            event.addAutoCraftGuideGenerator(new GeneratorCreateFanRecipes());
            event.addAutoCraftGuideGenerator(new GeneratorCreateUse());
            event.addAutoCraftGuideGenerator(new GeneratorCreateDeployer());
        }
        if (Integrations.mekanism()) {
            event.addAutoCraftGuideGenerator(new GeneratorMekEnrichment());
            event.addAutoCraftGuideGenerator(new GeneratorMekInfusion());
            event.addAutoCraftGuideGenerator(new GeneratorMekCrushing());
            event.addAutoCraftGuideGenerator(new GeneratorMekSawing());
            event.addAutoCraftGuideGenerator(new GeneratorMekOsmiumComp());
            event.addAutoCraftGuideGenerator(new GeneratorMekCombine());
            event.addAutoCraftGuideGenerator(new GeneratorMekSmelter());
        }
        if (Integrations.ae2()) {
            event.addAutoCraftGuideGenerator(new GeneratorAE2Inscriber());
            event.addAutoCraftGuideGenerator(new GeneratorAE2Charger());
            event.addAutoCraftGuideGenerator(new GeneratorAE2ItemTransform());
        }
        if (Integrations.botania()) {
            event.addAutoCraftGuideGenerator(new GeneratorBotaniaRunicAltar());
            event.addAutoCraftGuideGenerator(new GeneratorBotaniaApothecary());
            event.addAutoCraftGuideGenerator(new GeneratorBotaniaManaInfuse());
            event.addAutoCraftGuideGenerator(new GeneratorBotaniaMythicalFlower());
            event.addAutoCraftGuideGenerator(new GeneratorBotaniaElven());
            event.addAutoCraftGuideGenerator(new GeneratorBotaniaDaisy());
        }
        if (Integrations.ars()) {
            event.addAutoCraftGuideGenerator(new GeneratorArsNouveauImbuement());
            event.addAutoCraftGuideGenerator(new GeneratorArsNouveauApparatus());
            event.addAutoCraftGuideGenerator(new GeneratorArsNouveauEnchanting());
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

    public @NotNull CraftAction getAction(ResourceLocation type) {
        return Objects.requireNonNull(this.actionsMap.get(type));
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
        return this.autoCraftGuideGenerators;
    }

    public List<CraftAction> getActions() {
        return this.actions;
    }

    public List<ICraftType> getTypes() {
        return this.types;
    }

    public Optional<Boolean> predicateItemStack(ItemStack stack, ItemStack target) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!this.itemStackPredicates.containsKey(key)) return Optional.empty();
        for (BiPredicate<ItemStack, ItemStack> predicate : this.itemStackPredicates.get(key)) {
            if (!predicate.test(stack, target)) return Optional.of(false);
        }
        return Optional.of(true);
    }
}
