package studio.fantasyit.maid_storage_manager.integration.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.network.IngredientRequestC2SPacket;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class IngredientRequestClient {
    private final static Identifier R = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/jei_request.png");
    public static boolean keyPressed = false;
    public static int preferMaidId = -1;
    public static int multiple = 1;
    public static Component preferMaidName = Component.literal("");
    public static float maidAnimated = 0f;
    public static int lastAnimatedMaidId = -1;

    public static int hasButtonTick = 0;

    public static void hasButton() {
        hasButtonTick = 5;
    }

    public static void drawIcon(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        guiGraphics.blit(R, xOffset, yOffset, 0, 0, 9, 9, 9, 9);
        guiGraphics.text(
                Minecraft.getInstance().font,
                Component.literal(String.valueOf(multiple)),
                xOffset + 11,
                yOffset + 5,
                0xFFFFFFFF,
                false);
    }

    private static ItemStack getPriorityFromUUID(UUID uuid, ItemStack itemStack, int number) {
        return InventoryListDataClient.getInstance().get(uuid)
                .stream()
                .filter(i -> ItemStackUtil.isSame(i.itemStack, itemStack, false))
                .filter(i -> i.totalCount > number)
                .max(Comparator.comparingInt(i -> i.totalCount))
                .map(i -> i.itemStack)
                .orElse(ItemStack.EMPTY);
    }

    public static void processRequestNearByClient(List<List<ItemStack>> data) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        NonNullList<ItemStack> inventoryItems = player.getInventory().getNonEquipmentItems();
        Object inventoryListUUID = InventoryListUtil.getInventoryListUUIDFromPlayerInv(player.getInventory().getNonEquipmentItems());
        List<ItemStack> costed = new ArrayList<>();
        List<ItemStack> toRequest = new ArrayList<>();
        for (List<ItemStack> toSelect : data) {
            if (toSelect.size() == 0) continue;
            MutableObject<ItemStack> priority = new MutableObject<>();
            boolean notFound = true;
            for (ItemStack itemStack : toSelect) {
                MutableInt sum = new MutableInt(0);
                Stream<ItemStack> itemStackStream = inventoryItems.stream()
                        .filter(i -> ItemStackUtil.isSame(i, itemStack, false))
                        .map(i -> {
                            ItemStack costedCurrent = costed
                                    .stream()
                                    .filter(ii -> ItemStackUtil.isSame(ii, itemStack, false))
                                    .findFirst().orElse(ItemStack.EMPTY);
                            sum.add(i.getCount() - costedCurrent.getCount());
                            return i;
                        });

                if (sum.intValue() > 0) {
                    if (priority.getValue() == null) {
                        priority.setValue(itemStack);
                    }
                    if (sum.intValue() >= itemStack.getCount()) {
                        notFound = false;
                        break;
                    }
                }

                itemStackStream
                        .forEach(i -> {
                            costed.add(i.copyWithCount(i.getCount() * multiple));
                        });
            }
            if (notFound && inventoryListUUID != null) {
                priority.setValue(InventoryListUtil.getMatchingFromInventory(inventoryListUUID, toSelect));
            }
            if (priority.getValue() == null)
                priority.setValue(toSelect.get(0));
            if (priority.getValue() == null)
                continue;
            if (notFound) {
                ItemStackUtil.addToList(toRequest, priority.getValue(), false);
            }
        }
        toRequest.forEach(i -> i.setCount(i.getCount() * IngredientRequestClient.multiple));
        if (toRequest.size() > 0) {
            ClientPacketDistributor.sendToServer(new IngredientRequestC2SPacket(toRequest, IngredientRequestClient.preferMaidId));
        }
    }

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
        if (hasButtonTick > 0)
            hasButtonTick--;
        tickAnimation();
    }


    static long lastTick = 0;

    public static void tickAnimation() {
        long currentTick = System.currentTimeMillis();
        if (lastTick == 0) {
            lastTick = currentTick;
            return;
        }
        float deltaTick = (float) (currentTick - lastTick) / 1000f;
        lastTick = currentTick;

        if (keyPressed && preferMaidId != -1 && lastAnimatedMaidId == preferMaidId) {
            maidAnimated += deltaTick / 0.5;
        } else {
            maidAnimated -= deltaTick / 0.5;
        }
        maidAnimated = Math.max(0, Math.min(1, maidAnimated));
        if (preferMaidId != lastAnimatedMaidId && maidAnimated == 0) {
            lastAnimatedMaidId = preferMaidId;
        }
    }

    public static void renderGui(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, Screen screen) {
        // FIXME: MC 26.1 - renderEntityInInventoryFollowsMouse renamed to renderEntityInInventoryFollowsAngle with unknown new signature
        // Maid entity rendering when JEI request is active needs updating for new rendering API
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