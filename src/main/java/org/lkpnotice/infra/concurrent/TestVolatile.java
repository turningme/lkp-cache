package org.lkpnotice.infra.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2019/11/18.
 */
public class TestVolatile {
    volatile static String aa;

    public static void main(String[] args) throws InterruptedException {
        aa = " init ";
        Thread one = new Thread(new Runnable() {
            @Override
            public void run() {
            /*    try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                aa = "modified";
            }
        });


        Thread two = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(" result is " + aa);
            }
        });


        two.start();
        one.start();

        two.join();
        one.join();


    }


}
