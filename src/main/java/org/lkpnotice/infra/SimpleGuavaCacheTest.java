package org.lkpnotice.infra;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Created by jpliu on 2019/10/15.
 */
public class SimpleGuavaCacheTest {



    public static void main(String[] args){

        long maximumSize = 3;
        Cache<Integer, Integer>  guavaCache = CacheBuilder.newBuilder().maximumSize(maximumSize)
                .removalListener(new RemovalListener<Integer, Integer>() {
                    public void onRemoval(RemovalNotification<Integer, Integer> removalNotification) {

                        if (RemovalCause.EXPIRED.equals(removalNotification.getCause()) ||
                                RemovalCause.EXPLICIT.equals(removalNotification.getCause()) ||
                                RemovalCause.SIZE.equals(removalNotification.getCause())) {
                            System.out.printf("remove key %s \n",removalNotification.getKey());

                        }
                    }
                })
                .build();



        for (int i=0; i< 10 ; i ++){
            System.out.printf("input key is %s \n", i);
            guavaCache.put(i,i);
        }


        guavaCache.invalidateAll();

    }
}
