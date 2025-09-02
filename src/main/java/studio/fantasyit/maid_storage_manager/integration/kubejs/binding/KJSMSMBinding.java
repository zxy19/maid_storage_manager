package studio.fantasyit.maid_storage_manager.integration.kubejs.binding;

import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.context.common.*;
import studio.fantasyit.maid_storage_manager.craft.context.special.AeCraftingAction;
import studio.fantasyit.maid_storage_manager.craft.context.special.RsCraftingAction;
import studio.fantasyit.maid_storage_manager.craft.type.*;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.ae2.Ae2Storage;
import studio.fantasyit.maid_storage_manager.storage.create.place.CreateChainConveyorStorage;
import studio.fantasyit.maid_storage_manager.storage.create.stock.CreateStockTickerStorage;
import studio.fantasyit.maid_storage_manager.storage.qio.QIOStorage;
import studio.fantasyit.maid_storage_manager.storage.rs.RSStorage;

public class KJSMSMBinding {
    public final ResourceLocation UNAVAILABLE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "unavailable");

    public final ResourceLocation CRAFT_TYPE_COMMON = CommonType.TYPE;
    public final ResourceLocation CRAFT_TYPE_AE2;
    public final ResourceLocation CRAFT_TYPE_ALTAR = AltarType.TYPE;
    public final ResourceLocation CRAFT_TYPE_ANVIL = AnvilType.TYPE;
    public final ResourceLocation CRAFT_TYPE_BREWING = BrewingType.TYPE;
    public final ResourceLocation CRAFT_TYPE_CRAFTING = CraftingType.TYPE;
    public final ResourceLocation CRAFT_TYPE_FURNACE = FurnaceType.TYPE;
    public final ResourceLocation CRAFT_TYPE_RS;
    public final ResourceLocation CRAFT_TYPE_SMITHING = SmithingType.TYPE;
    public final ResourceLocation CRAFT_TYPE_STONECUTTING = StoneCuttingType.TYPE;
//    public final ResourceLocation CRAFT_TYPE_TACZ;

    public final ResourceLocation CRAFT_ACTION_COMMON_TAKE_ITEM = CommonTakeItemAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_COMMON_ATTACK = CommonAttackAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_COMMON_PLACE_ITEM = CommonPlaceItemAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_COMMON_SPLIT_ITEM = CommonSplitItemAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_COMMON_USE = CommonUseAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_COMMON_THROW = CommonTakeItemAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_COMMON_PICKUP = CommonPickupItemAction.TYPE;
    public final ResourceLocation CRAFT_ACTION_AE2;
    public final ResourceLocation CRAFT_ACTION_ALTAR = AltarType.TYPE;
    public final ResourceLocation CRAFT_ACTION_ANVIL = AnvilType.TYPE;
    public final ResourceLocation CRAFT_ACTION_BREWING = BrewingType.TYPE;
    public final ResourceLocation CRAFT_ACTION_CRAFTING = CraftingType.TYPE;
    public final ResourceLocation CRAFT_ACTION_FURNACE = FurnaceType.TYPE;
    public final ResourceLocation CRAFT_ACTION_RS;
    public final ResourceLocation CRAFT_ACTION_SMITHING = SmithingType.TYPE;
    public final ResourceLocation CRAFT_ACTION_STONECUTTING = StoneCuttingType.TYPE;

    public final ActionOption<Boolean> ACTION_OPTION_OPTIONAL = ActionOption.OPTIONAL;
    public final ActionOption<Boolean> ACTION_OPTION_WAIT = CommonIdleAction.OPTION_WAIT;
    public final ActionOption<CommonUseAction.USE_TYPE> ACTION_OPTION_USE_METHOD = CommonUseAction.OPTION_USE_METHOD;
    public final ActionOption<CommonAttackAction.USE_TYPE> ACTION_OPTION_ATTACK_METHOD = CommonAttackAction.OPTION_USE_METHOD;

    public final long ACTION_MARK_HAND_RELATED = CraftAction.MARK_HAND_RELATED;
    public final long ACTION_MARK_NO_OCCUPATION = CraftAction.MARK_NO_OCCUPATION;
    public final long ACTION_MARK_NO_MARKS = CraftAction.MARK_NO_MARKS;

    public final ResourceLocation STORAGE_ITEM_HANDLER = ItemHandlerStorage.TYPE;
    public final ResourceLocation STORAGE_AE2;
    public final ResourceLocation STORAGE_RS;
    public final ResourceLocation STORAGE_CREATE_STOCK_TICKER;
    public final ResourceLocation STORAGE_CREATE_CHAIN_CONVEYOR;
    public final ResourceLocation STORAGE_QIO;

    public final CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_BESIDE_OR_EXACTLY = PathTargetLocator::besidePosOrExactlyPos;
    public final CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_COMMON = PathTargetLocator::commonNearestAvailablePos;
    public final CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_EXACTLY = PathTargetLocator::exactlySidedPos;
    public final CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_TOUCH = PathTargetLocator::touchPos;
    public final CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_THROW_ITEM = PathTargetLocator::throwItemPos;
    public final CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_NO_LIMITATION = PathTargetLocator::nearByNoLimitation;

    public KJSMSMBinding() {
        CRAFT_TYPE_AE2 = Integrations.ae2() ? AE2Type.TYPE : UNAVAILABLE;
        CRAFT_TYPE_RS = Integrations.rs() ? RSType.TYPE : UNAVAILABLE;
//        CRAFT_TYPE_TACZ = Integrations.tacz() ? TaczType.TYPE : UNAVAILABLE;
        CRAFT_ACTION_AE2 = Integrations.ae2() ? AeCraftingAction.TYPE : UNAVAILABLE;
        CRAFT_ACTION_RS = Integrations.rs() ? RsCraftingAction.TYPE : UNAVAILABLE;

        STORAGE_AE2 = Integrations.ae2() ? Ae2Storage.TYPE : UNAVAILABLE;
        STORAGE_RS = Integrations.rs() ? RSStorage.TYPE : UNAVAILABLE;
        STORAGE_QIO = Integrations.mekanism() ? QIOStorage.TYPE : UNAVAILABLE;
        STORAGE_CREATE_STOCK_TICKER = Integrations.create6() ? CreateStockTickerStorage.TYPE : UNAVAILABLE;
        STORAGE_CREATE_CHAIN_CONVEYOR = Integrations.create6() ? CreateChainConveyorStorage.TYPE : UNAVAILABLE;
    }

    public boolean isAvailable(ResourceLocation type) {
        return !type.equals(UNAVAILABLE);
    }
}
