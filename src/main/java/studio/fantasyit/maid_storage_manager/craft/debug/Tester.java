package studio.fantasyit.maid_storage_manager.craft.debug;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Tester {
    public Tester(String path) throws FileNotFoundException {
        JsonElement parser = JsonParser.parseReader(new JsonReader(new FileReader(path)));

    }

    public static void exportTo(AbstractBiCraftGraph graph, String path) {
        graph.restoreCurrent();
    }
}
