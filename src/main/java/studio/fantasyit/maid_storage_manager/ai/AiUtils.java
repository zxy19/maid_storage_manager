package studio.fantasyit.maid_storage_manager.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;

import java.util.Locale;

import static com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.StringConstant.LANGUAGE_FORMAT;

public class AiUtils {
    public static String transformLanguage(String language) {
        Locale locale = Locale.forLanguageTag(language);
        return LANGUAGE_FORMAT.formatted(locale.getDisplayLanguage(), locale.getDisplayCountry());
    }
    public static String commonFailJson(String message){
        JsonObject o = new JsonObject() ;
        o.addProperty("error",true);
        o.addProperty("msg",message);
        return  new Gson().toJson(o);
    }
    public static JsonArray posToArray(BlockPos pos){
        JsonArray p = new JsonArray();
        p.add(pos.getX());
        p.add(pos.getY());
        p.add(pos.getZ());
        return p;
    }
}
