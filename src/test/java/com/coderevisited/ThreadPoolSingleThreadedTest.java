package com.coderevisited;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;

/**
 * User :  Suresh
 * Date :  23/08/15
 * Version : v1
 */
public class ThreadPoolSingleThreadedTest {


    @Test
    public void expectThreadPoolSizeWorks() {
        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);
        Assert.assertEquals(pool.poolSize(), 10);
        Assert.assertEquals(0, pool.map.size());
    }

    @Test
    public void expectJobAffinity() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);
        String jobId = "Myjob";
        final String[] jobResult = new String[5];
        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                jobResult[0] = "Executed by " + Thread.currentThread().getName();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                jobResult[1] = "Executed by " + Thread.currentThread().getName();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                jobResult[2] = "Executed by " + Thread.currentThread().getName();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                jobResult[3] = "Executed by " + Thread.currentThread().getName();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                jobResult[4] = "Executed by " + Thread.currentThread().getName();
            }
        });

        Assert.assertEquals(1, pool.map.size());

        pool.shutdown();


        for (String s : jobResult) {
            Assert.assertEquals(s, jobResult[0]);
        }

        Assert.assertEquals(0, pool.map.size());
    }

    @Test(expected = RejectedExecutionException.class)
    public void checkExceptionAfterShutdown() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);

        pool.submit("MyJob", new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });


        pool.shutdown();

        pool.submit("MyJob", new Runnable() {
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

    @Test
    public void expectJobsCompletedBeforeShutdown() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);
        final boolean[] status = new boolean[5];

        pool.submit("MyJob1", new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                status[0] = true;
            }
        });

        pool.submit("MyJob2", new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                status[1] = true;
            }
        });

        pool.submit("MyJob3", new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                status[2] = true;
            }
        });

        pool.submit("MyJob4", new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                status[3] = true;
            }
        });

        pool.submit("MyJob5", new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                status[4] = true;
            }
        });

        Assert.assertEquals(5, pool.map.size());

        pool.shutdown();

        Assert.assertEquals(0, pool.map.size());

        for (boolean s : status) {
            Assert.assertEquals(s, true);
        }
    }

    @Test
    public void determineJobOrder() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);
        String jobId = "Myjob";
        final long[] jobResult = new long[5];
        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jobResult[0] = System.nanoTime();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jobResult[1] = System.nanoTime();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jobResult[2] = System.nanoTime();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jobResult[3] = System.nanoTime();
            }
        });

        pool.submit(jobId, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jobResult[4] = System.nanoTime();
            }
        });

        Assert.assertEquals(1, pool.map.size());

        pool.shutdown();


        int prev = 0;
        for (int i = 1; i < jobResult.length; i++) {
            Assert.assertTrue(jobResult[i] >= jobResult[prev]);
            prev = i;
        }

    }

}
