package studio.fantasyit.maid_storage_manager.integration.tour_guide.tours;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.tour_guide.api.helper.TourDataBuilder;
import studio.fantasyit.tour_guide.data.ITourDataFactory;
import studio.fantasyit.tour_guide.mark.gui.GuiMainTipMark;
import studio.fantasyit.tour_guide.mark.gui.GuiRectMark;
import studio.fantasyit.tour_guide.mark.gui.GuiSlotMark;
import studio.fantasyit.tour_guide.mark.gui.GuiTextMark;
import studio.fantasyit.tour_guide.mark.world.EntityMark;
import studio.fantasyit.tour_guide.step.TourStepId;

import java.util.List;
import java.util.Optional;

public class RequestListTour {
    public static ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "request_list_tour");
    public static ResourceLocation ID2 = new ResourceLocation(MaidStorageManager.MODID, "request_list_tour2");
    public static final ResourceLocation GUI_REQUEST_LIST = new ResourceLocation(MaidStorageManager.MODID, "gui/request_list");
    public static final ResourceLocation GUI_REQUEST_LIST_NO_OFFSET = new ResourceLocation(MaidStorageManager.MODID, "gui/request_list_no_offset");

    public record MaidInv(EntityMaid maid, ItemStack item) {
    }

    public static TourStepId<MaidInv> STEP_TO_REQUEST_ITEM = new TourStepId<>(new ResourceLocation(MaidStorageManager.MODID, "request_item"), MaidInv.class);

    public static ITourDataFactory get() {
        return new TourDataBuilder()
                .step(STEP_TO_REQUEST_ITEM)
                .chat(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_1.chat"))
                .mainTipNoGui(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_1.main"))
                .withToGetData(d -> {
                    Optional<EntityMaid> first = d.getPlayer().level()
                            .getEntities(
                                    EntityTypeTest.forClass(EntityMaid.class),
                                    d.getPlayer().getBoundingBox().inflate(10),
                                    e -> e.getTask().getUid().equals(StorageManageTask.TASK_ID) && d.getPlayer().getUUID().equals(e.getOwnerUUID())
                            )
                            .stream()
                            .filter(t -> !MemoryUtil.getViewedInventory(t).flatten().isEmpty())
                            .findFirst();
                    if (first.isEmpty()) {
                        return null;
                    }
                    EntityMaid maid = first.get();
                    return new MaidInv(maid, MemoryUtil.getViewedInventory(maid).flatten().stream().findFirst().map(t -> t.itemStack).orElseThrow(RuntimeException::new));
                }, Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_1.no_data"))
                .add()
                .step(new ResourceLocation(MaidStorageManager.MODID, "request_list_tour_step_2"))
                .chat(List.of(
                        Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_2.chat_1"),
                        Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_2.chat_2").withStyle(ChatFormatting.YELLOW),
                        Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_2.chat_3")
                ))
                .mainTipNoGui(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_2.main"))
                .allowSkip(true)
                .triggers(List.of("request_list_bind"))
                .unfinishReason(t -> {
                    if (RequestListItem.getStorageBlock(t.getPlayer().getMainHandItem()) == null)
                        return Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_2.no_data");
                    return null;
                })
                .add()
                .step(new ResourceLocation(MaidStorageManager.MODID, "request_list_tour_step_3"))
                .dynamicChats((tChat, data) -> {
                    tChat.add(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_3.chat", data.getData(STEP_TO_REQUEST_ITEM).item.getHoverName()));
                })
                .mainTipNoGui(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_3.main"))
                .dynamicMarks((tMark, data) -> {
                    tMark.add(new GuiMainTipMark(GUI_REQUEST_LIST_NO_OFFSET, Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_3.in_gui", data.getData(STEP_TO_REQUEST_ITEM).item.getDisplayName()), false));
                    tMark.add(new GuiSlotMark(GUI_REQUEST_LIST, 36, 0xffff0000));
                    tMark.add(new GuiTextMark(GUI_REQUEST_LIST, Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_3.in_gui_text"), -60, 0, 100, 0xffffffff, 0x60000000));
                    tMark.add(new GuiRectMark(GUI_REQUEST_LIST, 147, 90, 18, 18, 0xFF00FF00, 0x00000000));
                    tMark.add(new GuiTextMark(GUI_REQUEST_LIST, Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_3.in_gui_text_2"), 165, 108, 100, 0xffffffff, 0x60000000));
                })
                .unfinishReason(t -> {
                    ItemStack di = t.getData(STEP_TO_REQUEST_ITEM).item;
                    if (
                            RequestListItem.getItemStacksNotDone(t.getPlayer().getMainHandItem(), true)
                                    .stream()
                                    .noneMatch(tt -> ItemStackUtil.isSame(tt.getA(), di, false))
                    ) {
                        return Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_3.no_marked");
                    }
                    return null;
                })
                .triggers(List.of("item_selector_save"))
                .add()
                .step(new ResourceLocation(MaidStorageManager.MODID, "request_list_tour_step_4"))
                .chat(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_4.chat"))
                .mainTipNoGui(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_4.main"))
                .dynamicMarks((tMark, data) -> {
                    tMark.add(new EntityMark(data.getData(STEP_TO_REQUEST_ITEM).maid.getId(), 0xffff0000, Component.literal("手持请求列表右键")));
                })
                .noCondition(true)
                .triggers(List.of("request_list_take"))
                .add()
                .onFinish(t -> t.sendSystemMessage(Component.translatable("tour_guide.maid_storage_manager.request_list_tour.step_4.finish")))
                .getBuilder();
    }

    public static ITourDataFactory get2() {
        return new TourDataBuilder()
                .step(STEP_TO_REQUEST_ITEM)
                .chat(Component.translatable("tour_guide.maid_storage_manager.request_list_tour2.step_1.chat"))
                .noCondition(true)
                .add()
                .getBuilder();
    }
}
