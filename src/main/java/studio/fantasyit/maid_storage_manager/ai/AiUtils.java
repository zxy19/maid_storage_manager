package studio.fantasyit.maid_storage_manager.ai;

import java.util.HashMap;
import java.util.Map;

public class AiUtils {
    public static final Map<String, String> languageMap = new HashMap<>();

    static {
        languageMap.put("de_DE", "Deutsch (Deutschland)");
        languageMap.put("es_ES", "Español (España)");
        languageMap.put("fr_FR", "Français (France)");
        languageMap.put("it_IT", "Italiano (Italia)");
        languageMap.put("ja_JP", "日本語 (日本)");
        languageMap.put("ko_KR", "한국어 (대한민국)");
        languageMap.put("la_LA", "Latina (Civitas Vaticana)  ");
        languageMap.put("pt_BR", "Português (Brasil)");
        languageMap.put("pt_PT", "Português (Portugal)");
        languageMap.put("ru_RU", "Русский (Россия)");
        languageMap.put("tr_TR", "Türkçe (Türkiye)");
        languageMap.put("vi_VN", "Tiếng Việt (Việt Nam)");
        languageMap.put("zh_CN", "简体中文 (中国大陆)");
    }

    public static String transformLanguage(String language) {
        for (Map.Entry<String, String> entry : languageMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(language)) {
                return entry.getValue();
            }
        }
        return language;
    }
}
