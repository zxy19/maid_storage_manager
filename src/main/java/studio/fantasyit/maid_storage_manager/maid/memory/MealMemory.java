package studio.fantasyit.maid_storage_manager.maid.memory;

import com.github.tartaricacid.touhoulittlemaid.api.task.meal.IMaidMeal;
import com.github.tartaricacid.touhoulittlemaid.api.task.meal.MaidMealType;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.MaidMealManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;

import java.util.List;

public class MealMemory extends AbstractTargetMemory {
    public static Codec<MealMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(AbstractTargetMemory::getTargetData)
            ).apply(instance, MealMemory::new)
    );

    public MealMemory(TargetData targetData) {
        super(targetData);
    }

    public MealMemory() {
        super();
    }

    int coolDown = 0;
    boolean isEating = false;

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    public boolean isEating() {
        return isEating && this.hasTarget();
    }

    public void setEating(boolean eating) {
        isEating = eating;
    }

    public void tick() {
        if (coolDown > 0) {
            coolDown--;
        }
    }

    public boolean shouldTakeMeal(EntityMaid maid) {
        if (coolDown > 0) {
            return false;
        }
        if (!StorageManagerConfigData.get(maid).allowSeekWorkMeal())
            return false;
        String workMealTypeName = Type.WORK_MEAL.getTypeName();
        FavorabilityManager manager = maid.getFavorabilityManager();
        if (!maid.isSleeping() && maid.getTask().enableEating(maid) && manager.canAdd(workMealTypeName)) {
            return true;
        }
        return false;
    }

    public boolean isWorkMeal(EntityMaid maid, ItemStack meal) {
        List<IMaidMeal> maidMeals = MaidMealManager.getMaidMeals(MaidMealType.WORK_MEAL);
        for (IMaidMeal maidMeal : maidMeals) {
            if (maidMeal.canMaidEat(maid, meal, InteractionHand.MAIN_HAND)) {
                return true;
            }
        }
        return false;
    }
}