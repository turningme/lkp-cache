package org.lkpnotice.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jpliu on 2019/10/15.
 */
public class TT1 {
    AtomicLong counterIn = new AtomicLong(0);
    AtomicLong counterOut = new AtomicLong(0);

    Random random = new Random();
    int bound = 10000;
    long maxSize = 1;


    Cache<Integer,Integer> caffeineCache;
    String name;

    public TT1(int bound, Random random) {
        this.bound = bound;
        this.random = random;
        init();
    }


    public TT1(int bound, long maxSize, Random random) {
        this.bound = bound;
        this.maxSize = maxSize;
        this.random = random;
        init();
    }

    public TT1(int bound, long maxSize, String name, Random random) {
        this.bound = bound;
        this.maxSize = maxSize;
        this.name = name;
        this.random = random;
        init();
    }

    public void init(){
/*        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .removalListener(new RemovalListener<Integer, Integer>() {
                    public void onRemoval(Integer k, Integer v, RemovalCause cause) {
                        boolean shouldRemove = RemovalCause.EXPIRED.equals(cause) ||
                                RemovalCause.EXPLICIT.equals(cause) ||
                                RemovalCause.SIZE.equals(cause);

                        if (shouldRemove){
                            counterOut.incrementAndGet();
                        }

                    }
                }).build();*/


                this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .writer(new CacheWriter<Integer, Integer>() {
                    public void delete(Integer key, Integer value, RemovalCause cause) {
                        boolean shouldRemove = RemovalCause.EXPIRED.equals(cause) ||
                                RemovalCause.EXPLICIT.equals(cause) ||
                                RemovalCause.SIZE.equals(cause);
                        if (shouldRemove){
                            counterOut.incrementAndGet();
                        }
                    }

                    public void write(Integer key, Integer value) {

                    }
                }).build();
    }


    public void process(){
        int digits = random.nextInt(bound);
        caffeineCache.put(digits,1);
        counterIn.incrementAndGet();
    }


    public void process(Integer k, Integer v){
        caffeineCache.put(k,v);
        counterIn.incrementAndGet();
    }


    public void complete(){
        this.caffeineCache.invalidateAll();
//        this.caffeineCache.cleanUp();
    }


    public void printInfo(){
        System.out.printf("name [%s] information is inputCnt=[%s], outPutCnt=[%s]  , maximumSize = [%s] \n", this.name,this.counterIn.get(), this.counterOut.get(), this.maxSize);
    }
}
