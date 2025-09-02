package studio.fantasyit.maid_storage_manager.menu.craft.tacz;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.special.TaczRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.type.TaczType;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.RollingTextWidget;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.ArrayList;
import java.util.List;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class TaczCraftScreen extends AbstractCraftScreen<TaczCraftMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/tacz.png");
    private static final ImageAsset BTN_SELECTED = new ImageAsset(background, 176 + 1, 24 + 1, 68 - 2, 18 - 2);
    private static final ImageAsset BTN_NORMAL = new ImageAsset(background, 176 + 1, 42 + 1, 68 - 2, 18 - 2);
    private static final ImageAsset BTN_BACK_NORMAL = new ImageAsset(background, 176, 60, 68, 18);
    private static final ImageAsset BTN_BACK_HOVER = new ImageAsset(background, 176, 78, 68, 18);
    private static final ImageAsset BTN_PAGEDOWN = new ImageAsset(background, 176, 0, 68, 6);
    private static final ImageAsset BTN_PAGEDOWN_HOVER = new ImageAsset(background, 176, 6, 68, 6);
    private static final ImageAsset BTN_PAGEUP = new ImageAsset(background, 176, 12, 68, 6);
    private static final ImageAsset BTN_PAGEUP_HOVER = new ImageAsset(background, 176, 18, 68, 6);
    int selected = -1;
    int page = 0;
    int maxPage = 0;
    protected List<SelectButtonWidget<Boolean>> buttons = new ArrayList<>();
    protected List<RollingTextWidget> rollingTexts = new ArrayList<>();
    SelectButtonWidget<Boolean> pageDownBtn;
    SelectButtonWidget<Boolean> pageUpBtn;
    protected List<Integer> pageNo = new ArrayList<>();
    private List<Pair<ItemStack, String>> taczRecipes = new ArrayList<>();
    private ResourceLocation blockId;

    public TaczCraftScreen(TaczCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background);
    }

    @Override
    protected void init() {
        this.getMenu().setScreenListener(this);
        initRecipe();
        super.init();
        String recipeId = menu.getRecipeId();
        for (int i = 0; i < taczRecipes.size(); i++) {
            if (taczRecipes.get(i).getB().equals(recipeId)) {
                selected = i;
                page = pageNo.get(i);
                updateButtons();
                break;
            }
        }
    }

    private void initRecipe() {
        taczRecipes.clear();
        blockId = menu.getBlockId();
        menu.getAllRecipes(taczRecipes);
    }

    @Override
    protected void addButtons() {
        buttons.clear();
        pageNo.clear();
        rollingTexts.forEach(rollingText -> rollingText.visible = false);
        rollingTexts.clear();
        int y = 38;
        int height = 130;
        int currentPage = 0;
        for (int i = 0; i < taczRecipes.size(); i++) {
            Pair<ItemStack, String> taczRecipe = taczRecipes.get(i);
            int finalI = i;
            SelectButtonWidget<Boolean> btn = addRenderableWidget(new SelectButtonWidget<>(
                    30 + 1,
                    y + 1,
                    (value) -> {
                        boolean toValue = (selected == finalI);
                        if (value != null) {
                            toValue = !toValue;
                            selected = toValue ? finalI : -1;
                            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(
                                    CraftGuideGuiPacket.Type.OPTION,
                                    CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_RECIPE_ID),
                                    0,
                                    CraftGuideGuiPacket.singleValue(this.blockId.toString())
                            ));
                            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(
                                    CraftGuideGuiPacket.Type.OPTION,
                                    CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_BLOCK_ID),
                                    0,
                                    CraftGuideGuiPacket.singleValue(toValue ? taczRecipe.getB() : "")
                            ));
                            updateButtons();
                        }
                        return new SelectButtonWidget.Option<>(
                                toValue,
                                toValue ? BTN_SELECTED : BTN_NORMAL,
                                toValue ? BTN_SELECTED : BTN_NORMAL,
                                Component.empty()
                        );
                    },
                    this
            ));
            RollingTextWidget text = addRenderableOnly(new RollingTextWidget(
                    getGuiLeft() + 50,
                    getGuiTop() + y,
                    48,
                    18,
                    taczRecipe.getA().getHoverName()
            ));
            rollingTexts.add(text);
            buttons.add(btn);
            pageNo.add(currentPage);
            y += BTN_BACK_NORMAL.h - 1;
            if (y + BTN_BACK_NORMAL.h - 1 > height) {
                y = 38;
                currentPage++;
            }
        }
        if (y == 38) currentPage--;
        maxPage = currentPage;

        pageDownBtn = addRenderableWidget(new SelectButtonWidget<>(
                30,
                127,
                (v) -> {
                    if (v != null) {
                        page++;
                        if (page > maxPage)
                            page = maxPage;
                        updateButtons();
                    }
                    return new SelectButtonWidget.Option<>(
                            true,
                            BTN_PAGEDOWN,
                            BTN_PAGEDOWN_HOVER,
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.page_down")
                    );
                },
                this
        ));
        pageUpBtn = addRenderableWidget(new SelectButtonWidget<>(
                        30,
                        29,
                        (v) -> {
                            if (v != null) {
                                page--;
                                if (page < 0)
                                    page = 0;
                                updateButtons();
                            }
                            return new SelectButtonWidget.Option<>(
                                    true,
                                    BTN_PAGEUP,
                                    BTN_PAGEUP_HOVER,
                                    Component.translatable("gui.maid_storage_manager.craft_guide.common.page_up")
                            );
                        },
                        this
                )
        );
        updateButtons();
    }

    protected void updateButtons() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setVisible(page == pageNo.get(i));
            buttons.get(i).setOption(null);
            rollingTexts.get(i).visible = (page == pageNo.get(i));
        }

        pageUpBtn.setVisible(page != 0);
        pageDownBtn.setVisible(page != maxPage);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(guiGraphics, p_97788_, p_97789_, p_97790_);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        for (SelectButtonWidget<Boolean> booleanSelectButtonWidget : buttons) {
            if (booleanSelectButtonWidget.isVisible()) {
                BTN_BACK_NORMAL.blit(guiGraphics, booleanSelectButtonWidget.getX() - 1, booleanSelectButtonWidget.getY() - 1);
            }
        }
        for (SelectButtonWidget<Boolean> button : buttons) {
            if (button.isVisible() && button.isHovered()) {
                BTN_BACK_HOVER.blit(guiGraphics, button.getX() - 1, button.getY() - 1);
            }
        }
        if (!menu.stepDataContainer.getItem(10).
                isEmpty()) {
            guiGraphics.drawString(font,
                    Component.translatable("gui.maid_storage_manager.craft_guide.tacz.amount",
                            menu.stepDataContainer.getCount(10)),
                    relX + 113,
                    relY + 127,
                    0xFFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        for (int i = 0; i < taczRecipes.size(); i++) {
            if (pageNo.get(i) != page) continue;
            graphics.renderItem(
                    taczRecipes.get(i).getA(),
                    buttons.get(i).getX(),
                    buttons.get(i).getY()
            );
        }
    }

    @Override
    public boolean mouseScrolled(double _x, double _y, double p_94688_) {
        double x = _x - getGuiLeft();
        double y = _y - getGuiTop();
        if (x >= 30 && y >= 38 && x <= 97 && y <= 128) {
            if (p_94688_ < 0)
                pageDownBtn.onClick(x, y);
            else if (p_94688_ > 0)
                pageUpBtn.onClick(x, y);
            return true;
        }
        return super.mouseScrolled(_x, _y, p_94688_);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);
        for (int i = 0; i < taczRecipes.size(); i++) {
            if (buttons.get(i).isVisible() && buttons.get(i).isHovered()) {
                graphics.renderTooltip(this.font,
                        taczRecipes.get(i).getA(),
                        x,
                        y
                );
            }
        }
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        if (type == CraftGuideGuiPacket.Type.OPTION) {
            String recipeId = menu.getRecipeId();
            for (int i = 0; i < taczRecipes.size(); i++) {
                if (taczRecipes.get(i).getB().equals(recipeId)) {
                    selected = i;
                    page = pageNo.get(i);
                    updateButtons();
                    break;
                }
            }
        }
    }
}