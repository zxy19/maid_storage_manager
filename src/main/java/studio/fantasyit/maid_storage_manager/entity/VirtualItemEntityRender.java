package studio.fantasyit.maid_storage_manager.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VirtualItemEntityRender extends EntityRenderer<VirtualItemEntity> {
    private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
    private static final int ITEM_COUNT_FOR_5_BUNDLE = 48;
    private static final int ITEM_COUNT_FOR_4_BUNDLE = 32;
    private static final int ITEM_COUNT_FOR_3_BUNDLE = 16;
    private static final int ITEM_COUNT_FOR_2_BUNDLE = 1;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public VirtualItemEntityRender(EntityRendererProvider.Context p_174198_) {
        super(p_174198_);
        this.itemRenderer = p_174198_.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    protected int getRenderAmount(ItemStack p_115043_) {
        int i = 1;
        if (p_115043_.getCount() > 48) {
            i = 5;
        } else if (p_115043_.getCount() > 32) {
            i = 4;
        } else if (p_115043_.getCount() > 16) {
            i = 3;
        } else if (p_115043_.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    public void render(VirtualItemEntity p_115036_, float p_115037_, float p_115038_, PoseStack p_115039_, MultiBufferSource p_115040_, int p_115041_) {
        p_115039_.pushPose();
        ItemStack itemstack = p_115036_.getItem();
        int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
        this.random.setSeed((long) i);
        BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, p_115036_.level(), (LivingEntity) null, p_115036_.getId());
        boolean flag = bakedmodel.isGui3d();
        int j = this.getRenderAmount(itemstack);
        float f = 0.25F;
        float f1 = Mth.sin(((float) p_115036_.getAge() + p_115038_) / 10.0F + p_115036_.bobOffs) * 0.1F + 0.1F;
        float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        p_115039_.translate(0.0F, f1 + 0.25F * f2, 0.0F);
        float f3 = (p_115036_.getAge() + p_115038_) / 20.0F + p_115036_.bobOffs;
        p_115039_.mulPose(Axis.YP.rotation(f3));
        if (!flag) {
            float f7 = -0.0F * (float) (j - 1) * 0.5F;
            float f8 = -0.0F * (float) (j - 1) * 0.5F;
            float f9 = -0.09375F * (float) (j - 1) * 0.5F;
            p_115039_.translate(f7, f8, f9);
        }

        for (int k = 0; k < j; ++k) {
            p_115039_.pushPose();
            if (k > 0) {
                if (flag) {
                    float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    p_115039_.translate(f11, f13, f10);
                } else {
                    float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    p_115039_.translate(f12, f14, 0.0D);
                }
            }

            this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, p_115039_, p_115040_, p_115041_, OverlayTexture.NO_OVERLAY, bakedmodel);
            p_115039_.popPose();
            if (!flag) {
                p_115039_.translate(0.0, 0.0, 0.09375F);
            }
        }

        p_115039_.popPose();
        super.render(p_115036_, p_115037_, p_115038_, p_115039_, p_115040_, p_115041_);
    }

    @Override
    public ResourceLocation getTextureLocation(VirtualItemEntity p_115034_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
