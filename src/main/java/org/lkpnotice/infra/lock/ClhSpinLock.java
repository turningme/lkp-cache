package org.lkpnotice.infra.lock;

import sun.misc.Contended;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by jpliu on 2019/11/24.
 */
@Contended
public class ClhSpinLock {
    private final ThreadLocal<Node> prev;
    private final ThreadLocal<Node> node;
    private final AtomicReference<Node> tail = new AtomicReference<Node>(new Node());

    public ClhSpinLock() {
        this.node = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };

        this.prev = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return null;
            }
        };
    }

    public void lock() {
        final Node node = this.node.get();
        node.locked = true;
        // 一个CAS操作即可将当前线程对应的节点加入到队列中，
        // 并且同时获得了前继节点的引用，然后就是等待前继释放锁
        Node pred = this.tail.getAndSet(node);
        this.prev.set(pred);
        while (pred.locked) {// 进入自旋
        }
    }

    public void unlock() {
        final Node node = this.node.get();
        node.locked = false;
        this.node.set(this.prev.get());
    }

    private static class Node {
        private volatile boolean locked;
    }



    public static void main(String[] args) throws InterruptedException {
        final ClhSpinLock lock = new ClhSpinLock();
        lock.lock();

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    System.out.println(Thread.currentThread().getName() + " acquired the lock!");
                    lock.unlock();
                }
            } , String.format("name-%s ", i)).start();
            Thread.sleep(1000);
        }

        System.out.println("main thread unlock!");
        lock.unlock();
    }
}
