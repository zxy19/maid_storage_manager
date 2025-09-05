package studio.fantasyit.maid_storage_manager.craft.generator.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.CraftNode;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.IngredientNode;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.ItemNode;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CraftGuideGeneratorTester {
    public static void exportTo(GeneratorGraph graph, List<ItemStack> inventory, String path) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", node.id);
            jsonObject.addProperty("inqueue", node.inqueue);
            jsonObject.addProperty("related", node.related);
            JsonArray edges = new JsonArray();
            node.edges.forEach(e -> {
                JsonObject edge = new JsonObject();
                edge.addProperty("id", e.getA());
                edge.addProperty("weight", e.getB());
                edges.add(edge);
            });
            jsonObject.add("edges", edges);
            JsonArray edgesRev = new JsonArray();
            node.edgesRev.forEach(e -> {
                JsonObject edge = new JsonObject();
                edge.addProperty("id", e.getA());
                edge.addProperty("weight", e.getB());
                edgesRev.add(edge);
            });
            jsonObject.add("edgesRev", edgesRev);
            if (node instanceof ItemNode in) {
                jsonObject.addProperty("isAvailable", in.isAvailable);
                jsonObject.addProperty("type", "item");
            } else if (node instanceof CraftNode cn) {
                jsonObject.addProperty("type", "craft");
                jsonObject.addProperty("scheduled", cn.inqueue);
            } else if (node instanceof IngredientNode in) {
                jsonObject.addProperty("type", "ingredient");
            }
            jsonObject.addProperty("info", node.toString());
            jsonArray.add(jsonObject);
        }
        JsonArray craftGuides = new JsonArray();
        for (CraftGuideData cg : graph.getCraftGuides()) {
            craftGuides.add(cg.toString());
        }
        JsonArray items = new JsonArray();
        for (ItemStack itemStack : inventory) {
            JsonObject item = new JsonObject();
            item.addProperty("id", itemStack.getItem().toString());
            item.addProperty("count", itemStack.getCount());
            items.add(item);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("nodes", jsonArray);
        jsonObject.add("craftGuides", craftGuides);
        jsonObject.add("items", items);
        try (FileWriter fileWriter = new FileWriter(path)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (IOException ignored) {
        }
    }
}
