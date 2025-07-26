//package studio.fantasyit.maid_storage_manager.menu.craft.tacz;
//
//import com.tacz.guns.crafting.GunSmithTableIngredient;
//import com.tacz.guns.crafting.GunSmithTableRecipe;
//import com.tacz.guns.init.ModRecipe;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.Slot;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.network.PacketDistributor;
//import org.jetbrains.annotations.Nullable;
//import oshi.util.tuples.Pair;
//import studio.fantasyit.maid_storage_manager.integration.tacz.TaczRecipe;
//import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
//import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
//import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
//import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
//import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
//import studio.fantasyit.maid_storage_manager.network.Network;
//import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TaczCraftMenu extends AbstractCraftMenu<TaczCraftMenu> {
//    private final List<Pair<ItemStack, String>> taczRecipes = new ArrayList<>();
//    GunSmithTableRecipe recipe = null;
//    ICraftGuiPacketReceiver screenListener;
//
//    public TaczCraftMenu(int p_38852_, Player player) {
//        super(GuiRegistry.CRAFT_GUIDE_MENU_TACZ.get(), p_38852_, player);
//        ResourceLocation blockId = TaczRecipe.getBlockId(player.level(), stepDataContainer.step.storage.pos);
//        List<GunSmithTableRecipe> allRecipesFor = TaczRecipe.getAllRecipesForBlockId(player.level(), blockId);
//        RegistryAccess registryAccess = player.level().registryAccess();
//        allRecipesFor.forEach(recipe -> {
//            taczRecipes.add(new Pair<>(recipe.getResultItem(registryAccess), recipe.getId().toString()));
//        });
//    }
//
//    public void setScreenListener(ICraftGuiPacketReceiver screenListener) {
//        this.screenListener = screenListener;
//    }
//
//    @Override
//    protected void addFilterSlots() {
//        for (int x = 112, j = 0; j < 2; x += 20, j++) {
//            for (int y = 24, i = 0; i < 5; y += 18, i++) {
//                Slot slot = this.addSlot(new FilterSlot(stepDataContainer, i * 2 + j, x, y));
//                int index = i * 2 + j;
//                this.setSlotFilter(slot, itemStack -> {
//                    if (itemStack.isEmpty()) return true;
//                    if (recipe == null) return false;
//                    if (recipe.getInputs().size() <= index) return false;
//                    return recipe.getInputs().get(index).getIngredient().test(itemStack);
//                });
//            }
//        }
//        FilterSlot filterSlot = new FilterSlot(stepDataContainer, 10, 0, 0);
//        this.addSlot(filterSlot);
//        filterSlot.setActive(false);
//    }
//
//    @Override
//    protected void addSpecialSlots() {
//        for (int i = 0; i < 11; i++) {
//            this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(i), stepDataContainer));
//        }
//    }
//
//    @Override
//    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
//        switch (type) {
//            case EXTRA -> {
//                if (data != null) {
//                    stepDataContainer.step.setExtraData(data);
//                    if (screenListener != null)
//                        screenListener.handleGuiPacket(type, key, value, data);
//                    save();
//                }
//            }
//            case SET_ITEM -> {
//                if (data != null) {
//                    this.getSlot(key).set(ItemStack.of(data));
//                    save();
//                }
//            }
//        }
//    }
//
//    public void recalculateRecipe() {
//        CompoundTag data = stepDataContainer.step.getExtraData();
//        if (data == null) {
//            stepDataContainer.step.setExtraData(new CompoundTag());
//            data = stepDataContainer.step.getExtraData();
//        }
//        String blockId = data.getString("block_id");
//        String recipeId = data.getString("recipe_id");
//
//
//        Level level = player.level();
//        List<GunSmithTableRecipe> allRecipesFor = level.getRecipeManager().getAllRecipesFor(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get());
//        GunSmithTableRecipe recipe = allRecipesFor.stream().filter(r -> r.getId().toString().equals(recipeId)).findFirst().orElse(null);
//        ResourceLocation realBlockId = TaczRecipe.getBlockId(level, stepDataContainer.step.storage.pos);
//        if (!realBlockId.toString().equals(blockId)) {
//            recipe = null;
//        }
//        this.recipe = recipe;
//
//        if (recipe != null) {
//            List<GunSmithTableIngredient> inputs = recipe.getInputs();
//            for (int i = 0; i < stepDataContainer.inputCount; i++) {
//                if (i >= inputs.size())
//                    stepDataContainer.setItemNoTrigger(i, ItemStack.EMPTY);
//                else if (stepDataContainer.getItem(i).isEmpty() && inputs.get(i).getIngredient().getItems().length > 0) {
//                    stepDataContainer.setItemNoTrigger(i, inputs.get(i).getIngredient().getItems()[0]);
//                    stepDataContainer.setCount(i, inputs.get(i).getCount());
//                } else if (!inputs.get(i).getIngredient().test(stepDataContainer.getItem(i))) {
//                    stepDataContainer.setItemNoTrigger(i, inputs.get(i).getIngredient().getItems()[0]);
//                    stepDataContainer.setCount(i, inputs.get(i).getCount());
//                } else if (stepDataContainer.getCount(i) != inputs.get(i).getCount()) {
//                    stepDataContainer.setCount(i, inputs.get(i).getCount());
//                }
//            }
//            ItemStack output = recipe.getResult().getResult();
//            stepDataContainer.setItemNoTrigger(stepDataContainer.inputCount, output);
//            stepDataContainer.setCount(stepDataContainer.inputCount, output.getCount());
//        } else {
//            for (int i = 0; i < stepDataContainer.inputCount; i++) {
//                stepDataContainer.setItemNoTrigger(i, ItemStack.EMPTY);
//            }
//            stepDataContainer.setItemNoTrigger(stepDataContainer.inputCount, ItemStack.EMPTY);
//            stepDataContainer.setCount(stepDataContainer.inputCount, 1);
//        }
//
//        if (player instanceof ServerPlayer sp)
//            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp),
//                    new CraftGuideGuiPacket(
//                            CraftGuideGuiPacket.Type.EXTRA,
//                            0,
//                            0,
//                            stepDataContainer.step.extraData
//                    ));
//    }
//
//    public ResourceLocation getBlockId() {
//        return TaczRecipe.getBlockId(player.level(), stepDataContainer.step.storage.pos);
//    }
//
//    public String getRecipeId() {
//        if (stepDataContainer.step.getExtraData() == null)
//            return "";
//        return stepDataContainer.step.getExtraData().getString("recipe_id");
//    }
//
//    public void getAllRecipes(List<Pair<ItemStack, String>> taczRecipes) {
//        taczRecipes.addAll(this.taczRecipes);
//    }
//}
