package studio.fantasyit.maid_storage_manager.ai;

import java.util.Locale;

import static com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.StringConstant.LANGUAGE_FORMAT;

public class AiUtils {
    public static String transformLanguage(String language) {
        Locale locale = Locale.forLanguageTag(language);
        return LANGUAGE_FORMAT.formatted(locale.getDisplayLanguage(), locale.getDisplayCountry());
    }
}
