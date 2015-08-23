package com.coderevisited;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * User :  Suresh
 * Date :  23/08/15
 * Version : v1
 */

/**
 * This class maintains the map of JobId to Single Thread executor
 * 1. Since this is a fixed sized thread pool and to accommodate all JobIds, map key is generated
 * by doing operation (hashcode of JobId) % poolSize
 * 2.
 */
public class ThreadPoolWithJobAffinityExecutor implements ThreadPoolWithJobAffinity {

    private final int poolSize;

    public final ConcurrentMap<Integer, ExecutorService> map;
    private Lock lock = new ReentrantLock();
    private AtomicBoolean state = new AtomicBoolean(true);


    public ThreadPoolWithJobAffinityExecutor(int corePoolSize) {
        this.poolSize = corePoolSize;
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public int poolSize() {
        return poolSize;
    }

    @Override
    public void submit(String jobId, Runnable job) {

        int bucketNumber = getPool(jobId);
        if (state.get()) {
            lock.lock();
            try {
                if (state.get()) {
                    if (!map.containsKey(bucketNumber)) {
                        map.put(bucketNumber, Executors.newSingleThreadExecutor());
                    }
                    map.get(bucketNumber).submit(job);
                } else {
                    throw new RejectedExecutionException("Thread pool is terminated");
                }
            } finally {
                lock.unlock();
            }
        } else {
            throw new RejectedExecutionException("Thread pool is terminated");
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            for (ConcurrentMap.Entry<Integer, ExecutorService> entry : map.entrySet()) {
                entry.getValue().shutdown();
                boolean terminated = false;
                while (!terminated) {
                    try {
                        terminated = entry.getValue().awaitTermination(10000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                map.remove(entry.getKey());
            }
        } finally {
            state.set(false);
            lock.unlock();
        }

    }

    private int getPool(String jobId) {
        int h = jobId.hashCode();
        return Math.abs(h) % poolSize;
    }
}
