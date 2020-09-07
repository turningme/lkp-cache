package org.lkpnotice.infra.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2020/9/7.
 */
public class TestThreadPoolConstructor {


    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS, queue,
                                                                       new ThreadFactoryBuilder().setNameFormat("test-pool-%d").build(),
                                                                       new ThreadPoolExecutor.CallerRunsPolicy());


        System.out.println("Start ... ");
        for (int i = 0; i < 1_000; i++) {
            threadPoolExecutor.submit(new Ts(i));
        }


        threadPoolExecutor.shutdown();
        threadPoolExecutor.shutdownNow();
    }


    static class Ts implements Runnable {
        int i;

        public Ts(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            System.out.println("i = " + i  + " thread " + Thread.currentThread().getName());
        }
    }
}
