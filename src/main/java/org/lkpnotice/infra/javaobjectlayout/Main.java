package org.lkpnotice.infra.javaobjectlayout;

import org.openjdk.jol.info.ClassLayout;

/**
 * Created by jpliu on 2020/5/6.
 * https://www.cnblogs.com/katsu2017/p/12610002.html
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        test4();
    }


    /*1.无锁状态*/
    public static void test1(){
        Object object = new Object();
        System.out.println("hash: " + object.hashCode());
        System.out.println(ClassLayout.parseInstance(object).toPrintable());
    }


    public static void test2() throws InterruptedException {
//        复制代码
        Thread.sleep(5000); //等待jvm开启偏向锁
        Object o = new Object();
        System.out.println(ClassLayout.parseInstance(o).toPrintable());

        synchronized (o){
            System.out.println(ClassLayout.parseInstance(o).toPrintable());
        }
    }

    public static void test3() throws InterruptedException {
        Thread.sleep(5000);
        Object o = new Object();
        synchronized (o) {
            System.out.println(ClassLayout.parseInstance(o).toPrintable());
        }
        for (int i = 0; i < 1; i++) {
            Thread t = new Thread(() -> {
                print(o);
            });
            t.start();
        }
    }


    public static void test4(){
        Object o = new Object();
        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(() -> {
                print(o);
            });
            t.start();
        }
    }


    public static void print(Object o) {
        synchronized (o){
            System.out.println(ClassLayout.parseInstance(o).toPrintable());
        }
    }
}
