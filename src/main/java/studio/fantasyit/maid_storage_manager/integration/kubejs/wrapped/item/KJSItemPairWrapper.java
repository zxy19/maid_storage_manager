package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.item;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import net.minecraft.world.item.ItemStack;

public class KJSItemPairWrapper implements TypeWrapperFactory<KJSItemPair> {
    @Override
    public KJSItemPair wrap(Context context, Object o) {
        if (o instanceof NativeObject obj) {
            int count = -1;
            if (obj.containsKey("count") && obj.get("count") instanceof Integer c)
                count = c;
            if (obj.containsKey("stack") && obj.get("stack") instanceof ItemStack stack) {
                if (count == -1) count = stack.getCount();
                return new KJSItemPair(stack, count);
            }
        } else if (o instanceof ItemStack itemStack) {
            return new KJSItemPair(itemStack, itemStack.getCount());
        }
        return null;
    }
}