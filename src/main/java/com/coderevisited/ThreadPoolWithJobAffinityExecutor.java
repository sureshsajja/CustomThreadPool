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
 * This class maintains the map of JobId to Single Thread executor.
 * Since this is a fixed sized thread pool and to accommodate all JobIds, map key is generated
 * by doing operation (hashcode of JobId) % poolSize
 */
public class ThreadPoolWithJobAffinityExecutor implements ThreadPoolWithJobAffinity {

    private final int poolSize;

    public final ConcurrentMap<Integer, ExecutorService> map;
    private Lock lock = new ReentrantLock();
    private AtomicBoolean state = new AtomicBoolean(true);


    public ThreadPoolWithJobAffinityExecutor(int corePoolSize) {

        if (corePoolSize <= 0)
            throw new IllegalArgumentException();
        this.poolSize = corePoolSize;
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public int poolSize() {
        return poolSize;
    }

    /**
     * Initiate a new singleThreaded executor if there is a JobId that was not seen earlier.
     *
     * @param jobId a string containing job id.
     * @param job   a Runnable representing the job to be executed.
     */
    @Override
    public void submit(String jobId, Runnable job) {

        if (jobId == null || job == null)
            throw new NullPointerException();

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

    /**
     * Shuts down all single threaded executors. And awaits their termination.
     * Removes references of those executors from map
     * Updates global reference that this pool is terminated.
     */
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

    /**
     * Returns bucket key for a given JobId
     *
     * @param jobId JobId
     * @return bucketKey
     */
    private int getPool(String jobId) {
        int h = jobId.hashCode();
        return Math.abs(h) % poolSize;
    }
}
