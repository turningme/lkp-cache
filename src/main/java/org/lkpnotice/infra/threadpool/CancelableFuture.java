package org.lkpnotice.infra.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by jpliu on 2020/7/7.
 */
public class CancelableFuture {
    public static void main(String[] args){
        cancelValidTask();
    }



    static void cancelRunningTask(){
        ExecutorService executors = Executors.newSingleThreadExecutor();
        FutureTask<?> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                for(int i=0;i<10000;i++){
                    System.out.println(i);
                }
                return null;
            }
        });
        executors.execute(futureTask);
        System.out.println("futureTask start");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        futureTask.cancel(true);
        System.out.println("futureTask cancel");


//        try {
//            executors.awaitTermination(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        executors.shutdown();
    }


    static void cancelValidTask(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<?> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                for(int i=0;i<10000&&!Thread.currentThread().isInterrupted();i++){
                    System.out.println(i);
                }
                return null;
            }
        });
        executor.execute(futureTask);
        System.out.println("futureTask start");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        futureTask.cancel(true);
        System.out.println("futureTask cancel");

        executor.shutdown();
    }

}
