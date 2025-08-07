package studio.fantasyit.maid_storage_manager.render.map_like;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;
import studio.fantasyit.maid_storage_manager.event.RenderHandMapLikeEvent;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.render.base.ICustomGraphics;

import java.util.UUID;

public class ProgressPadRender implements RenderHandMapLikeEvent.MapLikeRenderer {
    public static final ProgressPadRender INSTANCE = new ProgressPadRender();
    private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/process_pad.png"));

    @Override
    public float getWidth(RenderHandMapLikeEvent.MapLikeRenderContext context) {
        return switch (context) {
            case ITEM_FRAME_LARGE -> 132.5f;
            case ITEM_FRAME_SMALL -> 66;
            default -> 98;
        };
    }

    @Override
    public float getHeight(RenderHandMapLikeEvent.MapLikeRenderContext context) {
        return switch (context) {
            case ITEM_FRAME_SMALL -> 65.3f;
            default -> 132;
        };
    }

    @Override
    public RenderType backgroundRenderType(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack) {
        return MAP_BACKGROUND;
    }


    private static final ResourceLocation ELEM = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/process_pad_element.png");
    private static final ImageAsset LINE = new ImageAsset(ELEM, 14, 39, 154, 2);
    private static final ImageAsset PROGRESS_ALL = new ImageAsset(ELEM, 14, 37, 154, 1);
    private static final ImageAsset PROGRESS_ALL_WAIT = new ImageAsset(ELEM, 14, 35, 154, 1);
    private static final ImageAsset PROGRESS_ALL_ERROR = new ImageAsset(ELEM, 14, 33, 154, 1);
    private static final ImageAsset BORDER = new ImageAsset(ELEM, 14, 44, 78, 41);
    private static final ImageAsset BORDER_SMALL = new ImageAsset(ELEM, 90, 44, 78, 22);
    private static final ImageAsset PROGRESS = new ImageAsset(ELEM, 15, 89, 72, 1);
    private static final ImageAsset PROGRESS_WAIT = new ImageAsset(ELEM, 15, 93, 72, 1);
    private static final ImageAsset PROGRESS_ERROR = new ImageAsset(ELEM, 15, 91, 72, 1);

    private static final int COLOR_TEXT_ITEM = 0xff2b0609;
    private static final int COLOR_TEXT_PROGRESS = 0xff2b0609;
    private static final int COLOR_TEXT_MAID = 0xff917200;
    private static final int COLOR_TEXT_COUNT = 0xffffffff;

    public ImageAsset pickProgress(ProgressData.Status status) {
        return switch (status) {
            case WAITING -> PROGRESS_WAIT;
            case FAILED -> PROGRESS_ERROR;
            default -> PROGRESS;
        };
    }

    public ImageAsset pickProgressAll(ProgressData.Status status) {
        return switch (status) {
            case WAITING -> PROGRESS_ALL_WAIT;
            case FAILED -> PROGRESS_ALL_ERROR;
            default -> PROGRESS_ALL;
        };
    }

    @Override
    public void renderOnHand(ICustomGraphics graphics, ItemStack pStack, int pCombinedLight, RenderHandMapLikeEvent.MapLikeRenderContext context) {
        int tcr = Minecraft.getInstance().player.tickCount / 20;
        float widthScaleFactor = (getWidth(context) - 12) / 78;
        float maxWidth = getWidth(context) / 90 * 160;
        graphics.pose().scale(0.49f, 0.49f, 1);
        blit(graphics, LINE, 14, 39, widthScaleFactor);
        UUID bindingUUID = ProgressPad.getBindingUUID(pStack);
        if (bindingUUID == null) return;
        ProgressData.ProgressMeta progressMeta = ProgressData.ProgressMeta.fromItemStack(pStack);
        ProgressPad.Style style = progressMeta.style();
        ProgressData data = MaidProgressData.getByMaid(progressMeta);
        if (data == null) return;
        Font font = Minecraft.getInstance().font;
        int maidNameWidth = font.width(data.maidName);
        graphics.drawString(font, data.maidName.getVisualOrderText(), (int) (maxWidth - 2 - maidNameWidth), 11, COLOR_TEXT_ITEM, false);
        if (!data.workGroups.isEmpty()) {
            Component group = data.workGroups.get(tcr % data.workGroups.size());
            int gWidth = font.width(group);
            graphics.drawString(font, group.getVisualOrderText(), (int) (maxWidth - 2 - gWidth), 25, COLOR_TEXT_MAID, false);
        }
        ItemStack currentProcessingItem;
        if (!data.items.isEmpty()) {
            currentProcessingItem = data.items.get(tcr % data.items.size());
        } else {
            currentProcessingItem = Items.FEATHER.getDefaultInstance();
        }
        graphics.pose().pushPose();
        graphics.pose().translate(10, 10, 0);
        graphics.pose().scale(1.4f, 1.4f, 1);
        drawItemStackWithCount(graphics, currentProcessingItem, font);
        graphics.pose().popPose();

        if (data.total > 0) {
            if (context == RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_SMALL)
                graphics.drawString(font, Component.translatable("gui.maid_storage_manager.progress_pad.progress_all", data.progress, data.total).getVisualOrderText(), 12, 45, COLOR_TEXT_PROGRESS, false);
            else
                graphics.drawString(font, Component.translatable("gui.maid_storage_manager.progress_pad.progress_all", data.progress, data.total).getVisualOrderText(), 41, 17, COLOR_TEXT_PROGRESS, false);
            blit(graphics, pickProgressAll(data.status), 14, 37, widthScaleFactor * data.progress / data.total);
        } else if (data.working.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.maid_storage_manager.progress_pad.no_task").getVisualOrderText(), 12, 45, COLOR_TEXT_PROGRESS, false);
        }

        int ix = 14;
        int iy = 44;
        int row = 0;
        int COLS = switch (context) {
            case ITEM_FRAME_LARGE -> 3;
            case ITEM_FRAME_SMALL -> 1;
            default -> 2;
        };
        int MAX_ROWS = switch (context) {
            case ITEM_FRAME_SMALL -> 1;
            default -> 5;
        };
        if (style == ProgressPad.Style.SMALL)
            MAX_ROWS *= 2;
        if (context == RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_SMALL) {
            iy += 20;
        }
        //90 - 14 + 1 = 76
        //83 - 44 + 1 = 40
        int lineHeight = switch (style) {
            case NORMAL -> 39;
            case SMALL -> 20;
        };
        for (int i = 0; i < data.working.size(); i++) {
            ProgressData.TaskProgress progress = data.working.get(i);
            (switch (style) {
                case NORMAL -> BORDER;
                case SMALL -> BORDER_SMALL;
            }).blit(graphics, ix, iy);

            if (!progress.outputs().isEmpty()) {
                ItemStack display = progress.outputs().get(tcr % progress.outputs().size());
                graphics.pose().pushPose();
                if (style == ProgressPad.Style.SMALL) {
                    graphics.pose().translate(ix + 2, iy + 2, 0);
                } else {
                    graphics.pose().translate(ix + 5, iy + 5, 0);
                    graphics.pose().scale(1.4f, 1.4f, 1f);
                }
                drawItemStackWithCount(graphics, display, font);
                graphics.pose().popPose();

                int nameW = Math.min(font.width(display.getHoverName()), 40);
                int dNameY = switch (style) {
                    case NORMAL -> 6;
                    case SMALL -> 2;
                };
                drawCenteredString(graphics, font, display.getHoverName(), ix + 76 - 2 - nameW, iy + dNameY, 40, COLOR_TEXT_ITEM, false);
            }
            if (style == ProgressPad.Style.NORMAL) {
                String progressText = String.format("%d/%d", progress.progress(), progress.total());
                int progressW = font.width(progressText);
                graphics.drawString(font, progressText, ix + 76 - 2 - progressW, iy + lineHeight - 11, COLOR_TEXT_PROGRESS, false);
            }
            blit(graphics, pickProgress(progress.status()), ix + 2, iy + lineHeight - 1, 1.0f * progress.progress() / progress.total());


            if (!progress.taker().isEmpty()) {
                Component taker = progress.taker().get(tcr % progress.taker().size());
                if (style == ProgressPad.Style.SMALL) {
                    float _scale = 0.8f;
                    float takerWidth = Math.min(font.width(taker), 40) * _scale;
                    graphics.pose().pushPose();
                    graphics.pose().translate(ix + 76 - 2 - takerWidth, iy + lineHeight - 11 * _scale, 0);
                    graphics.pose().scale(_scale, _scale, 1f);
                    drawCenteredString(graphics, font, taker, 0, 0, 40, COLOR_TEXT_MAID, false);
                    graphics.pose().popPose();
                } else
                    drawCenteredString(graphics, font, taker, ix + 2, iy + lineHeight - 11, 40, COLOR_TEXT_MAID, false);
            }

            if (i % COLS != COLS - 1) {
                ix += 76;
            } else {
                ix = 14;
                iy += lineHeight;
                row++;
                if (row >= MAX_ROWS) {
                    break;
                }
            }
        }
    }

    public void drawCenteredString(ICustomGraphics graphics, Font pFont, Component pText, int pX, int pY, int maxWidth, int pColor, boolean shadow) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        int textWidth = pFont.width(formattedcharsequence);
        int drawWidth = Math.max(textWidth, maxWidth);
        int alignWidth = Math.min(maxWidth, textWidth);
        float scale = (float) maxWidth / drawWidth;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, scale);
        graphics.drawString(pFont, formattedcharsequence, pX / scale, (pY - 3 + (14 - 8 * scale) / 2) / scale, pColor, shadow);
        graphics.pose().popPose();
    }

    @Override
    public void extraTransform(PoseStack pPoseStack, RenderHandMapLikeEvent.MapLikeRenderContext context) {
        if (context == RenderHandMapLikeEvent.MapLikeRenderContext.BOTH_HANDS) {
            pPoseStack.scale(1.1f, 1.1f, 1);
            pPoseStack.translate(-6, 2f, -10);
        } else if (context == RenderHandMapLikeEvent.MapLikeRenderContext.MAIN_HAND || context == RenderHandMapLikeEvent.MapLikeRenderContext.OFF_HAND) {
            pPoseStack.scale(1.2f, 1.2f, 1);
            pPoseStack.translate(-10, 0, 0);
        }
    }

    protected void blit(ICustomGraphics graphics, ImageAsset asset, int x, int y, float xScale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(xScale, 1, 1);
        asset.blit(graphics, 0, 0);
        graphics.pose().popPose();
    }

    protected void drawItemStackWithCount(ICustomGraphics graphics, ItemStack pStack, Font font) {
        graphics.renderItem(pStack, 0, 0);
        if (pStack.getCount() != 1) {
            String s = String.valueOf(pStack.getCount());
            int wCount = font.width(s);
            graphics.pose().translate(16 - wCount * 0.7, 12, 1);
            graphics.pose().scale(0.7f, 0.7f, 1);
            graphics.drawString(font, s, 0, 0, COLOR_TEXT_COUNT, true);
        }
    }
}
