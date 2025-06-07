package studio.fantasyit.maid_storage_manager.storage;

import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;

public class StoragePredictor {
    public static boolean isPlaceable(Target target) {
        @Nullable IMaidStorage t = MaidStorage.getInstance().getStorage(target.getType());
        if (t == null) return false;
        return t.supportPlace();
    }

    public static boolean isCollectable(Target target) {
        @Nullable IMaidStorage t = MaidStorage.getInstance().getStorage(target.getType());
        if (t == null) return false;
        return t.supportCollect();
    }

    public static boolean isViewable(Target target) {
        @Nullable IMaidStorage t = MaidStorage.getInstance().getStorage(target.getType());
        if (t == null) return false;
        return t.supportView();
    }

}
