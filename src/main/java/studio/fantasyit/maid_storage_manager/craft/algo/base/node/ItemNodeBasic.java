package studio.fantasyit.maid_storage_manager.craft.algo.base.node;

public class ItemNodeBasic extends Node {
    public int required;
    public int crafted;
    public int count;
    public boolean isLoopedIngredient;
    public int loopInputIngredientCount;
    public int singleTimeCount;
    public boolean hasKeepIngredient;
    public int maxLack;

    public int bestRecipeStartAt;
    public boolean bestRecipeStartAtCalculating;

    public ItemNodeBasic(int id, boolean related) {
        super(id, related);
        this.count = 0;
        reinit();
    }

    public void reinit() {
        this.crafted = 0;
        this.required = 0;
        this.isLoopedIngredient = false;
        this.loopInputIngredientCount = 0;
        this.hasKeepIngredient = false;
        this.maxLack = 0;
        this.singleTimeCount = 1;
        this.bestRecipeStartAt = -1;
        this.bestRecipeStartAtCalculating = false;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public int getCurrentRemain() {
        return this.crafted + this.count - this.required;
    }
}
