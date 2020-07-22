package org.lkpnotice.infra.lock;

/**
 * Created by jpliu on 2020/7/22.
 */
public class MonitorLockPrint {
    int i = 1;

    public static void main(String[] args) {

        MonitorLockPrint obj = new MonitorLockPrint();
        // 使用匿名内部类的形式
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (obj.i < 10) {

                    synchronized (this) {
                        this.notify();
                        System.out.println("Thread " + Thread.currentThread().getName() + ":" + obj.i++);
                        try {
                            Thread.sleep(100); // 休眠100毫秒，放大线程差异
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        new Thread(runnable).start();
        new Thread(runnable).start();

    }


}
