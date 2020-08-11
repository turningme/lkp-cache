package org.lkpnotice.infra.memory;

import java.lang.ref.SoftReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2020/8/5.
 */
public class MemoryReuse {
    public static void main(String[] args) throws InterruptedException {
        test2_pooled();
    }



    static void test2_notpooled() throws InterruptedException {
        long start = System.nanoTime();
        ObjectHolder objectHolder = null;
        for (int i = 0; i< 1_000_000 ; i++){
            objectHolder = new ObjectHolder(new RealObject());
            objectHolder.getRealObject().doSomething();
            objectHolder = null;
        }

        long end = System.nanoTime();
        System.out.println("end-start = " + (end- start));
    }

    static void test2_pooled() throws InterruptedException {
        long start = System.nanoTime();

        ObjectManager objectManager = new ObjectManager();
        ObjectHolder objectHolder = null;
        for (int i = 0; i< 1_000_000 ; i++){
            objectHolder = objectManager.allocate();
            objectHolder.getRealObject().doSomething();
        }

        long end = System.nanoTime();
        System.out.println("end-start = " + (end- start));
    }


    static void test1() {
        ObjectManager objectManager = new ObjectManager();


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {

                        ObjectHolder objectHolder = objectManager.allocate();
                        TimeUnit.SECONDS.sleep(1);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("---- " + e);
                    }
                }
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("*******  pool size is " + objectManager.getSize());
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t1.start();
        t2.start();

    }


    static class ObjectHolder {
        SoftReference<RealObject> realObject;
        ObjectManager objectManager;

        public ObjectHolder(ObjectManager objectManager, RealObject realObject) {
            this.objectManager = objectManager;
            this.realObject = new SoftReference<>(realObject);
        }

        public ObjectHolder(RealObject realObject) {
            this.realObject = new SoftReference<>(realObject);
        }

        public RealObject getRealObject() {
            return realObject.get();
        }

        public void setRealObject(RealObject realObject) {
            this.realObject = new SoftReference<>(realObject);
        }



        @Override
        protected void finalize() throws Throwable {
            super.finalize();
//            System.out.println("revoke real object ");
            try {
                if (realObject != null){
                    objectManager.revoke(realObject.get());
                }
            }catch (Exception e){
                System.out.println("eeeee ");
            }
        }
    }

    static class ObjectManager {
        BlockingQueue<RealObject> deque;

        public ObjectManager() {
            deque = new ArrayBlockingQueue<>(1000);
            for (int i = 0; i < 500; i++) {
                deque.add(new RealObject());
            }
        }

        public ObjectHolder allocate() throws InterruptedException {
            RealObject realObject = null;
            if (deque.isEmpty()) {
                realObject = new RealObject();
            } else {
                realObject = deque.take();
            }
            return new ObjectHolder(this,realObject);
        }

        public void revoke(RealObject realObject) {
            if (deque.size() < 500 && realObject !=null) {
                deque.offer(realObject);
            }else {
                realObject = null;
            }
        }

        public int getSize() {
            return deque.size();
        }

    }

    static class RealObject {
        byte[] data;

        public RealObject() {
            data = new byte[1 * 1024  ];
        }

        public void reset() {
            //
        }

        public void doSomething(){

        }
    }
}
