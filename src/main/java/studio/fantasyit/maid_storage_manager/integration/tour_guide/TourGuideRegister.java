package studio.fantasyit.maid_storage_manager.integration.tour_guide;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.api.event.TourDataRegisterEvent;
import studio.fantasyit.tour_guide.data.ITourStepData;
import studio.fantasyit.tour_guide.data.TourData;
import studio.fantasyit.tour_guide.mark.IMark;
import studio.fantasyit.tour_guide.mark.ServerScreenPredicatorMarks;
import studio.fantasyit.tour_guide.mark.gui.GuiRectMark;
import studio.fantasyit.tour_guide.mark.gui.GuiTextMark;
import studio.fantasyit.tour_guide.mark.world.BlockMark;

import java.util.List;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TourGuideRegister {
    @SubscribeEvent
    public static void onRegisterTourGuide(TourDataRegisterEvent event) {
        ResourceLocation T1ID = new ResourceLocation(MaidStorageManager.MODID, "test_s1");
        ResourceLocation T2ID = new ResourceLocation(MaidStorageManager.MODID, "test_s2");
        event.register(new ResourceLocation(MaidStorageManager.MODID, "test"), p ->
                new TourData(
                        List.of(
                                new ITourStepData<BlockPos>() {
                                    @Override
                                    public ResourceLocation getId() {
                                        return T1ID;
                                    }

                                    @Override
                                    public List<IMark> init(TourData data) {
                                        p.sendSystemMessage(Component.literal("导游开始!请选择一个位置，然后点击完成当前步骤按钮"));
                                        return List.of(
                                                new GuiRectMark(ServerScreenPredicatorMarks.NO_GUI,
                                                        2, 2, 84, 54,
                                                        0xA000FF00,
                                                        0xA0000000
                                                ),
                                                new GuiTextMark(ServerScreenPredicatorMarks.NO_GUI,
                                                        Component.literal("1.选择一个位置\n2.点击完成当前步骤按钮"),
                                                        5,
                                                        5,
                                                        80,
                                                        0xffffffff)
                                        );
                                    }

                                    @Override
                                    public Component getUnfinishReason() {
                                        return null;
                                    }

                                    @Override
                                    public void skipped() {
                                    }

                                    @Override
                                    public BlockPos finish() {
                                        return p.blockPosition();
                                    }
                                },
                                new ITourStepData<Boolean>() {
                                    BlockPos dd;

                                    @Override
                                    public ResourceLocation getId() {
                                        return T2ID;
                                    }

                                    @Override
                                    public List<IMark> init(TourData data) {
                                        dd = data.getData(T1ID);
                                        p.sendSystemMessage(Component.literal("你刚才选择的位置是：" + dd + "。现在，离开这个位置再次点击完成即可结束导游。"));
                                        return List.of(
                                                new BlockMark(dd, null, 0xffffffff, Component.literal("离开这个位置"))
                                        );
                                    }

                                    @Override
                                    public Component getUnfinishReason() {
                                        if (p.blockPosition().equals(dd))
                                            return Component.literal("请离开这个位置");
                                        return null;
                                    }

                                    @Override
                                    public void skipped() {
                                    }

                                    @Override
                                    public Boolean finish() {
                                        return null;
                                    }
                                }
                        ),
                        p
                ).setOnFinish(() -> p.sendSystemMessage(Component.literal("导游结束")))
        );
    }
}
