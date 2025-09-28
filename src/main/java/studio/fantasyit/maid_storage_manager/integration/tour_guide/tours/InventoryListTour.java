package studio.fantasyit.maid_storage_manager.integration.tour_guide.tours;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.tour_guide.api.helper.TourDataBuilder;
import studio.fantasyit.tour_guide.data.ITourDataFactory;
import studio.fantasyit.tour_guide.mark.gui.GuiMainTipMark;
import studio.fantasyit.tour_guide.mark.world.EntityMark;
import studio.fantasyit.tour_guide.step.TourStepId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InventoryListTour {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "inventory_list");
    public static final TourStepId<EntityMaid> STEP_MAID = new TourStepId<>(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "inventory_list_maid"), EntityMaid.class);
    public static final TourStepId<ItemStack> STEP_TARGET_ITEM = new TourStepId<>(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "inventory_list_itemstack"), ItemStack.class);

    public static final String TRIGGER_WRITE_INV = "maid_write_inv_list";
    public static final String TRIGGER_CLICK_INV = "player_click_inventory_list_item";

    public static final ResourceLocation GUI_INVENTORY_SCREEN = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "gui/inventory_list");


    public static ITourDataFactory get() {
        return new TourDataBuilder()
                //第一步：防止女仆，调整工作模式
                .step(STEP_MAID)
                .chat(Component.literal("请放置一个女仆，并将其切换到仓库管理工作模式"))
                .mainTipNoGui(Component.literal("请放置一个女仆，并将其切换到仓库管理工作模式"))
                .withToGetData(data -> {
                    ServerPlayer player = data.getPlayer();
                    Optional<EntityMaid> first = player.level().getEntities(
                                    EntityTypeTest.forClass(EntityMaid.class),
                                    player.getBoundingBox().inflate(7, 7, 7),
                                    entity -> player.getUUID().equals(entity.getOwnerUUID()) && entity.getTask().getUid().equals(StorageManageTask.TASK_ID)
                            ).stream()
                            .findFirst();
                    return first.orElse(null);
                }, Component.literal("没有找到女仆，请放置一个女仆，并将其切换到仓库管理工作模式"))
                .add()
                //第二步：放置容器，放置物品
                .stepAnonymous()
                .chat(Component.literal("为了让女仆能够工作，你需要先准备一些存储容器，例如箱子、木桶等。在女仆的工作范围内放置若干个这样的容器，并放入一些物品，然后点击完成键"))
                .mainTipNoGui(Component.literal("准备一些容器（箱子、木桶等），将其放置在女仆的工作范围内并放入一些物品"))
                .unfinishReason(data -> {
                    EntityMaid maid = data.getData(STEP_MAID);
                    ServerPlayer player = data.getPlayer();
                    if (BlockPos.betweenClosedStream(
                                    new AABB(maid.blockPosition()).inflate(7, 7, 7)
                            ).map(t ->
                                    MaidStorage.getInstance().isValidTarget((ServerLevel) player.level(), maid, t)
                            )
                            .anyMatch(Objects::nonNull)) {
                        return null;
                    }
                    return Component.literal("没有找到合适的容器，请放置一些合适的容器");
                })
                .add()
                //第三步：等女仆查看物品
                .step(STEP_TARGET_ITEM)
                .chats(List.of(
                        Component.literal("女仆正在查看所有容器，请等待"),
                        Component.literal("如果女仆已经查看过每个容器至少一次，点击完成")
                ))
                .mainTipNoGui(Component.literal(
                        "等待女仆依次查看所有的容器，如果女仆已经查看过每个容器至少一次，点击完成"
                ))
                .withToGetData(
                        data -> MemoryUtil.getViewedInventory(data.getData(STEP_MAID)).flatten().stream().findFirst().map(t -> t.itemStack).orElse(null),
                        Component.literal("女仆目前还没有找到任何物品，你是否忘记在箱子里放些东西了呢？")
                )
                .add()
                //第四步：交互，获取库存清单
                .stepAnonymous()
                .chat(Component.literal("请将手持空白的库存清单，并右键女仆"))
                .mainTipNoGui(Component.literal("手持空白的库存清单右键女仆，女仆会将已经写好的库存清单交还给你"))
                .dynamicMarks((t, d) -> {
                    t.add(new EntityMark(d.getData(STEP_MAID).getId(), 0xffff0000, Component.literal("手持空白的库存清单右键")));
                })
                .triggers(List.of(TRIGGER_WRITE_INV))
                .add()
                //第五步：选择目标物品，查看存储位置
                .stepAnonymous()
                .mainTipNoGui(Component.literal("手持库存清单右键打开GUI"))
                .dynamicChats((tChat, data) -> {
                    tChat.add(Component.literal("拿到了库存清单，让我们来试试寻找一个物品。这里我们来找一个").append(data.getData(STEP_TARGET_ITEM).getHoverName()));
                    tChat.add((Component.literal("点击右键打开库存清单，并选择你要寻找的物品")));
                })
                .dynamicMarks((t, d) -> {
                    t.add(new GuiMainTipMark(GUI_INVENTORY_SCREEN, Component.literal("在下面的列表中，找到").append(d.getData(STEP_TARGET_ITEM).getHoverName()).append("并左键单击"), false));
                })
                .onFinish(d -> {
                    d.getPlayer().sendSystemMessage(Component.literal("现在，").append(d.getData(STEP_TARGET_ITEM).getHoverName()).append("的位置已经被标注在了世界中"));
                    return true;
                })
                .triggers(List.of(TRIGGER_CLICK_INV))
                .add()
                //结束
                .onFinish(p -> p.sendSystemMessage(Component.literal("你已完成当前教程").withStyle(ChatFormatting.GREEN)))
                .getBuilder();
    }
}
