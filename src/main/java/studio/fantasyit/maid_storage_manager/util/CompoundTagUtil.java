package studio.fantasyit.maid_storage_manager.util;

import joptsimple.internal.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.StringUtils;

public class CompoundTagUtil {
    protected static Tag _removeKeyFrom(Tag tag, String[] key, int depth) {
        if (depth >= key.length) return tag;
        if (tag instanceof CompoundTag c) return removeKeyFrom(c, key, depth);
        if (tag instanceof ListTag l) return removeKeyFrom(l, key, depth);
        return tag;
    }

    /**
     * 删除tag中的key
     * @param tag 待删除的tag
     * @param key 待删除的key
     * @param depth 当前深度
     * @return 删除后的tag
     */
    public static CompoundTag removeKeyFrom(CompoundTag tag, String[] key, int depth) {
        if (depth >= key.length) return tag;
        String k = key[depth];
        if (Strings.isNullOrEmpty(k))
            return tag;
        if (tag.contains(k) && depth == key.length - 1) {
            tag.remove(k);
            return tag;
        }
        if (k.equals("*") && depth == key.length - 1) {
            return new CompoundTag();
        }
        if (tag.contains(k)) {
            tag.put(k, removeKeyFrom(tag.getCompound(k), key, depth + 1));
            return tag;
        }
        if (k.equals("*")) {
            for (String kk : tag.getAllKeys()) {
                tag.put(kk, removeKeyFrom(tag.getCompound(kk), key, depth + 1));
            }
        }
        return tag;
    }

    /**
     * 删除tag中的key
     * @param tag 待删除的tag
     * @param key 待删除的key
     * @param depth 当前深度
     * @return 删除后的tag
     */
    public static ListTag removeKeyFrom(ListTag tag, String[] key, int depth) {
        if (depth >= key.length) return tag;
        String k = key[depth];
        if (Strings.isNullOrEmpty(k))
            return tag;
        if (k.endsWith("]")) {
            String k2 = k.substring(0, k.length() - 1);
            if (StringUtils.isNumeric(k2)) {
                int index = Integer.parseInt(k2);
                if (tag.size() > index && depth == key.length - 1) {
                    tag.remove(index);
                } else {
                    tag.set(index, _removeKeyFrom(tag.get(index), key, depth + 1));
                }
            }
        } else if (k.equals("*]") || k.equals("*")) {
            if (depth == key.length - 1)
                return new ListTag();
            for (int i = tag.size() - 1; i >= 0; i--) {
                tag.set(i, _removeKeyFrom(tag.get(i), key, depth + 1));
            }
        }
        return tag;
    }
}
