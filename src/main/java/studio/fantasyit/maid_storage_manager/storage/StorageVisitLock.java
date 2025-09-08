package studio.fantasyit.maid_storage_manager.storage;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StorageVisitLock {
    public static class LockContext {
        boolean granted;
        final boolean isWriteLock;
        final Target target;

        public LockContext(boolean isWriteLock, Target target) {
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
            }
        }
    }

    public static LockContext getReadLock(Target target) {
        return new LockContext(false, target);
    }

    public static LockContext getWriteLock(Target target) {
        return new LockContext(true, target);
    }


    public static final Map<Target, Integer> readLockCounter = new ConcurrentHashMap<>();
    public static final Map<Target, Boolean> writeLockCounter = new ConcurrentHashMap<>();
    public static final Map<Target, Boolean> writeLockWaiting = new ConcurrentHashMap<>();

    public static synchronized boolean lockRead(Target pos) {
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

    public static synchronized void unlockRead(Target pos) {
        if (readLockCounter.getOrDefault(pos, 0) > 0) {
            readLockCounter.put(pos, readLockCounter.getOrDefault(pos, 0) - 1);
        } else {
            throw new IllegalStateException("Trying to unlock a read lock that is not locked");
        }
    }

    public static synchronized boolean lockWrite(Target pos) {
        //写锁要求获得时：
        //1. 当前没有写锁
        if (writeLockWaiting.getOrDefault(pos, false)) {
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

    public static synchronized void unlockWrite(Target pos) {
        if (!writeLockCounter.get(pos)) {
            throw new IllegalStateException("Trying to unlock a write lock that is not locked");
        }
        writeLockCounter.put(pos, false);
    }

    public static final LockContext DUMMY = new LockContext(false, Target.virtual(BlockPos.ZERO, null)) {
        @Override
        public boolean checkAndTryGrantLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void release() {
        }
    };

}
