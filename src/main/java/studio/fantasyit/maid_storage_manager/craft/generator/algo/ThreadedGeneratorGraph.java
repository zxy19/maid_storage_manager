package studio.fantasyit.maid_storage_manager.craft.generator.algo;

import net.minecraft.core.RegistryAccess;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.util.ThreadingUtil;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadedGeneratorGraph extends GeneratorGraph {
    ReentrantReadWriteLock tickLock = new ReentrantReadWriteLock();
    Future<?> running = null;

    public ThreadedGeneratorGraph(RegistryAccess registryAccess) {
        super(registryAccess);
    }

    @Override
    public void clearStates() {
        super.clearStates();
        running = null;
    }

    @Override
    public boolean process() {
        if (running != null) {
            return running.isDone();
        } else {
            running = ThreadingUtil.run(this::_process);
            return false;
        }
    }

    public void _process() {
        while (true) {
            if (!tickLock.writeLock().tryLock()) {
                continue;
            }
            boolean ret;
            try {
                ret = super.process();
            } catch (Exception e) {
                Logger.logger.error("In process craft guides generator", e);
                break;
            } finally {
                tickLock.writeLock().unlock();
            }
            if (ret) break;
        }
    }

    @Override
    public int getProcessedSteps() {
        tickLock.readLock().lock();
        int processedSteps1 = super.getProcessedSteps();
        tickLock.readLock().unlock();
        return processedSteps1;
    }

    @Override
    public int getPushedSteps() {
        tickLock.readLock().lock();
        int pushedSteps1 = super.getPushedSteps();
        tickLock.readLock().unlock();
        return pushedSteps1;
    }
}
