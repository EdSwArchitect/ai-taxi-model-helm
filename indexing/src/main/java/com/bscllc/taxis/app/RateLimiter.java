package com.bscllc.taxis.app;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Simple rate limiter implementation using a semaphore and scheduled token refill.
 */
public class RateLimiter {
    
    private final Semaphore semaphore;
    private final int permitsPerSecond;
    private final java.util.concurrent.ScheduledExecutorService scheduler;
    
    /**
     * Creates a new rate limiter.
     *
     * @param permitsPerSecond maximum number of permits per second
     */
    public RateLimiter(int permitsPerSecond) {
        if (permitsPerSecond <= 0) {
            throw new IllegalArgumentException("Permits per second must be positive");
        }
        this.permitsPerSecond = permitsPerSecond;
        // Initialize with max permits available
        this.semaphore = new Semaphore(permitsPerSecond, true);
        this.scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Refill");
            t.setDaemon(true);
            return t;
        });
        
        // Refill permits every second - release up to permitsPerSecond
        scheduler.scheduleAtFixedRate(
            () -> {
                int currentAvailable = semaphore.availablePermits();
                // Only add permits if we're below the max
                if (currentAvailable < permitsPerSecond) {
                    int permitsToAdd = permitsPerSecond - currentAvailable;
                    semaphore.release(permitsToAdd);
                }
            },
            1, 1, TimeUnit.SECONDS
        );
    }
    
    /**
     * Acquires a permit, blocking if necessary until one is available.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }
    
    /**
     * Acquires the given number of permits, blocking if necessary.
     *
     * @param permits number of permits to acquire
     * @throws InterruptedException if interrupted while waiting
     */
    public void acquire(int permits) throws InterruptedException {
        semaphore.acquire(permits);
    }
    
    /**
     * Tries to acquire a permit without blocking.
     *
     * @return true if permit was acquired, false otherwise
     */
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }
    
    /**
     * Tries to acquire the given number of permits without blocking.
     *
     * @param permits number of permits to acquire
     * @return true if permits were acquired, false otherwise
     */
    public boolean tryAcquire(int permits) {
        return semaphore.tryAcquire(permits);
    }
    
    /**
     * Tries to acquire a permit within the given timeout.
     *
     * @param timeout timeout duration
     * @param unit time unit
     * @return true if permit was acquired, false if timeout
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(timeout, unit);
    }
    
    /**
     * Gets the number of available permits.
     *
     * @return available permits
     */
    public int availablePermits() {
        return semaphore.availablePermits();
    }
    
    /**
     * Shuts down the rate limiter.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

