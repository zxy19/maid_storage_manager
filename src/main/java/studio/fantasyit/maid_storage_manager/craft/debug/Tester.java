package studio.fantasyit.maid_storage_manager.craft.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.*;
import studio.fantasyit.maid_storage_manager.craft.algo.graph.SimpleSearchGraph;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    public static void startTestFrom(String path) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        JsonObject data = gson.fromJson(new FileReader(path), JsonObject.class);
        JsonArray jsonElements = data.get("nodes").getAsJsonArray();
        SimpleSearchGraph graph = new SimpleSearchGraph(List.of(), List.of());
        for (int i = 0; i < jsonElements.size(); i++) {
            JsonObject jsonObject = jsonElements.get(i).getAsJsonObject();
            if (jsonObject.get("type").getAsString().equals("item")) {
                ItemNodeBasic itemNode = new SimItemNode(i, true, jsonObject.get("info").getAsString());
                itemNode.count = jsonObject.get("count").getAsInt();
                itemNode.crafted = jsonObject.get("crafted").getAsInt();
                itemNode.required = jsonObject.get("required").getAsInt();
                itemNode.isLoopedIngredient = jsonObject.get("isLoopedIngredient").getAsBoolean();
                itemNode.loopInputIngredientCount = jsonObject.get("loopInputIngredientCount").getAsInt();
                itemNode.singleTimeCount = jsonObject.get("singleTimeCount").getAsInt();
                graph.addNode(itemNode);
            } else if (jsonObject.get("type").getAsString().equals("craft")) {
                List<Pair<Integer, Integer>> edges = new ArrayList<>();
                for (int j = 0; j < jsonObject.get("edges").getAsJsonArray().size(); j++) {
                    JsonObject edge = jsonObject.get("edges").getAsJsonArray().get(j).getAsJsonObject();
                    edges.add(new Pair<>(edge.get("id").getAsInt(), edge.get("weight").getAsInt()));
                }
                List<Pair<Integer, Integer>> edgesRev = new ArrayList<>();
                for (int j = 0; j < jsonObject.get("edgesRev").getAsJsonArray().size(); j++) {
                    JsonObject edge = jsonObject.get("edgesRev").getAsJsonArray().get(j).getAsJsonObject();
                    edgesRev.add(new Pair<>(edge.get("id").getAsInt(), edge.get("weight").getAsInt()));
                }

                CraftNodeBasic craftNode = new SimCraftNode(i, true, jsonObject.get("info").getAsString(), edges, edgesRev);
                graph.addNode(craftNode);
            }
        }
        graph.startContext(data.get("targetItemId").getAsInt(), data.get("targetItemCount").getAsInt());
        graph.buildGraphInstantly();
        graph.process();
    }

    public static void exportTo(AbstractBiCraftGraph graph, ItemStack targetItem, int count, String path) {
        graph.restoreCurrent();
        graph.startContext(targetItem, count);
        graph.buildGraphInstantly();

        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
            JsonObject jsonObject = new JsonObject();
            if (node instanceof ItemNode in) {
                jsonObject.addProperty("type", "item");
                jsonObject.addProperty("required", in.required);
                jsonObject.addProperty("crafted", in.crafted);
                jsonObject.addProperty("count", in.count);
                jsonObject.addProperty("isLoopedIngredient", in.isLoopedIngredient);
                jsonObject.addProperty("loopInputIngredientCount", in.loopInputIngredientCount);
                jsonObject.addProperty("singleTimeCount", in.singleTimeCount);
                jsonObject.addProperty("info", in.itemStack.getItem().toString());
            } else if (node instanceof CraftNode cn) {
                jsonObject.addProperty("type", "craft");
                jsonObject.addProperty("scheduled", cn.scheduled);
                jsonObject.addProperty("hasLoopIngredient", cn.hasLoopIngredient);
                JsonArray edges = new JsonArray();
                cn.edges.forEach(e -> {
                    JsonObject edge = new JsonObject();
                    edge.addProperty("id", e.getA());
                    edge.addProperty("weight", e.getB());
                    edges.add(edge);
                });
                jsonObject.add("edges", edges);
                JsonArray edgesRev = new JsonArray();
                cn.revEdges.forEach(e -> {
                    JsonObject edge = new JsonObject();
                    edge.addProperty("id", e.getA());
                    edge.addProperty("weight", e.getB());
                    edgesRev.add(edge);
                });
                jsonObject.add("edgesRev", edgesRev);
                jsonObject.addProperty("info", cn.craftGuideData.toString());
            }
            jsonArray.add(jsonObject);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("targetItemId", graph.getItemNode(targetItem).id);
        jsonObject.addProperty("targetItemCount", count);
        jsonObject.add("nodes", jsonArray);
        try {
            Gson gson = new GsonBuilder().create();
            gson.toJson(jsonObject, new FileWriter(path));
        } catch (IOException ignored) {
        }
    }
}
