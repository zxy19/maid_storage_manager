package studio.fantasyit.maid_storage_manager.craft.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//每一个请求寻找层。初始层的craftData为空
public class CraftLayer {
    public static Codec<CraftLayer> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftGuideData.CODEC.optionalFieldOf("craft").forGetter(CraftLayer::getCraftData),
                    ItemStack.CODEC.listOf().fieldOf("item").forGetter(CraftLayer::getItems),
                    Codec.INT.listOf().fieldOf("collectedCounts").forGetter(CraftLayer::getCollectedCounts),
                    Codec.INT.fieldOf("count").forGetter(CraftLayer::getCount),
                    Codec.INT.fieldOf("doneCount").forGetter(CraftLayer::getDoneCount),
                    Codec.INT.listOf().fieldOf("currentStepPlacedCounts").forGetter(CraftLayer::getCurrentStepCounts),
                    Codec.INT.fieldOf("step").forGetter(CraftLayer::getStep)
            ).apply(instance, CraftLayer::new)
    );
    protected CraftGuideData craftData;
    protected List<CraftGuideStepData> steps;

    protected List<ItemStack> items;
    protected List<Integer> collectedCounts;
    protected List<Integer> currentStepCounts;

    protected int count;
    protected int doneCount;
    protected int step;

    public CraftLayer(Optional<CraftGuideData> craftData,
                      List<ItemStack> items,
                      List<Integer> collectedCounts,
                      Integer count,
                      Integer doneCount,
                      List<Integer> currentStepCounts,
                      int step) {
        this.craftData = craftData.orElse(null);
        this.steps = new ArrayList<>(craftData.map(CraftGuideData::getTransformedSteps).orElse(new ArrayList<>()));
        this.items = new ArrayList<>(items);
        this.collectedCounts = new ArrayList<>(collectedCounts);
        this.count = count;
        this.doneCount = doneCount;
        this.currentStepCounts = new ArrayList<>(currentStepCounts);
        this.step = step;
    }

    public CraftLayer(Optional<CraftGuideData> craftData, List<ItemStack> items, Integer count) {
        this.craftData = craftData.orElse(null);
        this.steps = new ArrayList<>(craftData.map(CraftGuideData::getTransformedSteps).orElse(new ArrayList<>()));
        this.items = new ArrayList<>(items);
        this.collectedCounts = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            this.collectedCounts.add(0);
        }
        this.count = count;
        this.doneCount = 0;
        this.step = 0;
        this.currentStepCounts = new ArrayList<>();
    }

    public Integer getCount() {
        return count;
    }

    public Integer getDoneCount() {
        return doneCount;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<Integer> getCollectedCounts() {
        return collectedCounts;
    }

    public Optional<CraftGuideData> getCraftData() {
        return Optional.ofNullable(craftData);
    }

    public void addStep(CraftGuideStepData stepData) {
        steps.add(stepData);
    }

    /**
     * 尝试记忆合成物品，返回应该拿取多少该种物品。
     */
    public ItemStack memorizeItem(ItemStack itemStack, int maxStore) {
        for (int i = 0; i < items.size(); i++) {
            if (ItemStack.isSameItem(items.get(i), itemStack)) {
                int restNeed = items.get(i).getCount() - collectedCounts.get(i);
                int toTake = Math.min(Math.min(restNeed, maxStore), itemStack.getCount());
                collectedCounts.set(i, collectedCounts.get(i) + toTake);
                return itemStack.copyWithCount(toTake);
            }
        }
        return ItemStack.EMPTY;
    }


    /**
     * 下一个合成阶段。行为如下描述
     * 如果当前已经在合成，那么尝试进行下一步骤的合成。
     * 否则，尝试添加doneCount，如果doneCount达到count，则完成合成，不进行任何操作。
     * 否则，重置step。
     */
    public void nextStep() {
        currentStepCounts = new ArrayList<>();
        if (doneCount >= count) return;
        step++;
        if (step == steps.size()) {
            doneCount++;
            step = 0;
        }
    }

    public int getStep() {
        return step;
    }

    public boolean isDone() {
        return doneCount >= count;
    }

    public CraftGuideStepData getStepData() {
        if (craftData == null) return null;
        return steps.get(step);
    }

    public AbstractCraftActionContext startStep(EntityMaid maid) {
        if (craftData == null) return null;
        return CraftManager.getInstance().startCurrentStep(this, maid);
    }

    public boolean hasCollectedAll() {
        for (int i = 0; i < collectedCounts.size(); i++) {
            if (collectedCounts.get(i) < items.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }

    public List<ItemStack> getUnCollectedItems() {
        List<ItemStack> unCollectedItems = new ArrayList<>();
        for (int i = 0; i < collectedCounts.size(); i++) {
            if (collectedCounts.get(i) < items.get(i).getCount()) {
                unCollectedItems.add(items.get(i).copyWithCount(items.get(i).getCount() - collectedCounts.get(i)));
            }
        }
        return unCollectedItems;
    }

    public void resetStep() {
        this.step = 0;
    }

    public List<Integer> getCurrentStepCounts() {
        return currentStepCounts;
    }

    public int getCurrentStepCount(int id) {
        if (currentStepCounts.size() <= id)
            return 0;
        else
            return currentStepCounts.get(id);
    }

    public void addCurrentStepPlacedCounts(int id, int count) {
        while (currentStepCounts.size() <= id)
            currentStepCounts.add(0);
        currentStepCounts.set(id, currentStepCounts.get(id) + count);
    }

    public int getTotalStep() {
        return steps.size();
    }
}