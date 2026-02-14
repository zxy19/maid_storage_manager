package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.DummyCollector;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftMenu;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class CraftGuideGeneratorUpdate implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CraftGuideGeneratorUpdate> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "craft_guide_generator_update"
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, CraftGuideGeneratorUpdate> STREAM_CODEC = StreamCodec.of(
            (b, t) -> t.toBytes(b),
            CraftGuideGeneratorUpdate::new
    );
    public final List<List<Ingredient>> ingredients;
    public final List<List<ItemStack>> outputs;

    public CraftGuideGeneratorUpdate(DummyCollector dummyCollector) {
        this.ingredients = dummyCollector.ingredients;
        this.outputs = dummyCollector.outputs;
    }

    public CraftGuideGeneratorUpdate(FriendlyByteBuf buffer) {
        this.ingredients = buffer.readCollection(ArrayList::new, (t) -> t.readCollection(ArrayList::new, tt -> tt.readJsonWithCodec(Ingredient.CODEC)));
        this.outputs = buffer.readCollection(ArrayList::new, (t) -> t.readCollection(ArrayList::new, (tt) -> ItemStackUtil.parseStack(((RegistryFriendlyByteBuf) tt).registryAccess(), tt.readNbt())));
    }

    public static void handle(Player player, CraftGuideGeneratorUpdate p) {
        if (player.containerMenu instanceof CommonCraftMenu menu) {
            menu.generatedRecipes.syncFromServer(p.ingredients, p.outputs);
            menu.generatedUpdated = true;
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeCollection(ingredients, (t, i) -> t.writeCollection(i, (tt, ii) -> tt.writeJsonWithCodec(Ingredient.CODEC, ii)));
        buffer.writeCollection(outputs, (t, i) -> t.writeCollection(i, (tt, ii) -> tt.writeNbt(ItemStackUtil.saveStack(((RegistryFriendlyByteBuf) tt).registryAccess(), ii))));
    }

}
