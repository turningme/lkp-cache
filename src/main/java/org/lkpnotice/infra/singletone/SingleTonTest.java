package org.lkpnotice.infra.singletone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpliu on 2019/11/18.
 */
public class SingletonTest {
    public String content ;
    public static Integer mutex = new Integer(0);

    private SingletonTest() {
        content = "hello word ";
    }

    public void printInfo(){
        System.out.println(Thread.currentThread().getName() +  "  " + content);
    }

    private static SingletonTest singletonTest = null;

    public static SingletonTest getSingletonTest() {
        if (singletonTest == null) {
            // 若singletonTest为空，则加锁，再进一步判空
            synchronized (mutex) {
                // 再判断一次是否为null
                if (singletonTest == null) {
                    //若为空，则创建一个新的实例
                    singletonTest = new SingletonTest();
                }
            }
        }
        return singletonTest;
    }




    public static void main(String[] args) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        for (int i=0; i< 100 ; i++){
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    SingletonTest.getSingletonTest().printInfo();
                }
            }, "name" + i));

        }

        for (Thread t : threadList
             ) {
            t.start();
        }

        for (Thread t : threadList
                ) {
            t.join();
        }

    }


}
