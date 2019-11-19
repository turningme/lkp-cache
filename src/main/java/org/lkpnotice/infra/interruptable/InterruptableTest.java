package org.lkpnotice.infra.interruptable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by jpliu on 2019/11/19.
 */
public class InterruptableTest {


    public static void main(String[] args){
        testWait();
    }


    static void testPark(){
        Thread t = new Thread(new TaskParkDemo());
        t.start();


        try {
            System.out.println("before sleep");
            TimeUnit.SECONDS.sleep(10);
            System.out.println("after sleep");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t.interrupt();
    }


    static void testWait(){
        Thread t = new Thread(new TaskWaitDemo());
        t.start();


        try {
            System.out.println("before sleep");
            TimeUnit.SECONDS.sleep(10);
            System.out.println("after sleep");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t.interrupt();
    }

    static void testSleep(){
        Thread t = new Thread(new TaskSleepDemo());
        t.start();


        try {
            System.out.println("before sleep");
            TimeUnit.SECONDS.sleep(10);
            System.out.println("after sleep");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t.interrupt();
    }




    public static class TaskParkDemo implements Runnable{
        @Override
        public void run() {
            System.out.println("Before suspend interrupt = " + Thread.currentThread().isInterrupted());
            LockSupport.park();
            System.out.println("After suspend interrupt = " + Thread.currentThread().isInterrupted());
        }
    }


    public static class TaskSleepDemo implements Runnable{
        @Override
        public void run() {
            System.out.println("Before sleep interrupt = " + Thread.currentThread().isInterrupted());
            try {
                TimeUnit.SECONDS.sleep(50000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            System.out.println("After sleep interrupt = " + Thread.currentThread().isInterrupted());
        }
    }


    static Integer mutex = new Integer(1);
    public static class TaskWaitDemo implements Runnable{
        @Override
        public void run() {
            System.out.println("Before wait interrupt = " + Thread.currentThread().isInterrupted());
            try {
                synchronized (mutex){
                    mutex.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            System.out.println("After wait interrupt = " + Thread.currentThread().isInterrupted());
        }
    }


}
