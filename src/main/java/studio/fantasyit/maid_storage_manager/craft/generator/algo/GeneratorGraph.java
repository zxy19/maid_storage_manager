package studio.fantasyit.maid_storage_manager.craft.generator.algo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GeneratorGraph {

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

    public static class Node {
        public int id;
        public boolean inqueue;
        public boolean related;
        public final List<Pair<Integer, Integer>> edges;
        public final List<Pair<Integer, Integer>> edgesRev;


        public Node(int id) {
            this.id = id;
            this.edges = new ArrayList<>();
            this.edgesRev = new ArrayList<>();
            this.related = false;
        }

        public void addEdge(Node to, int weight) {
            this.edges.add(new Pair<>(to.id, weight));
            to.edgesRev.add(new Pair<>(this.id, weight));
        }

        public void forEachEdge(BiConsumer<Integer, Integer> visitor) {
            for (Pair<Integer, Integer> edge : this.edges) {
                visitor.accept(edge.getA(), edge.getB());
            }
        }

        public void forEachRev(BiConsumer<Integer, Integer> visitor) {
            for (Pair<Integer, Integer> edge : this.edgesRev) {
                visitor.accept(edge.getA(), edge.getB());
            }
        }

        public void removeAllEdges(GeneratorGraph graph) {
            this.edges.forEach(edge -> graph.getNode(edge.getA())
                    .edgesRev
                    .removeIf(edgeRev -> edgeRev.getA() == this.id && Objects.equals(edgeRev.getB(), edge.getB()))
            );
            this.edgesRev.forEach(edge -> graph.getNode(edge.getA())
                    .edges
                    .removeIf(edgeRev -> edgeRev.getA() == this.id && Objects.equals(edgeRev.getB(), edge.getB()))
            );
            this.edges.clear();
            this.edgesRev.clear();
        }
    }

    public static class ItemNode extends Node {
        public final ItemStack itemStack;
        public boolean isAvailable;

        public ItemNode(int id, boolean available, ItemStack itemStack) {
            super(id);
            this.itemStack = itemStack;
            this.isAvailable = available;
        }
    }

    public static class CraftNode extends Node {
        public final ResourceLocation recipeId;
        public final Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier;
        public HashSet<List<Integer>> used;
        public final List<IngredientNode> independentIngredients;
        public final List<IngredientNode> ingredientNodes;
        public final List<Integer> ingredientCounts;
        public final ResourceLocation type;
        public final boolean isOneTime;

        public CraftNode(ResourceLocation resourceLocation, int id,
                         Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier,
                         List<IngredientNode> ingredients,
                         List<Integer> ingredientCounts, ResourceLocation type, boolean isOneTime) {
            super(id);
            this.recipeId = resourceLocation;
            this.craftGuideSupplier = craftGuideSupplier;
            this.used = new HashSet<>();
            this.ingredientNodes = ingredients;
            this.ingredientCounts = ingredientCounts;
            this.isOneTime = isOneTime;
            this.type = type;
            HashSet<Integer> independentIngredientsId = new HashSet<>();
            independentIngredients = new ArrayList<>();
            for (IngredientNode ingredientNode : ingredients) {
                if (!independentIngredientsId.contains(ingredientNode.id)) {
                    independentIngredientsId.add(ingredientNode.id);
                    independentIngredients.add(ingredientNode);
                }
            }
        }
    }

    public static class IngredientNode extends Node {
        public List<ItemStack> possibleItems;
        public List<ItemNode> possibleItemNodes;
        public boolean anyAvailable;
        public @Nullable UUID cachedUUID;

        public IngredientNode(int id, List<ItemNode> possibleItemNodes) {
            super(id);
            this.possibleItemNodes = possibleItemNodes;
            this.possibleItems = possibleItemNodes.stream().map(i -> i.itemStack).toList();
            this.anyAvailable = false;
        }

        public boolean isEqualTo(Ingredient ingredient) {
            ItemStack[] items = ingredient.getItems();
            if (items.length != possibleItems.size())
                return false;
            for (int i = 0; i < items.length; i++) {
                if (!ItemStackUtil.isSameInCrafting(items[i], possibleItems.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    List<Node> nodes;
    HashMap<ResourceLocation, List<ItemNode>> itemNodeMap = new HashMap<>();
    HashMap<ResourceLocation, CraftNode> craftNodeMap = new HashMap<>();
    HashMap<UUID, IngredientNode> cachedIngredients = new HashMap<>();
    Set<ResourceLocation> notToAddRecipe = new HashSet<>();
    Set<ResourceLocation> notToAddType = new HashSet<>();

    public Node getNode(int a) {
        return nodes.get(a);
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
                CraftGuideData craftGuideData = CraftGuideData.fromItemStack(item);
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

    public ItemNode getItemNode(ItemStack itemStack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
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
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
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


    public void addRecipe(Recipe<?> recipe, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        List<Integer> ingredientCounts = recipe.getIngredients()
                .stream()
                .map(t -> Arrays.stream(t.getItems()).findFirst().map(ItemStack::getCount).orElse(1))
                .toList();
        addRecipe(
                recipe.getId(),
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


    protected int _addRecipe(ResourceLocation id,
                             List<Ingredient> ingredients,
                             List<Integer> ingredientCounts,
                             List<ItemStack> output,
                             Function<List<ItemStack>,
                                     @Nullable CraftGuideData> craftGuideSupplier,
                             ResourceLocation type,
                             boolean isOneTime
    ) {
        if (notToAddRecipe.contains(id) || notToAddType.contains(type))
            return 1;
        processedSteps++;
        int affectFactor = ingredients.size() + 1;
        if (!RecipeIngredientCache.isCached(id)) {
            affectFactor += (ingredients.size() + 1) + RecipeIngredientCache.getUncachedRecipeIngredient(id, ingredients, this) * 5;
            RecipeIngredientCache.addRecipeCache(id, ingredients);
        }
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
            originalNode.removeAllEdges(this);
            CraftNode craftNode = new CraftNode(id, originalNode.id, craftGuideSupplier, ingredientNodes, ingredientCounts, type, isOneTime);
            nodes.set(originalNode.id, craftNode);
            return craftNode;
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

    public boolean process() {
        if (!addRecipeQueue.isEmpty()) {
            processAddRecipe();
            return false;
        } else if (!reversedQueue.isEmpty()) {
            processReversed();
            return false;
        }
        return processData();
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
            node.inqueue = true;
            queue.add(node);
            pushedSteps++;
        }
    }

    public void addNewCraft(CraftNode craftNode) {
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
                CraftGuideData apply = craftNode.craftGuideSupplier.apply(items);
                if (apply != null)
                    craftGuides.add(apply);
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
    }

    public void invalidAllCraftWithType(ResourceLocation type) {
        for (Node node : nodes) {
            if (node instanceof CraftNode cn && cn.type.equals(type)) {
                cn.removeAllEdges(this);
            }
        }
    }
}