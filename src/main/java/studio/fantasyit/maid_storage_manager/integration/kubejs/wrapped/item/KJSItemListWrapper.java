package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.item;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import studio.fantasyit.maid_storage_manager.integration.kubejs.util.TypeCastingUtil;

import java.util.ArrayList;
import java.util.List;

public class KJSItemListWrapper implements TypeWrapperFactory<KJSItemPair[]> {
    public static final TypeWrapperFactory<KJSItemPair> wrapper = new KJSItemPairWrapper();

    @Override
    public KJSItemPair[] wrap(Context context, Object o) {
        if (o instanceof NativeObject obj) {
            return new KJSItemPair[]{TypeCastingUtil.wrapOrThrow(obj, context, wrapper)};
        } else if (o instanceof NativeArray arr) {
            List<KJSItemPair> list = new ArrayList<>();
            for (Object o1 : arr) {
                list.add(TypeCastingUtil.wrapOrThrow(o1, context, wrapper));
            }
            return list.toArray(new KJSItemPair[0]);
        } else if (o instanceof List<?> list)
            return (KJSItemPair[]) list.stream().map(o1 -> TypeCastingUtil.castOrThrow(o1, KJSItemPair.class)).toArray();
        return null;
    }
}