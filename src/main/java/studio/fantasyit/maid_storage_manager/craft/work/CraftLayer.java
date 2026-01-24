package studio.fantasyit.maid_storage_manager.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.capability.CraftBlockOccupyDataProvider;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//每一个请求寻找层。初始层的craftData为空
public class CraftLayer {
    public static Codec<CraftLayer> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftGuideData.CODEC.optionalFieldOf("craft").forGetter(CraftLayer::getCraftData),
                    CraftGuideData.CODEC.listOf().fieldOf("usableCraft").forGetter(CraftLayer::getUsableCraftData),
                    ItemStack.CODEC.listOf().fieldOf("item").forGetter(CraftLayer::getItems),
                    Codec.INT.listOf().fieldOf("collectedCounts").forGetter(CraftLayer::getCollectedCounts),
                    Codec.INT.fieldOf("count").forGetter(CraftLayer::getCount),
                    Codec.INT.fieldOf("doneCount").forGetter(CraftLayer::getDoneCount),
                    Codec.INT.fieldOf("tryTick").forGetter(CraftLayer::getTryTick),
                    Codec.INT.listOf().fieldOf("currentStepPlacedCounts").forGetter(CraftLayer::getCurrentStepCounts),
                    Codec.INT.fieldOf("step").forGetter(CraftLayer::getStep),
                    CompoundTag.CODEC.orElseGet(CompoundTag::new).fieldOf("env").forGetter(CraftLayer::getEnv),
                    Codec.BOOL.fieldOf("placeBefore").forGetter(CraftLayer::shouldPlaceBefore)
            ).apply(instance, CraftLayer::new)
    );


    protected int tryTick;
    protected CompoundTag env;
    protected CraftGuideData craftData;
    protected List<CraftGuideStepData> steps;
    protected List<CraftGuideData> usableCraftData;

    protected List<ItemStack> items;
    protected List<Integer> collectedCounts;
    protected List<Integer> currentStepCounts;

    protected int count;
    protected int doneCount;
    protected int step;
    private boolean placeBefore;

    public CraftLayer(Optional<CraftGuideData> craftData,
                      List<CraftGuideData> usableCraftData,
                      List<ItemStack> items,
                      List<Integer> collectedCounts,
                      Integer count,
                      Integer doneCount,
                      Integer tryTick,
                      List<Integer> currentStepCounts,
                      int step,
                      CompoundTag env,
                      boolean placeBefore
    ) {
        this.usableCraftData = usableCraftData;
        this.craftData = craftData.orElse(null);
        this.steps = new ArrayList<>(craftData.map(CraftGuideData::getTransformedSteps).orElse(new ArrayList<>()));
        this.items = new ArrayList<>(items);
        this.collectedCounts = new ArrayList<>(collectedCounts);
        this.count = count;
        this.doneCount = doneCount;
        this.currentStepCounts = new ArrayList<>(currentStepCounts);
        this.step = step;
        this.tryTick = tryTick;
        this.env = env;
        this.placeBefore = placeBefore;
    }

    public CraftLayer(Optional<CraftGuideData> craftData, List<ItemStack> items, Integer count) {
        this.craftData = craftData.orElse(null);
        this.steps = new ArrayList<>(craftData.map(CraftGuideData::getTransformedSteps).orElse(new ArrayList<>()));
        this.usableCraftData = List.of();
        this.items = new ArrayList<>(items);
        this.tryTick = 0;
        this.collectedCounts = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            this.collectedCounts.add(0);
        }
        this.count = count;
        this.doneCount = 0;
        this.step = 0;
        this.currentStepCounts = new ArrayList<>();
        this.env = new CompoundTag();
    }

    public CraftLayer(Optional<CraftGuideData> craftData, List<CraftGuideData> usableCraftData, List<ItemStack> items, int count) {
        this(craftData, items, count);
        setUsableCraftData(usableCraftData);
    }


    public List<CraftGuideData> getUsableCraftData() {
        return usableCraftData;
    }

    public void setUsableCraftData(List<CraftGuideData> usableCraftData) {
        this.usableCraftData = usableCraftData;
    }

    public boolean switchToNonOccupied(ServerLevel level, EntityMaid maid, int layerIndex, CraftBlockOccupyDataProvider.CraftBlockOccupy craftBlockOccupy) {
        //第一轮开始前允许更改目标
        if (step != 0) return false;
        for (CraftGuideData usable : getUsableCraftData()) {
            List<CraftGuideStepData> newSteps = usable.getTransformedSteps();
            if (newSteps
                    .stream().noneMatch(stepData -> craftBlockOccupy.isOccupiedByNonCurrent(
                            maid,
                            stepData.getStorage().pos,
                            layerIndex
                    ))) {
                craftData = usable;
                steps = new ArrayList<>(newSteps);
                return true;
            }
        }
        return false;
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

    public void setItems(List<ItemStack> items) {
        this.items = items;
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

    public void clearNonFirstStep() {
        while (steps.size() > 1)
            steps.remove(1);
        step = 0;
    }

    /**
     * 尝试记忆合成物品，返回应该拿取多少该种物品。
     */
    public ItemStack memorizeItem(ItemStack itemStack, int maxStore) {
        for (int i = 0; i < items.size(); i++) {
            if (ItemStackUtil.isSameInCrafting(items.get(i), itemStack)) {
                int restNeed = items.get(i).getCount() - collectedCounts.get(i);
                int toTake = Math.min(Math.min(restNeed, maxStore), itemStack.getCount());
                collectedCounts.set(i, collectedCounts.get(i) + toTake);
                return itemStack.copyWithCount(toTake);
            }
        }
        return ItemStack.EMPTY;
    }

    public int memorizeItemSimulate(ItemStack itemStack) {
        for (int i = 0; i < items.size(); i++) {
            if (ItemStackUtil.isSameInCrafting(items.get(i), itemStack)) {
                int restNeed = items.get(i).getCount() - collectedCounts.get(i);
                return Math.min(restNeed, itemStack.getCount());
            }
        }
        return 0;
    }


    /**
     * 下一个合成阶段。行为如下描述
     * 如果当前已经在合成，那么尝试进行下一步骤的合成。
     * 否则，尝试添加doneCount，如果doneCount达到count，则完成合成，不进行任何操作。
     * 否则，重置step。
     */
    public void nextStep() {
        currentStepCounts = new ArrayList<>();
        tryTick = 0;
        env = new CompoundTag();
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
        if (step >= steps.size()) return null;
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

    public List<ItemStack> getToPrefetchItems(List<ItemStack> original){
        List<ItemStack> toPrefetch = new ArrayList<>();
        for (ItemStack itemStack : original) {
            for(int i = 0; i < items.size(); i++){
                if(ItemStackUtil.isSameInCrafting(itemStack, items.get(i)) && itemStack.getCount() > collectedCounts.get(i)){
                    toPrefetch.add(itemStack.copyWithCount(itemStack.getCount() - collectedCounts.get(i)));
                }
            }
        }
        return toPrefetch;
    }

    public void resetStep() {
        this.step = 0;
        this.tryTick = 0;
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

    public void setCount(int i) {
        this.count = i;
    }

    public int addAndGetTryTick() {
        return ++tryTick;
    }

    public int getTryTick() {
        return tryTick;
    }

    public void setTryTick(int i) {
        tryTick = i;
    }

    public CompoundTag getEnv() {
        return env;
    }

    public void setEnv(CompoundTag env) {
        this.env = env;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CraftLayer craftLayer &&
                craftLayer.getCount().equals(this.count) &&
                Objects.equals(craftLayer.getCraftData().orElse(null), this.craftData);
    }

    public CraftLayer copyWithNoState() {
        return new CraftLayer(
                Optional.ofNullable(this.craftData),
                this.usableCraftData,
                this.items,
                this.count
        );
    }

    public void setPlaceBefore() {
        this.placeBefore = true;
    }

    public void clearPlaceBefore() {
        this.placeBefore = false;
    }

    public boolean shouldPlaceBefore() {
        return placeBefore;
    }

    public int getExtraSlotConsume() {
        if (craftData != null)
            return craftData.getExtraSlotConsume();
        return 0;
    }
}