package org.lkpnotice.infra.concurrent.atomic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by jpliu on 2021/1/4.
 */
public class AtomicStampedReferenceTest {
    private final static String A = "A";
    private final static String B = "B";
    private static AtomicInteger ai = new AtomicInteger(1);
    private final static AtomicStampedReference<String> ar = new AtomicStampedReference<>(A, 1);
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Thread.sleep(Math.abs((int) (Math.random() * 100)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ar.compareAndSet(A, B, 1,2)) {
                System.out.println("我是线程1,我成功将A改成了B");
            }
        }).start();
        new Thread(() -> {
            if (ar.compareAndSet(A, B, ai.get(),ai.incrementAndGet())) {
                System.out.println("我是线程2,我成功将A改成了B");
            }
        }).start();
        new Thread(() -> {
            if (ar.compareAndSet(B, A, ai.get(),ai.incrementAndGet())) {
                System.out.println("我是线程3,我成功将B改成了A");
            }
        }).start();
    }
}
