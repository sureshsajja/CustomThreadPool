package com.coderevisited;

import org.junit.Test;

/**
 * User :  Suresh
 * Date :  24/08/15
 * Version : v1
 */

public class ThreadPoolInputValidationTest {

    @Test(expected = IllegalArgumentException.class)
    public void checkConstruction() {
        ThreadPoolWithJobAffinityExecutor executor = new ThreadPoolWithJobAffinityExecutor(0);
        executor.shutdown();
    }


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

    @Test(expected = NullPointerException.class)
    public void checkNullJob() {

        ThreadPoolWithJobAffinityExecutor executor = new ThreadPoolWithJobAffinityExecutor(2);
        executor.submit("MyJob", null);

    }


}
