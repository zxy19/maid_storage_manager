package studio.fantasyit.maid_storage_manager.craft.generator.algo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.IDebugContextSetter;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.*;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.RecipeUtil;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.*;
import java.util.function.Function;

public class GeneratorGraph implements ICachableGeneratorGraph, IDebugContextSetter {
    private CraftingDebugContext debugContext = CraftingDebugContext.Dummy.INSTANCE;

    public int getNodeCount() {
        return nodes.size();
    }

    protected record AddRecipeData(ResourceLocation id,
                                   List<Ingredient> ingredients,
                                   List<Integer> ingredientCounts,
                                   List<ItemStack> output,
                                   Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier,
                                   ResourceLocation currentType, boolean oneTime) {
    }

    protected final int MAX_PRE_TICK = 50;
    public final List<CraftGuideData> craftGuides = new ArrayList<>();

    private final RegistryAccess registryAccess;
    public int pushedSteps = 0;
    public int processedSteps = 0;

    @Override
    public void setDebugContext(CraftingDebugContext context) {
        debugContext = context;
    }

    List<Node> nodes;
    HashMap<ResourceLocation, List<ItemNode>> itemNodeMap = new HashMap<>();
    HashMap<ResourceLocation, CraftNode> craftNodeMap = new HashMap<>();
    HashMap<UUID, IngredientNode> cachedIngredients = new HashMap<>();
    Set<ResourceLocation> notToAddRecipe = new HashSet<>();
    Set<ResourceLocation> notToAddType = new HashSet<>();

    @Override
    public Node getNode(int a) {
        return nodes.get(a);
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }


    public GeneratorGraph(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
        this.nodes = new ArrayList<>();
        IngredientNode ingredientNode = addOrGetIngredientNode(Ingredient.EMPTY);
        queue.add(ingredientNode);
        ingredientNode.related = true;
    }

    public void setItems(List<ItemStack> items, List<ItemStack> required) {
        for (ItemStack item : items) {
            ItemNode itemNode = getItemNodeOrCreate(item, false);
            queue.add(itemNode);
            if (item.is(ItemRegistry.CRAFT_GUIDE.get())) {
                CraftGuideData craftGuideData = item.get(DataComponentRegistry.CRAFT_GUIDE_DATA);
                if (craftGuideData != null)
                    craftGuideData.getOutput().forEach(itemStack -> queue.add(getItemNodeOrCreate(itemStack, false)));
            }
        }
        for (ItemStack item : required) {
            ItemNode itemNode = getItemNodeOrCreate(item, false);
            reversedQueue.add(itemNode);
        }
    }

    /// /////////////////物品节点处理/////////////////////////
    public @NotNull ItemNode getItemNodeOrCreate(ItemStack itemStack, boolean available) {
        ItemNode tmp = getItemNode(itemStack);
        if (tmp == null)
            tmp = addItemNode(itemStack, available);
        return tmp;
    }

    @Override
    public void addToQueue(Node node) {
        addToQueueIfNotIn(node);
    }

    @Override
    public void addCraftGuide(CraftGuideData craftGuideData) {
        craftGuides.add(craftGuideData);
    }

    public ItemNode getItemNode(ItemStack itemStack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (itemNodeMap.containsKey(id)) {
            for (Node node : itemNodeMap.get(id)) {
                if (node instanceof ItemNode in) {
                    if (ItemStackUtil.isSameInCrafting(itemStack, in.itemStack)) {
                        return in;
                    }
                }
            }
        }
        return null;
    }

    public ItemNode addItemNode(ItemStack itemStack, boolean available) {
        ItemNode itemNode = new ItemNode(nodes.size(), available, itemStack);
        nodes.add(itemNode);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (!itemNodeMap.containsKey(id))
            itemNodeMap.put(id, new ArrayList<>());
        itemNodeMap.get(id).add(itemNode);
        return itemNode;
    }

    /// ///////////////原料节点处理///////
    public IngredientNode addOrGetIngredientNode(Ingredient ingredient) {
        for (Node node : nodes) {
            if (node instanceof IngredientNode in) {
                if (in.isEqualTo(ingredient)) {
                    return in;
                }
            }
        }

        return addIngredientNode(ingredient);
    }

    public IngredientNode addOrGetCahcedIngredientNode(Ingredient ingredient, UUID uuid) {
        if (ingredient.isEmpty()) {
            return addOrGetIngredientNode(ingredient);
        }
        if (cachedIngredients.containsKey(uuid)) {
            return cachedIngredients.get(uuid);
        } else {
            IngredientNode ingredientNode = addIngredientNode(ingredient);
            cachedIngredients.put(uuid, ingredientNode);
            return ingredientNode;
        }
    }

    private IngredientNode addIngredientNode(Ingredient ingredient) {
        List<ItemNode> possibleItems = Arrays
                .stream(ingredient.getItems())
                .map(t -> this.getItemNodeOrCreate(t, false))
                .toList();
        IngredientNode ingredientNode = new IngredientNode(nodes.size(), possibleItems);
        nodes.add(ingredientNode);

        for (ItemNode itemNode : possibleItems) {
            itemNode.addEdge(ingredientNode, 1);
        }
        return ingredientNode;
    }


    /// ////////////配方节点处理/////////////

    protected ResourceLocation currentType;
    protected boolean oneTime = false;

    public void setCurrentGeneratorType(IAutoCraftGuideGenerator generator) {
        this.currentType = generator.getType();
        this.oneTime = generator.canCacheGraph();
    }

    public void setCurrentGeneratorType(ResourceLocation internalType, boolean b) {
        this.currentType = internalType;
        this.oneTime = b;
    }


    public void addRecipe(RecipeHolder<? extends Recipe<?>> holder, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        Recipe<?> recipe = holder.value();
        List<Integer> ingredientCounts = recipe.getIngredients()
                .stream()
                .map(t -> Arrays.stream(t.getItems()).findFirst().map(ItemStack::getCount).orElse(1))
                .toList();
        addRecipe(
                holder.id(),
                recipe.getIngredients(),
                ingredientCounts,
                recipe.getResultItem(registryAccess),
                craftGuideSupplier
        );
        pushedSteps++;
    }

    public void addRecipeWrapId(RecipeHolder<? extends Recipe<?>> holder, ResourceLocation generator, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        Recipe<?> recipe = holder.value();
        List<Integer> ingredientCounts = recipe.getIngredients()
                .stream()
                .map(t -> Arrays.stream(t.getItems()).findFirst().map(ItemStack::getCount).orElse(1))
                .toList();
        addRecipe(
                RecipeUtil.wrapLocation(generator, holder.id()),
                recipe.getIngredients(),
                ingredientCounts,
                recipe.getResultItem(registryAccess),
                craftGuideSupplier
        );
        pushedSteps++;
    }


    public void addRecipe(ResourceLocation id, List<Ingredient> ingredients, List<Integer> ingredientCounts, ItemStack output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        addRecipe(id, ingredients, ingredientCounts, List.of(output), craftGuideSupplier);
    }

    public void addRecipe(ResourceLocation id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        addRecipeQueue.add(new AddRecipeData(
                        id,
                        ingredients,
                        ingredientCounts,
                        output,
                        craftGuideSupplier,
                        currentType,
                        oneTime
                )
        );
        pushedSteps++;
    }

    public void addSpecialCraftNode(Function<Integer, SpecialCraftNode> idToNodeBuilder) {
        specialCraftNodeBuilder.add(idToNodeBuilder);
        pushedSteps++;
    }

    public void blockType(ResourceLocation type) {
        notToAddType.add(type);
    }

    public void blockRecipe(ResourceLocation id) {
        notToAddRecipe.add(id);
    }

    public void removeBlockedRecipe(ResourceLocation id) {
        notToAddRecipe.remove(id);
    }

    public void removeBlockedType(ResourceLocation type) {
        notToAddType.remove(type);
    }

    @Override
    public List<CraftGuideData> getCraftGuides() {
        return craftGuides;
    }

    @Override
    public int getProcessedSteps() {
        return processedSteps;
    }

    @Override
    public int getPushedSteps() {
        return pushedSteps;
    }


    protected int _addRecipe(ResourceLocation id,
                             List<Ingredient> ingredients,
                             List<Integer> ingredientCounts,
                             List<ItemStack> output,
                             Function<List<ItemStack>,
                                     @Nullable CraftGuideData> craftGuideSupplier,
                             ResourceLocation type,
                             boolean isOneTime
    ) {
        if (notToAddRecipe.contains(id) || notToAddType.contains(type)) {
            debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR_RECIPE, "recipe blocked %s", id);
            return 1;
        }
        debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR_RECIPE, "recipe add %s", id);
        processedSteps++;
        int affectFactor = ingredients.size() + 1;
        if (RecipeIngredientCache.isCached(id)) {
            if (RecipeIngredientCache.addCahcedRecipeToGraph(this, id, ingredients, ingredientCounts, output, craftGuideSupplier, type, isOneTime))
                return affectFactor;
        }
        affectFactor += (ingredients.size() + 1) + RecipeIngredientCache.getUncachedRecipeIngredient(id, ingredients, this) * 5;
        RecipeIngredientCache.addRecipeCache(id, ingredients);
        RecipeIngredientCache.addCahcedRecipeToGraph(this, id, ingredients, ingredientCounts, output, craftGuideSupplier, type, isOneTime);

        return affectFactor;
    }

    public void addRecipeWithIngredients(ResourceLocation id,
                                         List<Ingredient> ingredients,
                                         List<Integer> ingredientCounts,
                                         List<ItemStack> outputs,
                                         List<IngredientNode> ingredientNodes,
                                         Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier, ResourceLocation type, boolean isOneTime) {
        CraftNode craftNode = getOrCreateCraftNode(id, ingredientCounts, ingredientNodes, craftGuideSupplier, type, isOneTime);

        for (IngredientNode ingredientNode : craftNode.independentIngredients) {
            ingredientNode.addEdge(craftNode, 1);
        }

        outputs.forEach(output -> craftNode.addEdge(getItemNodeOrCreate(output, false), 1));
    }

    private @NotNull CraftNode getOrCreateCraftNode(ResourceLocation id, List<Integer> ingredientCounts, List<IngredientNode> ingredientNodes, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier, ResourceLocation type, boolean isOneTime) {
        if (craftNodeMap.containsKey(id)) {
            CraftNode originalNode = craftNodeMap.get(id);
            if (originalNode.isRemoved) {
                originalNode.removeAllEdges(this);
                CraftNode craftNode = new CraftNode(id, originalNode.id, craftGuideSupplier, ingredientNodes, ingredientCounts, type, isOneTime);
                nodes.set(originalNode.id, craftNode);
                craftNodeMap.put(id, craftNode);
                return craftNode;
            } else {
                originalNode.removeAllEdges(this);
                originalNode.setNonRemoved();
                originalNode.addCraftGuideSupplier(craftGuideSupplier);
                return originalNode;
            }
        }
        CraftNode craftNode = new CraftNode(id, nodes.size(), craftGuideSupplier, ingredientNodes, ingredientCounts, type, isOneTime);
        nodes.add(craftNode);
        craftNodeMap.put(id, craftNode);
        return craftNode;
    }

    public boolean hasCachedIngredientNode(UUID ingredient) {
        return cachedIngredients.containsKey(ingredient);
    }


    /// //////////队列逻辑
    Queue<Node> queue = new LinkedList<>();
    Queue<Node> reversedQueue = new LinkedList<>();
    Queue<AddRecipeData> addRecipeQueue = new LinkedList<>();
    Queue<Function<Integer, SpecialCraftNode>> specialCraftNodeBuilder = new LinkedList<>();

    public boolean process() {
        if (!specialCraftNodeBuilder.isEmpty()) {
            processAddSpecial();
            return false;
        } else if (!addRecipeQueue.isEmpty()) {
            processAddRecipe();
            return false;
        } else if (!reversedQueue.isEmpty()) {
            processReversed();
            return false;
        }
        return processData();
    }

    private void processAddSpecial() {
        if (specialCraftNodeBuilder.isEmpty()) return;
        Function<Integer, SpecialCraftNode> builder = specialCraftNodeBuilder.poll();
        SpecialCraftNode specialNode = builder.apply(nodes.size());
        nodes.add(specialNode);
        debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR, "add special node %s", specialNode);
        specialNode.buildGraph(this);
    }

    public void processAddRecipe() {
        int c = 0;
        while (!addRecipeQueue.isEmpty() && c++ < MAX_PRE_TICK * 20) {
            AddRecipeData addRecipeData = addRecipeQueue.poll();
            c += _addRecipe(addRecipeData.id,
                    addRecipeData.ingredients,
                    addRecipeData.ingredientCounts,
                    addRecipeData.output,
                    addRecipeData.craftGuideSupplier,
                    addRecipeData.currentType,
                    addRecipeData.oneTime
            );
        }
    }

    public boolean processData() {
        int c = 0;
        while (!queue.isEmpty()) {
            if (c++ > MAX_PRE_TICK)
                return false;
            Node node = queue.poll();
            processedSteps++;
            node.inqueue = false;
            if (!node.related) continue;
            if (node instanceof ItemNode itemNode && !itemNode.isAvailable) {
                itemNode.isAvailable = true;
                itemNode.forEachEdge((toId, weight) -> {
                    Node to = getNode(toId);
                    addToQueueIfNotIn(to);
                });
            } else if (node instanceof IngredientNode ingredientNode) {
                ingredientNode.anyAvailable = true;
                ingredientNode.forEachEdge((toId, weight) -> {
                    Node to = getNode(toId);
                    if (to instanceof CraftNode craftNode &&
                            (Config.generatePartial || craftNode.independentIngredients.stream().allMatch(t -> t.anyAvailable))) {
                        addToQueueIfNotIn(craftNode);
                    }
                });
            } else if (node instanceof CraftNode craftNode) {
                addNewCraft(craftNode);
                c++;
                craftNode.forEachEdge((toId, weight) -> {
                    Node to = getNode(toId);
                    addToQueueIfNotIn(to);
                });
            } else if (node instanceof SpecialCraftNode specialCraftNode) {
                specialCraftNode.generate(this);
                specialCraftNode.addNextNodes(this);
            }
        }
        return true;
    }

    public boolean processReversed() {
        int c = 0;
        while (!reversedQueue.isEmpty()) {
            if (c++ >= MAX_PRE_TICK * 10)
                return false;
            Node node = reversedQueue.poll();
            node.related = true;
            debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR, "%s marked related", node);
            processedSteps++;
            node.forEachRev((toId, weight) -> {
                Node to = getNode(toId);
                if (!to.related) {
                    to.related = true;
                    reversedQueue.add(to);
                    pushedSteps++;
                }
            });
        }
        return true;
    }

    protected void addToQueueIfNotIn(Node node) {
        if (!node.inqueue) {
            debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR, "%s marked available", node);
            node.inqueue = true;
            queue.add(node);
            pushedSteps++;
        }
    }

    public void addNewCraft(CraftNode craftNode) {
        debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR, "%s generated", craftNode);
        ArrayList<Integer> selections = new ArrayList<>();
        for (int i = 0; i < craftNode.independentIngredients.size(); i++)
            selections.add(0);
        addNewCraft(craftNode, 0, selections);
    }

    public void addNewCraft(CraftNode craftNode, int step, List<Integer> ingredientSelections) {
        if (step >= craftNode.independentIngredients.size()) {
            if (!craftNode.used.contains(ingredientSelections)) {
                craftNode.used.add(ingredientSelections);
                Map<Integer, ItemStack> ingredientId2ItemStack = new HashMap<>();
                for (int i = 0; i < craftNode.independentIngredients.size(); i++) {
                    IngredientNode ingredientNode = craftNode.independentIngredients.get(i);
                    if (ingredientNode.possibleItems.isEmpty())
                        ingredientId2ItemStack.put(ingredientNode.id, ItemStack.EMPTY);
                    else
                        ingredientId2ItemStack.put(
                                ingredientNode.id,
                                ingredientNode
                                        .possibleItems
                                        .get(ingredientSelections.get(i))
                        );
                }
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < craftNode.ingredientNodes.size(); i++) {
                    ItemStack itemStack = ingredientId2ItemStack.get(craftNode.ingredientNodes.get(i).id);
                    items.add(itemStack.copyWithCount(craftNode.ingredientCounts.get(i)));
                }
                for (Function<List<ItemStack>, @Nullable CraftGuideData> f : craftNode.craftGuideSupplier) {
                    CraftGuideData apply = f.apply(items);
                    if (apply != null) {
                        debugContext.log(CraftingDebugContext.TYPE.GENERATOR_GUIDE, "Craft guide added %s", apply);
                        craftGuides.add(apply);
                    }
                }
            }
        } else {
            if (craftNode.independentIngredients.get(step).possibleItems.isEmpty()) {
                addNewCraft(craftNode, step + 1, ingredientSelections);
                return;
            }
            for (int i = 0; i < craftNode.independentIngredients.get(step).possibleItems.size(); i++) {
                if (!craftNode.independentIngredients.get(step).possibleItemNodes.get(i).isAvailable)
                    continue;
                ingredientSelections.set(step, i);
                addNewCraft(craftNode, step + 1, ingredientSelections);
            }
        }
    }


    public void clearStates() {
        for (Node node : nodes) {
            node.inqueue = false;
            node.related = false;
            if (node instanceof ItemNode in) {
                in.isAvailable = false;
            } else if (node instanceof IngredientNode in) {
                in.anyAvailable = false;
            } else if (node instanceof CraftNode cn) {
                cn.used.clear();
            }
        }
        addRecipeQueue.clear();
        queue.clear();
        reversedQueue.clear();
        notToAddRecipe.clear();
        notToAddType.clear();
        craftGuides.clear();
    }

    public void invalidAllCraftWithType(ResourceLocation type) {
        for (Node node : nodes) {
            if (node instanceof CraftNode cn && cn.type.equals(type)) {
                cn.removeAllEdges(this);
            }
        }
    }


}