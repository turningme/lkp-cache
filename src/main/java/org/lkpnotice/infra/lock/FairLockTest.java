package org.lkpnotice.infra.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jpliu on 2020/7/22.
 */
public class FairLockTest {
    // 分别设置公平锁和非公平锁，分析打印结果
    private static ReentrantLock lock = new ReentrantLock(true);


    private static Runnable runnable = () -> {
        for (int i = 0 ;i<10 ;i++) {
            try {
                lock.lock();
                System.out.println(Thread.currentThread().getName() + " 获取了锁");
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    };


    public static void main(String[] args) {
        Thread thread1 = new Thread(runnable, "thread---1");
        Thread thread2 = new Thread(runnable, "thread---2");
        Thread thread3 = new Thread(runnable, "thread---3");
        Thread thread4 = new Thread(runnable, "thread---4");
        Thread thread5 = new Thread(runnable, "thread---5");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
    }
}
