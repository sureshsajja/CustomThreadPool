package com.coderevisited;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;

/**
 * User :  Suresh
 * Date :  24/08/15
 * Version : v1
 */
public class ThreadPoolMultiThreadedTest {

    /**
     * Tests pool size and map size after submitting many tasks
     */
    @Test
    public void expectThreadPoolSizeWorksAfterSubmittingManyTasks() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);

        Runnable r = new Runnable() {
            @Override
            public void run() {

                //submit 20 tasks to thread pool
                for (int i = 0; i < 20; i++) {
                    pool.submit(Thread.currentThread().getName() + " " + String.valueOf(i), new Runnable() {
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

                Assert.assertEquals(10, pool.poolSize());
            }
        };

        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        Thread t3 = new Thread(r);

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        Assert.assertEquals(10, pool.poolSize());
        Assert.assertEquals(10, pool.map.size());

        pool.shutdown();

        Assert.assertEquals(0, pool.map.size());
    }

    /**
     * Tests if shutdown works and makes sure no job submission is allowed after shutdown.
     */
    @Test
    public void expectShutdownWorks() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);

        Runnable r = new Runnable() {
            @Override
            public void run() {

                //submit 20 tasks to thread pool
                for (int i = 0; i < 20; i++) {
                    pool.submit(Thread.currentThread().getName() + " " + String.valueOf(i), new Runnable() {
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

                Assert.assertEquals(10, pool.poolSize());
            }
        };

        Thread t1 = new Thread(r);
        t1.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

                Assert.assertTrue(e instanceof RejectedExecutionException);

            }
        });
        Thread t2 = new Thread(r);
        t2.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

                Assert.assertTrue(e instanceof RejectedExecutionException);

            }
        });
        Thread t3 = new Thread(r);
        t3.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

                Assert.assertTrue(e instanceof RejectedExecutionException);

            }
        });

        t1.start();
        t2.start();
        t3.start();

        pool.shutdown();

        try {
            t1.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        try {
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tests if Jobs with same JobId, gets executed with the same thread.
     */
    @Test
    public void expectJobAffinityWorks() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);

        final String[][] results = new String[3][20];

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final String job = String.valueOf(i);
                    pool.submit(job, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            results[0][Integer.valueOf(job)] = "Executed by " + Thread.currentThread().getName();
                        }
                    });
                }

                Assert.assertEquals(10, pool.poolSize());
            }
        };

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final String job = String.valueOf(i);
                    pool.submit(job, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            results[1][Integer.valueOf(job)] = "Executed by " + Thread.currentThread().getName();
                        }
                    });
                }

                Assert.assertEquals(10, pool.poolSize());
            }
        };

        Runnable r3 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final String job = String.valueOf(i);
                    pool.submit(job, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            results[2][Integer.valueOf(job)] = "Executed by " + Thread.currentThread().getName();
                        }
                    });
                }

                Assert.assertEquals(10, pool.poolSize());
            }
        };


        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r3);

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pool.shutdown();

        for (int i = 0; i < 20; i++) {

            Assert.assertEquals(results[0][i], results[1][i]);
            Assert.assertEquals(results[2][i], results[1][i]);

        }

    }

    /**
     * Tests if execution order is maintained as per the job submission order
     */
    @Test
    public void expectJobOrderWorks() {

        final ThreadPoolWithJobAffinityExecutor pool = new ThreadPoolWithJobAffinityExecutor(10);
        final long[][] results = new long[3][20];
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final int job = i;
                    pool.submit("MyJob", new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            results[0][job] = System.nanoTime();
                        }
                    });
                }

                Assert.assertEquals(10, pool.poolSize());
            }
        };

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final int job = i;
                    pool.submit("MyJob", new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            results[1][job] = System.nanoTime();
                        }
                    });
                }

                Assert.assertEquals(10, pool.poolSize());
            }
        };

        Runnable r3 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final int job = i;
                    pool.submit("MyJob", new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            results[2][job] = System.nanoTime();
                        }
                    });
                }

                Assert.assertEquals(10, pool.poolSize());
            }
        };


        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r3);

        t1.start();
        t2.start();
        t3.start();


        try {
            t1.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        try {
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        try {
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pool.shutdown();

        for (int j = 0; j < 3; j++) {
            int prev = 0;
            for (int i = 1; i < 20; i++) {
                Assert.assertTrue(results[j][i] >= results[j][prev]);
                prev = i;
            }
        }

    }
}
