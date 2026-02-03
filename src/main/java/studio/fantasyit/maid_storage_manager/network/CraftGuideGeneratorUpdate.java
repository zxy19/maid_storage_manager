package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.DummyCollector;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftMenu;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CraftGuideGeneratorUpdate {
    public final List<List<Ingredient>> ingredients;
    public final List<List<ItemStack>> outputs;

    public CraftGuideGeneratorUpdate(DummyCollector dummyCollector) {
        this.ingredients = dummyCollector.ingredients;
        this.outputs = dummyCollector.outputs;
    }

    public CraftGuideGeneratorUpdate(FriendlyByteBuf buffer) {
        this.ingredients = buffer.readCollection(ArrayList::new, (t) -> t.readCollection(ArrayList::new, Ingredient::fromNetwork));
        this.outputs = buffer.readCollection(ArrayList::new, (t) -> t.readCollection(ArrayList::new, tt -> ItemStackUtil.parseStack(Objects.requireNonNull(tt.readNbt()))));
    }

    public static void handle(Player player, CraftGuideGeneratorUpdate p) {
        if (player.containerMenu instanceof CommonCraftMenu menu) {
            menu.generatedRecipes.syncFromServer(p.ingredients, p.outputs);
            menu.generatedUpdated = true;
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeCollection(ingredients, (t, i) -> t.writeCollection(i, (tt, ii) -> ii.toNetwork(tt)));
        buffer.writeCollection(outputs, (t, i) -> t.writeCollection(i, (tt, ii) -> tt.writeNbt(ItemStackUtil.saveStack(ii))));
    }
}
