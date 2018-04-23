package Concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe BlockingThreads class for FrontEnd to store blocking requests.
 */
public class BlockingThreads {
    private ReentrantReadWriteLock lock;
    private List<Thread> blockingThreads;

    /**
     * Constructor of BlockingThreads.
     */
    public BlockingThreads() {
        this.lock = new ReentrantReadWriteLock();
        this.blockingThreads = new ArrayList<>();
    }

    /**
     * add method to add the reference of the request thread.
     *
     * @param thread
     *      - request thread
     */
    public void add(Thread thread) {
        this.lock.writeLock().lock();
        this.blockingThreads.add(thread);
        this.lock.writeLock().unlock();
    }

    /**
     * wake all the blocking thread and remove them from the list.
     */
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
