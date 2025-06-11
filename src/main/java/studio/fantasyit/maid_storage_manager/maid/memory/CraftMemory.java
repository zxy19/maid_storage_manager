package studio.fantasyit.maid_storage_manager.maid.memory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CraftMemory extends AbstractTargetMemory {
    public static Codec<CraftMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(AbstractTargetMemory::getTargetData),
                    CraftLayer.CODEC
                            .listOf()
                            .fieldOf("craft")
                            .forGetter(CraftMemory::getCraftLayer),
                    CraftGuideData.CODEC
                            .listOf()
                            .fieldOf("craftGuides")
                            .forGetter(CraftMemory::getCraftGuides),
                    ItemStack.CODEC.listOf().fieldOf("remainMaterials")
                            .forGetter(CraftMemory::getRemainMaterials),
                    Codec.INT.fieldOf("currentLayer")
                            .forGetter(CraftMemory::getCurrentLayerIndex),
                    Codec.BOOL.fieldOf("isFinishCurrent")
                            .forGetter(CraftMemory::isGoPlacingBeforeCraft),
                    Codec.BOOL.fieldOf("isLastSuccess")
                            .forGetter(CraftMemory::isLastSuccess),
                    Codec.BOOL.fieldOf("startWorking")
                            .forGetter(CraftMemory::hasStartWorking)
            ).apply(instance, CraftMemory::new)
    );

    public List<CraftLayer> layers;
    public List<CraftGuideData> craftGuides;
    public List<ItemStack> remainMaterials;
    public int currentLayer;
    public boolean goPlacingBeforeCraft;
    public boolean isLastSuccess;
    private boolean startWorking;
    private int pathFindingFailCount = 0;

    public CraftMemory(TargetData targetData,
                       List<CraftLayer> layers,
                       List<CraftGuideData> craftGuides,
                       List<ItemStack> remainMaterials,
                       int currentLayer,
                       boolean goPlacingBeforeCraft,
                       boolean isLastSuccess,
                       boolean startWorking
    ) {
        super(targetData);
        this.layers = new ArrayList<>(layers);
        this.craftGuides = new ArrayList<>(craftGuides);
        this.remainMaterials = new ArrayList<>(remainMaterials);
        this.currentLayer = currentLayer;
        this.goPlacingBeforeCraft = goPlacingBeforeCraft;
        this.isLastSuccess = isLastSuccess;
        this.startWorking = startWorking;
    }

    public CraftMemory() {
        super();
        this.layers = new ArrayList<>();
        this.craftGuides = new ArrayList<>();
        this.remainMaterials = new ArrayList<>();
        this.currentLayer = 0;
        this.goPlacingBeforeCraft = false;
        this.isLastSuccess = false;
        this.startWorking = false;
    }

    public List<CraftLayer> getCraftLayer() {
        return layers;
    }

    public void addLayer(CraftLayer layer) {
        this.layers.add(layer);
    }

    public void clearLayers() {
        this.layers.clear();
        this.remainMaterials.clear();
        this.currentLayer = 0;
    }

    public int getCurrentLayerIndex() {
        return this.currentLayer;
    }

    public @Nullable CraftLayer getCurrentLayer() {
        return this.layers.get(this.currentLayer);
    }

    public void nextLayer() {
        @Nullable CraftLayer layer = getCurrentLayer();
        this.currentLayer++;
        if (this.currentLayer >= this.layers.size()) {
            this.currentLayer = this.layers.size();
        }
        this.isSwappingHandWhenCrafting = false;
        this.startWorking(false);
        if (layer != null && hasCurrent() && layer.getCraftData().isPresent()) {
            @Nullable CraftLayer nextLayer = getCurrentLayer();
            if (nextLayer != null) {
                layer.getCraftData().ifPresent(data -> {
                    data.getOutput().forEach(itemStack -> {
                        if (itemStack.isEmpty()) return;
                        this.remainMaterials.add(itemStack.copyWithCount(itemStack.getCount() * layer.getCount()));
                    });
                });
                for (int i = 0; i < this.remainMaterials.size(); i++) {
                    ItemStack toTake = nextLayer.memorizeItem(remainMaterials.get(i), remainMaterials.get(i).getCount());
                    remainMaterials.get(i).shrink(toTake.getCount());
                    if (remainMaterials.get(i).isEmpty()) {
                        remainMaterials.remove(i);
                        i--;
                    }
                }
                //来到树根了，清除剩余的材料（因为女仆会尝试放置到附近）
                if (nextLayer.getCraftData().isEmpty()) {
                    remainMaterials.clear();
                }
                DebugData.sendDebug(
                        "[REQUEST_CRAFT]Next Layer,%s", nextLayer.getCraftData().map(e -> "Normal").orElse("TreeRoot")
                );
                for (int i = 0; i < nextLayer.getItems().size(); i++) {
                    DebugData.sendDebug(
                            "[REQUEST_CRAFT] + %s [%d/%d]",
                            nextLayer.getItems().get(i).getDisplayName().getString(),
                            nextLayer.getCollectedCounts().get(i),
                            nextLayer.getItems().get(i).getCount()
                    );
                }
            }
        }
    }

    public boolean hasTasks() {
        return !this.layers.isEmpty();
    }

    public boolean hasCurrent() {
        return this.currentLayer < this.layers.size();
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


    public void finishGathering(EntityMaid maid) {
        if (this.hasCurrent()) {
            CraftLayer layer = Objects.requireNonNull(this.getCurrentLayer());
            if (layer.hasCollectedAll()) {
                //收集完了再次检查满足开始条件。如果否则开始前再次进行补齐
                if (!checkInputInbackpack(maid)) return;
                showCraftingProgress(maid);
                ChatTexts.send(maid,
                        Component.translatable(
                                ChatTexts.CHAT_CRAFT_WORK,
                                layer.getCraftData().map(t -> t
                                                .getOutput()
                                                .get(0)
                                                .getHoverName())
                                        .orElse(Component.empty())
                        )
                );
                //按说没啥用？layer.resetStep();
                startWorking(true);
            } else {
                //如果不是最终步骤，而且没有收集成功，那么意味着记忆存在问题，重置记忆，并再次计算合成树
                if (getCurrentLayer().getCraftData().isPresent())
                    markMissingSchedulePlaceAndRecalc(maid);
                else
                    failCurrent(maid, getCurrentLayer().getUnCollectedItems());
            }
        }
    }

    private void markMissingSchedulePlaceAndRecalc(EntityMaid maid) {
        CraftLayer currentLayer1 = this.getCurrentLayer();
        List<ItemStack> unCollectedItems = currentLayer1.getUnCollectedItems();
        ViewedInventoryMemory viewedInventoryMemory = MemoryUtil.getViewedInventory(maid);
        unCollectedItems.forEach(itemStack -> viewedInventoryMemory.removeItemFromAllTargets(itemStack, i -> ItemStackUtil.isSameTagInCrafting(i, itemStack)));
        setGoPlacingBeforeCraft(true);
        ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_RESCHEDULE);
        ChatTexts.removeSecondary(maid);
        clearLayers();
    }

    public void failCurrent(EntityMaid maid, List<ItemStack> missing) {
        CraftLayer layer = Objects.requireNonNull(this.getCurrentLayer());
        ChatTexts.send(
                maid,
                Component.translatable(
                        ChatTexts.CHAT_CRAFTING_FAIL,
                        layer
                                .getCraftData()
                                .map(CraftGuideData::getOutput)
                                .map(l -> l.get(0).getHoverName())
                                .orElse(Component.empty())
                )
        );
        List<ItemStack> targets = null;
        //跳过合成直到一个根任务（表示树完全失败）
        for (; currentLayer < layers.size(); currentLayer++) {
            CraftLayer craftLayer = layers.get(currentLayer);
            if (craftLayer.getCraftData().isEmpty()) {
                targets = craftLayer.getItems();
                currentLayer++;
                break;
            }
        }
        if (targets != null) {
            CombinedInvWrapper inv = maid.getAvailableInv(true);
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                RequestListItem.updateCollectedItem(maid.getMainHandItem(), stack, stack.getCount());
            }
            for (ItemStack target : targets) {
                RequestListItem.setMissingItem(
                        maid.getMainHandItem(),
                        target,
                        missing
                );
                RequestListItem.markDone(maid.getMainHandItem(), target);
            }
            MemoryUtil.getRequestProgress(maid).setReturn();
        }
        this.setGoPlacingBeforeCraft(true);
        this.setLastSuccess(false);
        this.startWorking(false);
        showCraftingProgress(maid);
    }


    public boolean isGoPlacingBeforeCraft() {
        return goPlacingBeforeCraft;
    }

    public void setGoPlacingBeforeCraft(boolean goPlacingBeforeCraft) {
        this.goPlacingBeforeCraft = goPlacingBeforeCraft;
    }

    public boolean isLastSuccess() {
        return isLastSuccess;
    }

    public void setLastSuccess(boolean lastSuccess) {
        isLastSuccess = lastSuccess;
    }

    public void lastSuccess() {
        this.isLastSuccess = true;
        this.goPlacingBeforeCraft = true;
    }

    public boolean hasStartWorking() {
        return this.startWorking;
    }

    public void startWorking(boolean isStartWorking) {
        this.startWorking = isStartWorking;
    }

    public void resetAndMarkVisForRequest(ServerLevel level, EntityMaid maid) {
        this.resetVisitedPos();
        Target storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock != null) {
            addVisitedPos(storageBlock);
            DebugData.sendDebug("[REQUEST_CRAFT]initial vis %s", storageBlock);
            InvUtil.checkNearByContainers(level, storageBlock.getPos(), pos -> {
                addVisitedPos(storageBlock.sameType(pos, null));
                DebugData.sendDebug("[REQUEST_CRAFT]initial vis %s", pos.toShortString());
            });
        }
    }

    public boolean checkInputInbackpack(EntityMaid maid) {
        if (!hasCurrent()) return true;
        CraftLayer layer = Objects.requireNonNull(getCurrentLayer());
        if (layer.getStep() != 0) return true;
        CraftGuideData craftData = layer.getCraftData().orElse(null);
        if (craftData == null) return true;
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack itemStack : craftData.getInput()) {
            if (itemStack.isEmpty()) continue;
            ItemStackUtil.addToList(inputs, itemStack, false);
        }
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (ItemStack itemStack : inputs) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack item = inv.getStackInSlot(i);
                if (ItemStackUtil.isSameInCrafting(item, itemStack)) {
                    itemStack.shrink(Math.min(itemStack.getCount(), item.getCount()));
                    if (itemStack.isEmpty()) break;
                }
            }
        }
        if (!inputs.stream().allMatch(ItemStack::isEmpty)) {
            for (ItemStack itemStack : inputs) {
                if (!itemStack.isEmpty()) {
                    for (int i = 0; i < layer.getItems().size(); i++) {
                        if (ItemStackUtil.isSameInCrafting(layer.getItems().get(i), itemStack)) {
                            layer.getItems().get(i).grow(itemStack.getCount());
                            break;
                        }
                    }
                }
            }
            startWorking(false);
            return false;
        }
        return true;
    }

    public List<ItemStack> getRemainMaterials() {
        return remainMaterials;
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

    public void showCraftingProgress(EntityMaid maid) {
        ChatTexts.showSecondary(maid,
                Component.translatable(
                        ChatTexts.CHAT_SECONDARY_CRAFTING,
                        this.currentLayer,
                        this.layers.size(),
                        hasStartWorking() ? Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_WORK) :
                                Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_GATHER)
                ),
                ((double) this.currentLayer * 2 - (hasStartWorking() ? 0 : 1)) / ((double) this.layers.size() * 2)
        );
    }
}