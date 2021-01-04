package org.lkpnotice.infra.concurrent.atomic;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by jpliu on 2021/1/4.
 */
public class AtomicMarkableReferenceTest {
    private final static String A = "A";
    private final static String B = "B";
    private final static AtomicMarkableReference<String> ar = new AtomicMarkableReference<>(A, false);

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Thread.sleep(Math.abs((int) (Math.random() * 100)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ar.compareAndSet(A, B, false, true)) {
                System.out.println("我是线程1,我成功将A改成了B");
            }
        }).start();
        new Thread(() -> {
            if (ar.compareAndSet(A, B, false, true)) {
                System.out.println("我是线程2,我成功将A改成了B");
            }
        }).start();
        new Thread(() -> {
            if (ar.compareAndSet(B, A, ar.isMarked(), true)) {
                System.out.println("我是线程3,我成功将B改成了A");
            }
        }).start();
    }
}
