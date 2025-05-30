package studio.fantasyit.maid_storage_manager.jei.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.network.JEIRequestPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class JEIRequestClient {
    public static boolean keyPressed = false;
    public static int preferMaidId = -1;
    public static int multiple = 1;
    public static Component preferMaidName = Component.literal("");
    public static IDrawable icon = new IDrawable() {
        private final static ResourceLocation R = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/jei_request.png");

        @Override
        public int getWidth() {
            return 9;
        }

        @Override
        public int getHeight() {
            return 9;
        }

        @Override
        public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
            guiGraphics.blit(R, xOffset, yOffset, 0, 0, 9, 9, 9, 9);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(xOffset + 11, yOffset + 8, 0);
            guiGraphics.pose().scale(0.5f, 0.5f, 1f);
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    String.valueOf(multiple),
                    0,
                    0,
                    0xFFFFFF);
            guiGraphics.pose().popPose();
        }
    };

    public static void tickClient() {
        if (keyPressed) {
            LocalPlayer player = Minecraft.getInstance().player;
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null || player == null)
                return;
            List<EntityMaid> entities = level.getEntities(
                    EntityTypeTest.forClass(EntityMaid.class),
                    player.getBoundingBox().inflate(10, 7, 10),
                    e -> e.isOwnedBy(player) &&
                            e.getTask().getUid().equals(StorageManageTask.TASK_ID) &&
                            MemoryUtil.getCurrentlyWorking(e) == ScheduleBehavior.Schedule.CO_WORK
            );
            if (preferMaidId != -1) {
                if (entities.stream().noneMatch(e -> e.getId() == preferMaidId))
                    preferMaidId = -1;
            }
            if (entities.size() == 0) {
                preferMaidId = -1;
            } else if (preferMaidId == -1) {
                preferMaidId = entities.get(0).getId();
                preferMaidName = entities.get(0).getDisplayName();
            }
        } else {
            preferMaidId = -1;
            multiple = 1;
        }
    }

    public static void processRequestNearByClient(IRecipeLayoutDrawable<?> recipeLayout) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        List<ItemStack> toRequest = new ArrayList<>();
        IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
        List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
        for (IRecipeSlotView slotView : slotViews) {
            Optional<ItemStack> first = slotView
                    .getItemStacks()
                    .filter(i -> player.getInventory().items.stream().anyMatch(i2 -> ItemStackUtil.isSame(i2, i, false)))
                    .findFirst();

            if (first.isPresent()) {
                MutableInt count = new MutableInt(first.get().getCount());
                player.getInventory().items.stream()
                        .filter(i -> ItemStackUtil.isSame(i, first.get(), false))
                        .forEach(i -> count.subtract(i.getCount()));
                if (count.intValue() > 0) {
                    ItemStackUtil.addToList(toRequest, first.get().copyWithCount(count.intValue()), false);
                }
            } else if (slotView.getItemStacks().findFirst().isPresent()) {
                ItemStackUtil.addToList(toRequest, slotView.getItemStacks().findFirst().get(), false);
            }
        }
        toRequest.forEach(i -> i.setCount(i.getCount() * multiple));
        if (toRequest.size() > 0) {
            Network.INSTANCE.send(PacketDistributor.SERVER.noArg(), new JEIRequestPacket(toRequest, preferMaidId));
        }
    }

    public static void scroll(double scrollDelta) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        int i = scrollDelta < 0 ? -1 : 1;
        if (player.isShiftKeyDown())
            i *= 10;
        multiple += i;
        multiple = Math.max(1, multiple);
    }
}