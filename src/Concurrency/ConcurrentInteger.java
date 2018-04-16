package Concurrency;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentInteger {
    private final ReentrantReadWriteLock lock;
    private int value;

    public ConcurrentInteger() {
        this.lock = new ReentrantReadWriteLock();
        this.value = 0;
    }

    public int incrementAndGet() {
        int value;

        this.lock.writeLock().lock();
        this.value += 1;
        value = this.value;
        this.lock.writeLock().unlock();

        return value;
    }

    public int get() {
        int value;

        this.lock.readLock().lock();
        value = this.value;
        this.lock.readLock().unlock();

        return value;
    }

    public void set(int value) {
        this.lock.writeLock().lock();
        this.value = value;
        this.lock.writeLock().unlock();
    }

    public int incrementAndGetWithOutLock() {
        this.value++;
        return this.value;
    }

    public int decrementAndGetWithOutLock() {
        this.value--;
        return this.value;
    }

    public void lockWrite() {
        this.lock.writeLock().lock();
    }

    public void unlockWrite() {
        this.lock.writeLock().unlock();
    }
}
