package Concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlockingThreads {
    private ReentrantReadWriteLock lock;
    private List<Thread> blockingThreads;

    public BlockingThreads() {
        this.lock = new ReentrantReadWriteLock();
        this.blockingThreads = new ArrayList<>();
    }

    public void add(Thread thread) {
        this.lock.writeLock().lock();
        this.blockingThreads.add(thread);
        this.lock.writeLock().unlock();
    }

    public void wakeAndRemoveAll() {
        this.lock.writeLock().lock();
        for (Thread thread : this.blockingThreads) {
            try {
                thread.interrupt();
            }
            catch (Exception ignored) {}
        }
        this.blockingThreads.clear();
        this.lock.writeLock().unlock();
    }
}
