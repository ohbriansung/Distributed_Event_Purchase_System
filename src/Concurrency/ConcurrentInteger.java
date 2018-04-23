package Concurrency;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe ConcurrentInteger class to implement Lamport Timestamps.
 */
public class ConcurrentInteger {
    private final ReentrantReadWriteLock lock;
    private int value;

    /**
     * Constructor of ConcurrentInteger.
     */
    public ConcurrentInteger() {
        this.lock = new ReentrantReadWriteLock();
        this.value = 0;
    }

    /**
     * Increase and return the value.
     *
     * @return int
     */
    public int incrementAndGet() {
        int value;

        this.lock.writeLock().lock();
        this.value += 1;
        value = this.value;
        this.lock.writeLock().unlock();

        return value;
    }

    /**
     * Return the value.
     *
     * @return int
     */
    public int get() {
        int value;

        this.lock.readLock().lock();
        value = this.value;
        this.lock.readLock().unlock();

        return value;
    }

    /**
     * Set the value.
     *
     * @param value
     */
    public void set(int value) {
        this.lock.writeLock().lock();
        this.value = value;
        this.lock.writeLock().unlock();
    }

    /**
     * Increase and return the value without lock.
     *
     * @return int
     */
    public int incrementAndGetWithOutLock() {
        this.value++;
        return this.value;
    }

    /**
     * Decrease and return the value without lock.
     *
     * @return int
     */
    public int decrementAndGetWithOutLock() {
        this.value--;
        return this.value;
    }

    /**
     * Lock write from outside.
     */
    public void lockWrite() {
        this.lock.writeLock().lock();
    }

    /**
     * Unlock write from outside.
     */
    public void unlockWrite() {
        this.lock.writeLock().unlock();
    }
}
