package Concurrency;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentHashSet<T> {
    private final ReentrantReadWriteLock lock;
    private final Set<T> set;

    public ConcurrentHashSet() {
        this.lock = new ReentrantReadWriteLock();
        this.set = new HashSet<>();
    }

    public void add(T element) {
        this.lock.writeLock().lock();
        this.set.add(element);
        this.lock.writeLock().unlock();
    }

    public boolean conatins(T element) {
        boolean result;

        this.lock.readLock().lock();
        result = this.set.contains(element);
        this.lock.readLock().unlock();

        return result;
    }
}
