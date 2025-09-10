package studio.fantasyit.maid_storage_manager.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageVisitLock {
    public static final Map<UUID, LockContext> MAID_TO_LOCK = new ConcurrentHashMap<>();

    public static class LockContext {
        boolean granted;
        final boolean isWriteLock;
        final Target target;
        final EntityMaid maid;

        public LockContext(boolean isWriteLock, Target target, EntityMaid maid) {
            this.maid = maid;
            this.isWriteLock = isWriteLock;
            this.target = target;
            tryGrantLock();
        }

        private void tryGrantLock() {
            if (granted) {
                return;
            }
            if (isWriteLock) {
                granted = StorageVisitLock.lockWrite(target);
            } else {
                granted = StorageVisitLock.lockRead(target);
            }
            if (granted && maid != null) {
                if (MAID_TO_LOCK.containsKey(maid.getUUID()))
                    MAID_TO_LOCK.get(maid.getUUID()).release();
                MAID_TO_LOCK.put(maid.getUUID(), this);
            }
        }

        public boolean checkAndTryGrantLock() {
            if (granted)
                return true;
            tryGrantLock();
            return granted;
        }

        public void release() {
            if (granted) {
                if (isWriteLock) {
                    StorageVisitLock.unlockWrite(target);
                } else {
                    StorageVisitLock.unlockRead(target);
                }
                if (maid != null)
                    MAID_TO_LOCK.remove(maid.getUUID());
            }
        }

        public boolean isHolderValid() {
            return maid.level() instanceof ServerLevel sl && sl.getEntity(maid.getUUID()) instanceof EntityMaid _maid && _maid.isAlive() && _maid.getTask().getUid().equals(StorageManageTask.TASK_ID);
        }
    }

    public static LockContext getReadLock(Target target, EntityMaid maid) {
        return new LockContext(false, target, maid);
    }

    public static LockContext getWriteLock(Target target, EntityMaid maid) {
        return new LockContext(true, target, maid);
    }


    public static final Map<Target, Integer> readLockCounter = new ConcurrentHashMap<>();
    public static final Map<Target, Boolean> writeLockCounter = new ConcurrentHashMap<>();
    public static final Map<Target, Boolean> writeLockWaiting = new ConcurrentHashMap<>();

    private static synchronized boolean lockRead(Target pos) {
        //读锁获取时
        //1.当前没有写锁
        if (writeLockCounter.getOrDefault(pos, false)) {
            return false;
        }
        //2.没有尝试获取写锁
        if (writeLockWaiting.getOrDefault(pos, false)) {
            return false;
        }
        readLockCounter.put(pos, readLockCounter.getOrDefault(pos, 0) + 1);
        return true;
    }

    private static synchronized void unlockRead(Target pos) {
        if (readLockCounter.getOrDefault(pos, 0) > 0) {
            readLockCounter.put(pos, readLockCounter.getOrDefault(pos, 0) - 1);
        } else {
            throw new IllegalStateException("Trying to unlock a read lock that is not locked");
        }
    }

    private static synchronized boolean lockWrite(Target pos) {
        //写锁要求获得时：
        //1. 当前没有写锁
        if (writeLockCounter.getOrDefault(pos, false)) {
            return false;
        }
        // 2. 当前没有读锁
        if (readLockCounter.getOrDefault(pos, 0) > 0) {
            // 有读锁，那么阻止获取新的读锁
            writeLockWaiting.put(pos, true);
            return false;
        }
        // 获取写锁时，取消阻止获取新的读锁
        if (writeLockWaiting.getOrDefault(pos, false)) {
            writeLockWaiting.put(pos, false);
        }
        writeLockCounter.put(pos, true);
        return true;
    }

    private static synchronized void unlockWrite(Target pos) {
        if (!writeLockCounter.get(pos)) {
            throw new IllegalStateException("Trying to unlock a write lock that is not locked");
        }
        writeLockCounter.put(pos, false);
    }

    public static final LockContext DUMMY = new LockContext(false, Target.virtual(BlockPos.ZERO, null), null) {
        @Override
        public boolean checkAndTryGrantLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void release() {
        }
    };

    public static void invalidateInvalidedLock() {
        HashSet<Map.Entry<UUID, LockContext>> entries = new HashSet<>(MAID_TO_LOCK.entrySet());
        for (Map.Entry<UUID, LockContext> entry : entries) {
            if (!entry.getValue().isHolderValid()) {
                entry.getValue().release();
                MAID_TO_LOCK.remove(entry.getKey());
            }
        }
    }

}
