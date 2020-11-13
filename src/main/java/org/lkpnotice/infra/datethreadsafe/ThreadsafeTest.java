package org.lkpnotice.infra.datethreadsafe;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by jpliu on 2020/9/12.
 */
public class ThreadsafeTest {
    public static void main(String[] args) throws InterruptedException {
        isThreadSafeCase();

    }


    static void isThreadSafeCase(){
        ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 0,
                                                         TimeUnit.MILLISECONDS,
                                                         new ArrayBlockingQueue<>(20),
                                                         new ThreadFactoryBuilder().setNameFormat("example-pool-%d").build(),
                                                         new ThreadPoolExecutor.CallerRunsPolicy());
//        System.out.print(System.currentTimeMillis());

        final long millisBase = 1599870640351L;
        long firstDayofMonthExpect =  DateUtil.firstDayMonthSafeLocal(millisBase,DateUtil.UTC_TIMEZONE);
        System.out.println("firstDayofMonthExpect = " + firstDayofMonthExpect);


        for (int i=0 ; i< 10 ;i++){
            pool.submit(()->{
                long threadMillis = millisBase;
                Map<Long,Long> sets = new HashMap();
                while (true){
                    threadMillis++;
                    long dof = DateUtil.firstDayMonthSafeLocal(threadMillis,DateUtil.UTC_TIMEZONE);
                    sets.put(dof,threadMillis);

                    if (sets.size() >1){

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("stopping ThreadName="+ Thread.currentThread().getName());
                        sets.forEach((k,v)->{
                            //System.out.println(String.format(" key=%s, value=%s",k,v));
                            stringBuilder.append(String.format(" key=%s, value=%s",k,v));
                        });
                        System.out.println(stringBuilder.toString());

                        break;
                    }
                }


                return  null;
            });
        }


        //TimeUnit.SECONDS.sleep(120);
        System.out.println("shutdown");
        pool.shutdown();



        try {

            System.out.println("awaitTermination");
            while (!pool.awaitTermination(10,TimeUnit.SECONDS)){
                System.out.println("awaitTermination");

            }
        }catch (Exception e){

        }finally {
            System.out.println("shutdownNow");
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    static void notThreadSafeCase(){
        ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 0,
                                                         TimeUnit.MILLISECONDS,
                                                         new ArrayBlockingQueue<>(20),
                                                         new ThreadFactoryBuilder().setNameFormat("example-pool-%d").build(),
                                                         new ThreadPoolExecutor.CallerRunsPolicy());
//        System.out.print(System.currentTimeMillis());

        final long millisBase = 1599870640351L;
        long firstDayofMonthExpect =  DateUtil.firstDayMonth(millisBase,DateUtil.UTC_TIMEZONE);
        System.out.println("firstDayofMonthExpect = " + firstDayofMonthExpect);


        for (int i=0 ; i< 10 ;i++){
            pool.submit(()->{
                long threadMillis = millisBase;
                Map<Long,Long> sets = new HashMap();
                while (true){
                    threadMillis++;
                    long dof = DateUtil.firstDayMonth(threadMillis,DateUtil.UTC_TIMEZONE);
                    sets.put(dof,threadMillis);

                    if (sets.size() >1){

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("stopping ThreadName="+ Thread.currentThread().getName());
                        sets.forEach((k,v)->{
                            //System.out.println(String.format(" key=%s, value=%s",k,v));
                            stringBuilder.append(String.format(" key=%s, value=%s",k,v));
                        });
                        System.out.println(stringBuilder.toString());

                        break;
                    }
                }


                return  null;
            });
        }


        //TimeUnit.SECONDS.sleep(120);
        System.out.println("shutdown");
        pool.shutdown();



        try {

            System.out.println("awaitTermination");
            while (!pool.awaitTermination(10,TimeUnit.SECONDS)){
                System.out.println("awaitTermination");

            }
        }catch (Exception e){

        }finally {
            System.out.println("shutdownNow");
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }
}
