package studio.fantasyit.maid_storage_manager.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;

import java.util.List;

import static net.minecraft.client.renderer.entity.ItemRenderer.getFoilBuffer;

public class FilterListRenderer extends BlockEntityWithoutLevelRenderer {
    static FilterListRenderer instance;

    public static FilterListRenderer getInstance() {
        if (instance == null) {
            instance = new FilterListRenderer();
        }
        return instance;
    }

    private final BlockEntityRenderDispatcher dispatcher;

    public FilterListRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
        this.dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @Override
    public void renderByItem(@NotNull ItemStack itemStack,
                             @NotNull ItemDisplayContext context,
                             @NotNull PoseStack pose,
                             @NotNull MultiBufferSource multiBufferSource,
                             int light,
                             int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                new ModelResourceLocation(MaidStorageManager.MODID, "filter_list_base", "inventory")
        );
        for (var rendertype : model.getRenderTypes(itemStack, false)) {
            VertexConsumer vertexconsumer = getFoilBuffer(multiBufferSource, rendertype, true, itemStack.hasFoil());
            Minecraft.getInstance().getItemRenderer().renderModelLists(model, itemStack, light, overlay, pose, vertexconsumer);
        }

        ListTag list = itemStack.getOrCreateTag().getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        List<ItemStack> items = list
                .stream()
                .map(e -> ((CompoundTag) e).getCompound(FilterListItem.TAG_ITEMS_ITEM))
                .map(ItemStack::of)
                .filter(e -> !e.isEmpty())
                .toList();
        if (!items.isEmpty()) {
            int i = (Minecraft.getInstance().player.tickCount / 20) % items.size();
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                pose.pushPose();
                if (context != ItemDisplayContext.FIXED) {
                    pose.translate(0.22, 0.22, 0.54);
                } else {
                    Quaternionf rotation = new Quaternionf();
                    rotation.rotateAxis((float) Math.PI, 0, 1, 0);
                    pose.mulPose(rotation);
                    pose.translate(-0.78, 0.23, -0.46);
                }
//                pose.translate(0.04, 0.2, 0.45F);
                pose.scale(0.55f,0.55f, 0.001F);
                pose.translate(0.5F, 0.5F, 0.5F);
                Minecraft.getInstance().getItemRenderer().render(
                        item,
                        ItemDisplayContext.GUI,
                        false,
                        pose,
                        multiBufferSource,
                        light,
                        overlay,
                        Minecraft.getInstance().getItemRenderer().getModel(
                                item,
                                null,
                                null,
                                0
                        )
                );
                pose.popPose();
            }
        }
    }
}
