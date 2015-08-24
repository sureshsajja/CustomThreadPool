package com.coderevisited;

import org.junit.Test;

/**
 * User :  Suresh
 * Date :  24/08/15
 * Version : v1
 */

public class ThreadPoolInputValidationTest {

    /**
     * Tests if pool is initiated with at least one thread
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkConstruction() {
        ThreadPoolWithJobAffinityExecutor executor = new ThreadPoolWithJobAffinityExecutor(0);
        executor.shutdown();
    }

    /**
     * Tests if pool is initiated with at least one thread
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkConstructionWithNegativeSize() {
        ThreadPoolWithJobAffinityExecutor executor = new ThreadPoolWithJobAffinityExecutor(-1);
        executor.shutdown();
    }


    /**
     * Tests if JobId is not null
     */
    @Test(expected = NullPointerException.class)
    public void checkNullJobId() {
        ThreadPoolWithJobAffinityExecutor executor = new ThreadPoolWithJobAffinityExecutor(2);
        executor.submit(null, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * Tests if runnable job is not null
     */
    @Test(expected = NullPointerException.class)
    public void checkNullJob() {

        ThreadPoolWithJobAffinityExecutor executor = new ThreadPoolWithJobAffinityExecutor(2);
        executor.submit("MyJob", null);

    }


}
