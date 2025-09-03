package studio.fantasyit.maid_storage_manager.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;

import java.util.Collection;
import java.util.List;

public class CraftingDebugControlArgument implements ArgumentType<String> {
    protected boolean isAllowChar(char c) {
        if (c >= 'a' && c <= 'z') return true;
        if (c >= 'A' && c <= 'Z') return true;
        if (c == ',' || c == '+' || c == '-' || c == '_') return true;
        return false;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && isAllowChar(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());
        String[] ss = s.split("/,/");
        for (String s1 : ss) {
            CraftingDebugContext.TYPE.valueOf(s1.substring(1));
            assert s1.startsWith("+") || s1.startsWith("-");
        }
        return s;
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("+GENERATOR_RECIPE", "-GENERATOR", "+GENERATOR_RECIPE,-PLANNER");
    }
}
