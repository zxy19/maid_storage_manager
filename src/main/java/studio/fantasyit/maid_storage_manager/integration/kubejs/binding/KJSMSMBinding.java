package studio.fantasyit.maid_storage_manager.integration.kubejs.binding;

import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.context.common.*;
import studio.fantasyit.maid_storage_manager.craft.type.*;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.ae2.Ae2Storage;
import studio.fantasyit.maid_storage_manager.storage.create.place.CreateChainConveyorStorage;
import studio.fantasyit.maid_storage_manager.storage.create.stock.CreateStockTickerStorage;
import studio.fantasyit.maid_storage_manager.storage.qio.QIOStorage;
import studio.fantasyit.maid_storage_manager.storage.rs.RSStorage;

public class KJSMSMBinding {
    public ResourceLocation CRAFT_TYPE_COMMON = CommonType.TYPE;
    public ResourceLocation CRAFT_TYPE_AE2 = AE2Type.TYPE;
    public ResourceLocation CRAFT_TYPE_ALTAR = AltarType.TYPE;
    public ResourceLocation CRAFT_TYPE_ANVIL = AnvilType.TYPE;
    public ResourceLocation CRAFT_TYPE_BREWING = BrewingType.TYPE;
    public ResourceLocation CRAFT_TYPE_CRAFTING = CraftingType.TYPE;
    public ResourceLocation CRAFT_TYPE_FURNACE = FurnaceType.TYPE;
    public ResourceLocation CRAFT_TYPE_RS = RSType.TYPE;
    public ResourceLocation CRAFT_TYPE_SMITHING = SmithingType.TYPE;
    public ResourceLocation CRAFT_TYPE_STONECUTTING = StoneCuttingType.TYPE;
    public ResourceLocation CRAFT_TYPE_TACZ = TaczType.TYPE;

    public ResourceLocation CRAFT_ACTION_COMMON_TAKE_ITEM = CommonTakeItemAction.TYPE;
    public ResourceLocation CRAFT_ACTION_COMMON_ATTACK = CommonAttackAction.TYPE;
    public ResourceLocation CRAFT_ACTION_COMMON_PLACE_ITEM = CommonPlaceItemAction.TYPE;
    public ResourceLocation CRAFT_ACTION_COMMON_USE = CommonUseAction.TYPE;
    public ResourceLocation CRAFT_ACTION_COMMON_THROW = CommonTakeItemAction.TYPE;
    public ResourceLocation CRAFT_ACTION_COMMON_PICKUP = CommonPickupItemAction.TYPE;
    public ResourceLocation CRAFT_ACTION_AE2 = AE2Type.TYPE;
    public ResourceLocation CRAFT_ACTION_ALTAR = AltarType.TYPE;
    public ResourceLocation CRAFT_ACTION_ANVIL = AnvilType.TYPE;
    public ResourceLocation CRAFT_ACTION_BREWING = BrewingType.TYPE;
    public ResourceLocation CRAFT_ACTION_CRAFTING = CraftingType.TYPE;
    public ResourceLocation CRAFT_ACTION_FURNACE = FurnaceType.TYPE;
    public ResourceLocation CRAFT_ACTION_RS = RSType.TYPE;
    public ResourceLocation CRAFT_ACTION_SMITHING = SmithingType.TYPE;
    public ResourceLocation CRAFT_ACTION_STONECUTTING = StoneCuttingType.TYPE;
    public ResourceLocation CRAFT_ACTION_TACZ = TaczType.TYPE;

    public ResourceLocation STORAGE_ITEM_HANDLER = ItemHandlerStorage.TYPE;
    public ResourceLocation STORAGE_AE2 = Ae2Storage.TYPE;
    public ResourceLocation STORAGE_RS = RSStorage.TYPE;
    public ResourceLocation STORAGE_CREATE_STOCK_TICKER = CreateStockTickerStorage.TYPE;
    public ResourceLocation STORAGE_CREATE_CHAIN_CONVEYOR = CreateChainConveyorStorage.TYPE;
    public ResourceLocation STORAGE_QIO = QIOStorage.TYPE;

    public CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_BESIDE_OR_EXACTLY = PathTargetLocator::besidePosOrExactlyPos;
    public CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_COMMON = PathTargetLocator::commonNearestAvailablePos;
    public CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_EXACTLY = PathTargetLocator::exactlySidedPos;
    public CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_TOUCH = PathTargetLocator::touchPos;
    public CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_THROW_ITEM = PathTargetLocator::throwItemPos;
    public CraftAction.CraftActionPathFindingTargetProvider PATH_FINDING_NO_LIMITATION = PathTargetLocator::nearByNoLimitation;
}
