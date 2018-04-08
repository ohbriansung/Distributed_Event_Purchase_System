package Concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServiceList<T> {
    private ReentrantReadWriteLock lock;
    private Set<T> list;
    private T primary;

    /**
     * ServiceList constructor.
     * Use TreeSet for storing service list to prevent duplicates,
     * and to sort for Bully Election.
     */
    public ServiceList() {
        this.lock = new ReentrantReadWriteLock();
        this.list = new TreeSet<>();
    }

    public void addService(T service) {
        this.lock.writeLock().lock();
        this.list.add(service);
        this.lock.writeLock().unlock();
    }

    public boolean removeService(T service) {
        boolean result;

        this.lock.writeLock().lock();
        result = this.list.remove(service);
        this.lock.writeLock().unlock();

        return result;
    }

    public List<T> getList() {
        List<T> list = new ArrayList<>();

        this.lock.readLock().lock();
        list.addAll(this.list);
        this.lock.readLock().unlock();

        return list;
    }

    public void setPrimary(T primary) {
        this.lock.writeLock().lock();
        this.primary = primary;
        this.lock.writeLock().unlock();
    }

    public T getPrimary() {
        T primary;

        this.lock.readLock().lock();
        primary = this.primary;
        this.lock.readLock().unlock();

        return primary;
    }
}
