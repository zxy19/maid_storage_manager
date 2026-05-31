package studio.fantasyit.maid_storage_manager.craft.generator.algo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
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

    protected record AddRecipeData(Identifier id,
                                   List<Ingredient> ingredients,
                                   List<Integer> ingredientCounts,
                                   List<ItemStack> output,
                                   Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier,
                                   Identifier currentType, boolean oneTime) {
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
    HashMap<Identifier, List<ItemNode>> itemNodeMap = new HashMap<>();
    HashMap<Identifier, CraftNode> craftNodeMap = new HashMap<>();
    HashMap<UUID, IngredientNode> cachedIngredients = new HashMap<>();
    Set<Identifier> notToAddRecipe = new HashSet<>();
    Set<Identifier> notToAddType = new HashSet<>();

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
        Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
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
        Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (!itemNodeMap.containsKey(id))
            itemNodeMap.put(id, new ArrayList<>());
        itemNodeMap.get(id).add(itemNode);

        for (Node n : nodes) {
            if (n instanceof IngredientNode in && in.test(itemStack)) {
                in.addPossibleItem(itemNode);
                itemNode.addEdge(in, 1);
                if (available) {
                    addToQueueIfNotIn(in);
                }
            }
        }
        return itemNode;
    }

    /// ///////////////原料节点处理///////
    public IngredientNode addOrGetIngredientNode(Ingredient ingredient) {
        for (Node node : nodes) {
            if (node instanceof IngredientNode in && in.ingredient.equals(ingredient)) {
                return in;
            }
        }
        return addIngredientNode(ingredient);
    }

    public IngredientNode addOrGetCahcedIngredientNode(Ingredient ingredient, UUID uuid) {
        return addOrGetIngredientNode(ingredient);
    }

    private IngredientNode addIngredientNode(Ingredient ingredient) {
        List<ItemNode> matched = new ArrayList<>();
        for (Node n : nodes) {
            if (n instanceof ItemNode itemNode && ingredient.test(itemNode.itemStack)) {
                matched.add(itemNode);
            }
        }
        IngredientNode ingredientNode = new IngredientNode(nodes.size(), ingredient, matched);
        nodes.add(ingredientNode);

        for (ItemNode itemNode : matched) {
            itemNode.addEdge(ingredientNode, 1);
            if (itemNode.isAvailable) {
                addToQueueIfNotIn(ingredientNode);
            }
        }
        return ingredientNode;
    }


    /// ////////////配方节点处理/////////////

    protected Identifier currentType;
    protected boolean oneTime = false;

    public void setCurrentGeneratorType(IAutoCraftGuideGenerator generator) {
        this.currentType = generator.getType();
        this.oneTime = generator.canCacheGraph();
    }

    public void setCurrentGeneratorType(Identifier internalType, boolean b) {
        this.currentType = internalType;
        this.oneTime = b;
    }


    public void addRecipe(RecipeHolder<? extends Recipe<?>> holder, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        Recipe<?> recipe = holder.value();
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        List<Integer> ingredientCounts = ingredients
                .stream()
                .map(GenerateIngredientUtil::getIngredientCount)
                .toList();
        addRecipe(
                holder.id().identifier(),
                ingredients,
                ingredientCounts,
                ItemStack.EMPTY,
                craftGuideSupplier
        );
        pushedSteps++;
    }

    public void addRecipeWrapId(RecipeHolder<? extends Recipe<?>> holder, Identifier generator, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        Recipe<?> recipe = holder.value();
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        List<Integer> ingredientCounts = ingredients
                .stream()
                .map(GenerateIngredientUtil::getIngredientCount)
                .toList();
        addRecipe(
                RecipeUtil.wrapLocation(generator, holder.id().identifier()),
                ingredients,
                ingredientCounts,
                ItemStack.EMPTY,
                craftGuideSupplier
        );
        pushedSteps++;
    }


    public void addRecipe(Identifier id, List<Ingredient> ingredients, List<Integer> ingredientCounts, ItemStack output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        addRecipe(id, ingredients, ingredientCounts, List.of(output), craftGuideSupplier);
    }

    public void addRecipe(Identifier id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        if (RecipeUtil.shouldSkip(id, ingredients, ingredientCounts, output))
            return;
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

    public void blockType(Identifier type) {
        notToAddType.add(type);
    }

    public void blockRecipe(Identifier id) {
        notToAddRecipe.add(id);
    }

    public void removeBlockedRecipe(Identifier id) {
        notToAddRecipe.remove(id);
    }

    public void removeBlockedType(Identifier type) {
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


    protected int _addRecipe(Identifier id,
                             List<Ingredient> ingredients,
                             List<Integer> ingredientCounts,
                             List<ItemStack> output,
                             Function<List<ItemStack>,
                                     @Nullable CraftGuideData> craftGuideSupplier,
                             Identifier type,
                             boolean isOneTime
    ) {
        if (notToAddRecipe.contains(id) || notToAddType.contains(type)) {
            debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR_RECIPE, "recipe blocked %s", id);
            return 1;
        }
        debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR_RECIPE, "recipe add %s", id);
        processedSteps++;

        List<IngredientNode> ingredientNodes = ingredients.stream()
                .map(this::addOrGetIngredientNode)
                .toList();
        addRecipeWithIngredients(id, ingredients, ingredientCounts, output, ingredientNodes, craftGuideSupplier, type, isOneTime);

        return ingredients.size() + 1;
    }

    public void addRecipeWithIngredients(Identifier id,
                                         List<Ingredient> ingredients,
                                         List<Integer> ingredientCounts,
                                         List<ItemStack> outputs,
                                         List<IngredientNode> ingredientNodes,
                                         Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier, Identifier type, boolean isOneTime) {
        CraftNode craftNode = getOrCreateCraftNode(id, ingredientCounts, ingredientNodes, craftGuideSupplier, type, isOneTime);

        for (IngredientNode ingredientNode : craftNode.independentIngredients) {
            ingredientNode.addEdge(craftNode, 1);
        }

        outputs.forEach(output -> craftNode.addEdge(getItemNodeOrCreate(output, false), 1));
    }

    private @NotNull CraftNode getOrCreateCraftNode(Identifier id, List<Integer> ingredientCounts, List<IngredientNode> ingredientNodes, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier, Identifier type, boolean isOneTime) {
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
        List<AddRecipeData> batch = new ArrayList<>();
        while (!addRecipeQueue.isEmpty() && c++ < MAX_PRE_TICK * 10) {
            batch.add(addRecipeQueue.poll());
        }

        // Phase 1: pre-create all IngredientNodes
        for (AddRecipeData data : batch) {
            if (notToAddRecipe.contains(data.id) || notToAddType.contains(data.currentType))
                continue;
            for (Ingredient ingredient : data.ingredients) {
                addOrGetIngredientNode(ingredient);
            }
        }

        // Phase 2: create CraftNodes
        c = 0;
        for (AddRecipeData data : batch) {
            c += _addRecipe(data.id,
                    data.ingredients,
                    data.ingredientCounts,
                    data.output,
                    data.craftGuideSupplier,
                    data.currentType,
                    data.oneTime
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

    public void invalidAllCraftWithType(Identifier type) {
        for (Node node : nodes) {
            if (node instanceof CraftNode cn && cn.type.equals(type)) {
                cn.removeAllEdges(this);
            }
        }
    }


}