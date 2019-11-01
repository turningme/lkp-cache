package org.lkpnotice.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jpliu on 2019/10/15.
 */
public class SimpleCaffeineCacheTest {

    static AtomicLong counterIn = new AtomicLong(0);
    static AtomicLong counterOut = new AtomicLong(0);

    static Random random = new Random();
    static int bound = 10000;


    public static void main(String[] args){
        test0();
    }


    static void test3(){

        int ttCount = 10;


        List<TT1> ttList = new ArrayList();
        for (int i=0 ; i < ttCount ;i++){
            String name = String.format("ttCnt-%s",i);
            long maxSize = random.nextInt(5000) + 2000;
            if (maxSize > 5000){
                maxSize = 5000;
            }
            ttList.add(new TT1(bound,2,name,random));
        }

        ttList.add(new TT1(bound,1000,"tt-111",random));

        for (int i=0; i< 1000000 ; i ++){
            for (TT1 tt1:ttList
                    ) {
                int digits = random.nextInt(bound);
                digits = i % 1000;
                tt1.process(digits,1);
            }
        }

        for (TT1 tt1:ttList
                ) {
            tt1.complete();
        }


//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        for (TT1 tt1:ttList
                ) {
            tt1.printInfo();
        }


    }

    static void test2(){
        int ttCount = 10;


        List<TT1> ttList = new ArrayList();
        for (int i=0 ; i < ttCount ;i++){
            String name = String.format("ttCnt-%s",i);
            long maxSize = random.nextInt(5000) + 2000;
            if (maxSize > 5000){
                maxSize = 5000;
            }
            ttList.add(new TT1(bound,maxSize,name,random));
        }

        ttList.add(new TT1(bound,2000,"tt-111",random));

        for (int i=0; i< 1000000 ; i ++){
            for (TT1 tt1:ttList
                    ) {
                int digits = random.nextInt(bound);
                digits = i % 1000;
                tt1.process(digits,1);
            }
        }

        for (TT1 tt1:ttList
                ) {
            tt1.complete();
        }



        for (TT1 tt1:ttList
                ) {
            tt1.printInfo();
        }
    }


    static void test1(){
        Cache<Integer,Integer> caffeineCache = Caffeine.newBuilder()
                .maximumSize(200)
                .removalListener(new RemovalListener<Integer, Integer>() {
                    public void onRemoval(Integer k, Integer v, RemovalCause removalCause) {
//                        System.out.printf("remove key %s \n",k);
                        counterOut.incrementAndGet();
                    }
                }).build();

        for (int i=0; i< 100000 ; i ++){
//            System.out.printf("input key is %s \n", i);
            int digits = random.nextInt(bound);
            caffeineCache.put(digits,1);
            counterIn.incrementAndGet();
        }


        caffeineCache.invalidateAll();

        System.out.printf("stats input count [%s] , output count [%s]", counterIn.get(), counterOut.get());

    }


    static void test0(){
        long maximumSize = 3;
        Cache<Integer,Integer> caffeineCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .removalListener(new RemovalListener<Integer, Integer>() {
                    public void onRemoval(Integer k, Integer v, RemovalCause removalCause) {
                        System.out.printf("remove key %s \n",k);
                    }
                }).build();

        for (int i=0; i< 10 ; i ++){
            System.out.printf("input key is %s \n", i);
            caffeineCache.put(i,i);
        }


        caffeineCache.invalidateAll();

//        System.out.printf("stats input count [%s] , output count [%s]", counterIn.get(), counterOut.get());

    }


}
