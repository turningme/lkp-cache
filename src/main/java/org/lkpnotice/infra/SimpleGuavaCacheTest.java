package org.lkpnotice.infra;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2019/10/15.
 */
public class SimpleGuavaCacheTest {



    public static void main(String[] args) throws ExecutionException {
        System.out.println(Thread.currentThread().getName());

        CacheLoader t = new CacheLoader<Integer, Integer>() {
            @Override
            public Integer load(Integer key) throws Exception {
                System.out.println(Thread.currentThread().getName());
                return -1;
            }
        };

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L,
                                                                       TimeUnit.MILLISECONDS,
                                                                       new LinkedBlockingQueue<>(10));
        ListeningExecutorService  cacheLoaderExecutorService = MoreExecutors.listeningDecorator(threadPoolExecutor);


        long maximumSize = 2;
        LoadingCache<Integer, Integer> guavaCache = CacheBuilder.newBuilder().maximumSize(maximumSize)
                .removalListener(new RemovalListener<Integer, Integer>() {
                    @Override
                    public void onRemoval(RemovalNotification<Integer, Integer> removalNotification) {

                        if (RemovalCause.EXPIRED.equals(removalNotification.getCause()) ||
                                RemovalCause.EXPLICIT.equals(removalNotification.getCause()) ||
                                RemovalCause.SIZE.equals(removalNotification.getCause())) {
                            System.out.printf("remove key %s \n",removalNotification.getKey());
                            System.out.println("Remove thread " + Thread.currentThread().getName());
                        }
                    }
                }).recordStats()
                .build(t);




        for (int i=0; i< 10 ; i ++){
            System.out.printf("input key is %s \n", i);
            guavaCache.get(i);
            guavaCache.put(i,i);
        }




        guavaCache.invalidateAll();

        System.out.println(guavaCache.stats().toString());
    }
}
