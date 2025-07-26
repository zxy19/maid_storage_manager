package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.generator;


import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CacheOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.GeneratorConfigOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.GraphOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.util.FunctionUtil;
import studio.fantasyit.maid_storage_manager.integration.kubejs.util.TypeCastingUtil;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.AbstractObjectWrapped;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class KJSAutoCraftGuideGenerator extends AbstractObjectWrapped implements IAutoCraftGuideGenerator, IKJSAutoCraftGuideGenerator.Full {

    public KJSAutoCraftGuideGenerator(NativeObject obj, Context context, TypeInfo typeInfo) {
        super(obj, context, typeInfo);
    }

    @Override
    public @NotNull ResourceLocation getType() {
        return get("type", this::resourceLocationParser).orElseThrow();
    }

    @Override
    public boolean allowMultiPosition() {
        return get("allowMultiPosition", this::booleanParser).orElseGet(IAutoCraftGuideGenerator.super::allowMultiPosition);
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return get("isBlockValid", this::booleanParser, level, pos).orElseThrow();
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        get("generate", this::any, inventory, level, pos, new GraphOperator(graph), recognizedTypePositions).orElseThrow();
    }

    @Override
    public void onCache(RecipeManager manager) {
        get("onCache", this::any, manager, CacheOperator.INSTANCE).orElseThrow();
    }

    @Override
    public boolean canCacheGraph() {
        return get("canCacheGraph", this::booleanParser).orElseGet(IAutoCraftGuideGenerator.super::canCacheGraph);
    }

    @Override
    public void init() {
        get("init", this::any);
    }

    @Override
    public boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        Supplier<Boolean> parent = FunctionUtil.wrap(() -> IAutoCraftGuideGenerator.super.positionalAvailable(level, maid, pos, pathFinding));
        return get("positionalAvailable", this::booleanParser, parent, level, maid, pos, pathFinding).orElseGet(() -> IAutoCraftGuideGenerator.super.positionalAvailable(level, maid, pos, pathFinding));
    }

    @Override
    public List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return get("configurations", t -> {
            if (t instanceof NativeArray array) {
                List<ConfigTypes.ConfigType<?>> result = new ArrayList<>();
                for (int i = 0; i < array.getLength(); i++) {
                    result.add(TypeCastingUtil.castOrThrow(array.get(i), ConfigTypes.ConfigType.class));
                }
                return result;
            } else if (t instanceof List<?> list) {
                if (list.isEmpty() || list.get(0) instanceof ConfigTypes.ConfigType<?> config) {
                    return (List<ConfigTypes.ConfigType<?>>) list;
                } else {
                    throw new RuntimeException("ConfigType is not ConfigTypes.ConfigType");
                }
            }
            return null;
        }, GeneratorConfigOperator.INSTANCE).orElseGet(IAutoCraftGuideGenerator.super::getConfigurations);
    }

    @Override
    public Component getConfigName() {
        return get("configName", this::componentParser).orElseThrow();
    }

    @ApiStatus.Internal
    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GraphOperator graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        generate(inventory, level, pos, graph.graph, recognizedTypePositions);
    }

    @ApiStatus.Internal
    @Override
    public void onCache(RecipeManager manager, CacheOperator operator) {
        onCache(manager);
    }

    @ApiStatus.Internal
    @Override
    public Component configName() {
        return getConfigName();
    }

    @ApiStatus.Internal
    @Override
    public ResourceLocation type() {
        return getType();
    }

    @ApiStatus.Internal
    @Override
    public boolean positionalAvailable(Supplier<Boolean> parent, ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        return this.positionalAvailable(level, maid, pos, pathFinding);
    }

    @ApiStatus.Internal
    @Override
    public ConfigTypes.ConfigType<?>[] configurations(GeneratorConfigOperator operator) {
        return null;
    }
}