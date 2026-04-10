package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.NumberParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.MaidCraftPlanner;
import studio.fantasyit.maid_storage_manager.items.PortableCraftCalculatorBauble;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SimulateCraftingFunction extends AbstractTool<StorageFetchFunction.StorageFetchFunctionData> {
    @Override
    public String call(StorageFetchFunction.StorageFetchFunctionData data, EntityMaid maid, LLMCallback callback) {
        return "";
    }

    @Override
    public CompletableFuture<LLMCallback> onCallAsync(String toolCallId, StorageFetchFunction.StorageFetchFunctionData data, LLMCallback callback, LLMClient client) {

        List<Pair<ItemStack, Integer>> items = new ArrayList<>();
        @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data.itemId()));
        if (item == null) {
            return CompletableFuture.completedFuture(callback.addToolResult(AiUtils.commonFailJson("Item" + data.itemId() + " not found"), toolCallId));
        }
        items.add(new Pair<>(item.getDefaultInstance(), data.count()));
        EntityMaid maid = callback.getMaid();
        MemoryUtil.getCrafting(maid).addAllFromViewed(maid);
        ServerLevel level = (ServerLevel) maid.level();
        return CompletableFuture.supplyAsync(() -> {
            MaidCraftPlanner planner = new MaidCraftPlanner((ServerLevel) maid.level(), maid, items);
            while (!planner.done()) {
                try {
                    level.getServer().submit(()->planner.tick(0)).get();
                    Thread.sleep(50);
                } catch (InterruptedException | ExecutionException e) {
                    break;
                }
            }
            ServerLevel serverLevel = (ServerLevel) maid.level();
            MinecraftServer server = serverLevel.getServer();
            JsonObject obj = new JsonObject();
            if(planner.anySuccess()){
                obj.addProperty("success", true);
                obj.addProperty("steps", planner.getPlan().getLayerCount());
                JsonArray consumes = new JsonArray();
                for(ItemStack itemStack : planner.getPlan().getConsumes()) {
                    JsonObject itemConsumed = new JsonObject();
                    itemConsumed.addProperty("id", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).toString());
                    itemConsumed.addProperty("count", itemStack.getCount());
                    consumes.add(itemConsumed);
                }
                obj.add("consumes", consumes);
            }else{
                obj.addProperty("success", false);
                JsonArray fails = new JsonArray();
                for(String fail : planner.getExtraFailMessage()) {
                    fails.add(fail);
                }
                obj.add("fails", fails);
                JsonArray missings = new JsonArray();
                for(Pair<ItemStack, Integer> missing : planner.getMissings()) {
                    JsonObject missingItem = new JsonObject();
                    missingItem.addProperty("id", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(missing.getA().getItem())).toString());
                    missingItem.addProperty("count", missing.getB());
                    missings.add(missingItem);
                }
                obj.add("missings", missings);
            }

            return server.submit(()-> callback.addToolResult(new Gson().toJson(obj), toolCallId));
        }).thenCompose(t->t);
    }

    @Override
    public String id() {
        return "simulate_crafting";
    }

    @Override
    public String summary(EntityMaid entityMaid) {
        return AiUtils.toolDenyTemplate("get_storage_manual");
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid entityMaid) {
        root.addProperties("itemId", StringParameter.create().setPattern("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+"));
        root.addProperties("count", NumberParameter.create());
        return root;
    }

    @Override
    public Component invocationSummaryComponent(StorageFetchFunction.StorageFetchFunctionData result) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(result.itemId()));
        return Component.translatable("chat_bubbles.maid_storage_manager.ai.simulating",item == null? Component.literal("?"): item.getDefaultInstance().getHoverName()).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public Codec<StorageFetchFunction.StorageFetchFunctionData> codec() {
        return StorageFetchFunction.StorageFetchFunctionData.CODEC;
    }

    @Override
    public boolean trigger(EntityMaid maid, ChatCompletion chatCompletion) {
        return super.trigger(maid, chatCompletion) && !PortableCraftCalculatorBauble.getCalculator(maid).isEmpty();
    }
}
