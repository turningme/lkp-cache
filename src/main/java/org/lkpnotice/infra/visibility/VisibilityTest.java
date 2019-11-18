package org.lkpnotice.infra.visibility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2019/11/18.
 */
public class VisibilityTest {
    private String a;
    private String b;
    private String c;

    Integer mutex = new Integer(1);

    public void syncWriteAndSleep(){
//        synchronized (mutex){
            a = "aa";
            b = "bb";
            c = "cc";
            try {
                TimeUnit.SECONDS.sleep(10);
                System.out.println("sleep complete ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }

    }


    public void printInfo(){
//        synchronized (mutex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(a).append("-");
            stringBuilder.append(b).append("-");
            stringBuilder.append(c).append("-");
            System.out.println(stringBuilder.toString() + "  " + Thread.currentThread().getName());
//        }
    }


    public static void main(String[] args) throws InterruptedException {
        VisibilityTest visibilityTest = new VisibilityTest();

        Thread producer = new Thread(new Runnable() {
            @Override
            public void run() {
                visibilityTest.syncWriteAndSleep();
            }
        });

        producer.setDaemon(true);


        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i< 10 ; i++){
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        visibilityTest.printInfo();
                        try {
                            TimeUnit.MILLISECONDS.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            } , "name-"+i));
        }



        for (Thread t :threadList
             ) {
            t.start();
        }

        TimeUnit.SECONDS.sleep(50);
        producer.start();

        for (Thread t :threadList
                ) {
            t.join();
        }
    }

}
