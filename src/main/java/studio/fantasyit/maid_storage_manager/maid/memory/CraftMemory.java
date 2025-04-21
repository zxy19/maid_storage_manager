package studio.fantasyit.maid_storage_manager.maid.memory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

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
                            .forGetter(CraftMemory::isFinishCurrent),
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
    public boolean isFinishCurrent;
    public boolean isLastSuccess;
    private boolean startWorking;

    public CraftMemory(TargetData targetData,
                       List<CraftLayer> layers,
                       List<CraftGuideData> craftGuides,
                       List<ItemStack> remainMaterials,
                       int currentLayer,
                       boolean isFinishCurrent,
                       boolean isLastSuccess,
                       boolean startWorking
    ) {
        super(targetData);
        this.layers = new ArrayList<>(layers);
        this.craftGuides = new ArrayList<>(craftGuides);
        this.remainMaterials = new ArrayList<>(remainMaterials);
        this.currentLayer = currentLayer;
        this.isFinishCurrent = isFinishCurrent;
        this.isLastSuccess = isLastSuccess;
    }

    public CraftMemory() {
        super();
        this.layers = new ArrayList<>();
        this.craftGuides = new ArrayList<>();
        this.remainMaterials = new ArrayList<>();
        this.currentLayer = 0;
        this.isFinishCurrent = false;
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
                DebugData.getInstance().sendMessage(
                        "[REQUEST_CRAFT]Next Layer,%s", nextLayer.getCraftData().map(e -> "Normal").orElse("TreeRoot")
                );
                for (int i = 0; i < nextLayer.getItems().size(); i++) {
                    DebugData.getInstance().sendMessage(
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
                ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_WORK,
                        layer.getCraftData().map(t -> ChatTexts.fromComponent(t
                                        .getOutput()
                                        .get(0)
                                        .getHoverName()))
                                .orElse("")
                );
                layer.resetStep();
                startWorking(true);
            } else {
                failCurrent(maid, layer.getUnCollectedItems());
            }
        }
    }

    public void failCurrent(EntityMaid maid, List<ItemStack> missing) {
        CraftLayer layer = Objects.requireNonNull(this.getCurrentLayer());
        ChatTexts.send(
                maid, ChatTexts.CHAT_CRAFTING_FAIL,
                layer
                        .getCraftData()
                        .map(CraftGuideData::getOutput)
                        .map(l -> ChatTexts.fromComponent(l.get(0).getHoverName()))
                        .orElse("")
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
        if (targets != null)
            for (ItemStack target : targets) {
                RequestListItem.setMissingItem(
                        maid.getMainHandItem(),
                        target,
                        missing
                );
                RequestListItem.markDone(maid.getMainHandItem(), target);
            }
        this.setFinishCurrent(true);
        this.setLastSuccess(false);
        this.startWorking(false);
    }

    public boolean isFinishCurrent() {
        return isFinishCurrent;
    }

    public void setFinishCurrent(boolean finishCurrent) {
        isFinishCurrent = finishCurrent;
    }

    public boolean isLastSuccess() {
        return isLastSuccess;
    }

    public void setLastSuccess(boolean lastSuccess) {
        isLastSuccess = lastSuccess;
    }

    public void lastSuccess() {
        this.isLastSuccess = true;
        this.isFinishCurrent = true;
    }

    public void lastFail() {
        this.isLastSuccess = false;
        this.isFinishCurrent = true;
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
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT]initial vis %s", storageBlock);
            InvUtil.checkNearByContainers(level, storageBlock.getPos(), pos -> {
                addVisitedPos(storageBlock.sameType(pos, null));
                DebugData.getInstance().sendMessage("[REQUEST_CRAFT]initial vis %s", pos.toShortString());
            });
        }
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
}