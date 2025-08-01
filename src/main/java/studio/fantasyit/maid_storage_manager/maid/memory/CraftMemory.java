package studio.fantasyit.maid_storage_manager.maid.memory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftMemory extends AbstractTargetMemory {
    public static Codec<CraftMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(AbstractTargetMemory::getTargetData),
                    CraftGuideData.CODEC
                            .listOf()
                            .fieldOf("craftGuides")
                            .forGetter(CraftMemory::getCraftGuides),
                    Codec.BOOL.fieldOf("goPlacingBeforeCraft")
                            .forGetter(CraftMemory::isGoPlacingBeforeCraft),
                    CraftLayerChain.CODEC
                            .optionalFieldOf("craftPlan")
                            .forGetter(t -> Optional.ofNullable(t.plan)),
                    Target.CODEC.listOf()
                            .fieldOf("ignoreTargets")
                            .orElse(List.of())
                            .forGetter(CraftMemory::getIgnoreTargets),
                    Codec.BOOL.fieldOf("isGatheringDispatched")
                            .forGetter(CraftMemory::isGatheringDispatched)
            ).apply(instance, CraftMemory::new)
    );

    public CraftLayerChain plan;
    public List<CraftGuideData> craftGuides;
    public boolean goPlacingBeforeCraft;
    private int pathFindingFailCount = 0;
    private List<Target> ignoreTargets;
    private boolean isGatheringDispatched;

    public int calculatingTotal = -1;
    public int calculatingProgress = -1;

    public CraftMemory(TargetData targetData,
                       List<CraftGuideData> craftGuides,
                       boolean goPlacingBeforeCraft,
                       Optional<CraftLayerChain> plan,
                       List<Target> ignoreTargets,
                       boolean isGatheringDispatched
    ) {
        super(targetData);
        this.craftGuides = new ArrayList<>(craftGuides);
        this.goPlacingBeforeCraft = goPlacingBeforeCraft;
        this.plan = plan.orElse(null);
        this.ignoreTargets = new ArrayList<>(ignoreTargets);
        this.isGatheringDispatched = isGatheringDispatched;
    }

    public CraftMemory() {
        super();
        this.craftGuides = new ArrayList<>();
        this.goPlacingBeforeCraft = false;
        this.plan = null;
        this.ignoreTargets = new ArrayList<>();
        this.isGatheringDispatched = false;
    }

    public void setPlan(CraftLayerChain plan) {
        this.plan = plan;
        if (!plan.freeze) {
            plan.build();
        }
        if (!plan.isMaster())
            this.isGatheringDispatched = true;
        this.calculatingProgress = this.calculatingTotal = -1;
    }

    public void clearPlan() {
        this.plan = null;
        this.isGatheringDispatched = false;
        this.calculatingProgress = this.calculatingTotal = -1;
    }

    public boolean hasPlan() {
        return this.plan != null;
    }

    public CraftLayerChain plan() {
        return plan;
    }

    public void stopAndClearPlan(EntityMaid maid) {
        setGoPlacingBeforeCraft(true);
        ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_RESCHEDULE);
        ChatTexts.removeSecondary(maid);
        clearPlan();
        MemoryUtil.getRequestProgress(maid).setReturn(false);
    }

    public void initPreLayer() {
        this.isSwappingHandWhenCrafting = false;
    }

    public boolean isGatheringDispatched() {
        return isGatheringDispatched;
    }

    public void setGatheringDispatched(boolean isGatheringDispatched) {
        this.isGatheringDispatched = isGatheringDispatched;
    }

    public List<CraftGuideData> getCraftGuides() {
        return craftGuides;
    }

    public void addCraftGuide(CraftGuideData craftGuideData) {
        if (!craftGuideData.available())
            return;
        this.craftGuides.add(craftGuideData);
    }

    public void clearCraftGuides() {
        this.craftGuides.clear();
    }

    /// 继续合成前进行放置物品相关的控制选项

    public boolean isGoPlacingBeforeCraft() {
        return goPlacingBeforeCraft;
    }

    public void setGoPlacingBeforeCraft(boolean goPlacingBeforeCraft) {
        this.goPlacingBeforeCraft = goPlacingBeforeCraft;
    }


    ///  合成状态设置

    /**
     * 当上次合成成功时，调用这个方法。控制回存产物等。
     */
    public void lastSuccess() {
        this.goPlacingBeforeCraft = true;
    }

    public void resetAndMarkVis(ServerLevel level, EntityMaid maid) {
        this.resetVisitedPos();
        for (Target target : getIgnoreTargets()) {
            addVisitedPos(target);
        }
    }

    boolean isSwappingHandWhenCrafting = false;

    public boolean isSwappingHandWhenCrafting() {
        return isSwappingHandWhenCrafting;
    }

    public void setSwappingHandWhenCrafting(boolean isSwappingHandWhenCrafting) {
        this.isSwappingHandWhenCrafting = isSwappingHandWhenCrafting;
    }

    public int getPathFindingFailCount() {
        return pathFindingFailCount;
    }

    public void addPathFindingFailCount() {
        this.pathFindingFailCount++;
    }

    public void resetPathFindingFailCount() {
        this.pathFindingFailCount = 0;
    }

    public List<Target> getIgnoreTargets() {
        return ignoreTargets;
    }

    public void addIgnoreTargets(Target ignoreTargets) {
        this.ignoreTargets.add(ignoreTargets);
    }

    public void addIgnoreTargets(List<Target> ignoreTargets) {
        this.ignoreTargets.addAll(ignoreTargets);
    }

    public void clearIgnoreTargets() {
        this.ignoreTargets.clear();
    }

    public void addIgnoreTargetFromRequest(EntityMaid maid, ServerLevel level) {
        Target storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock != null) {
            clearIgnoreTargets();
            addIgnoreTargets(storageBlock);
            StorageAccessUtil.checkNearByContainers(level, storageBlock.getPos(), pos -> {
                addIgnoreTargets(storageBlock.sameType(pos, null));
            });
        }
    }

    public void tryStartIfHasPlan() {
        if (hasPlan()) {
            plan.startAny();
        }
    }
}