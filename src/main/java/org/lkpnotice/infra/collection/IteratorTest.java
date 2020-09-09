package org.lkpnotice.infra.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jpliu on 2020/9/7.
 * while do etl style job ,  from a collection , with one transformation , to a new collection ,
 * one stype is
 * for(i =0; i< collection.size() ;i++){
 *     put into new collection
 * }
 *
 *
 */
public class IteratorTest {
    public static void main(String[] args){
        List<String> list = new ArrayList<>();
        list.add("ttt");
        list.add("ttt");
        list.add("ttt");
        list.add("ttt");

        Iterator<String> iter = list.iterator();
        while(iter.hasNext()){
            String ele = iter.next();
            iter.remove();

            System.out.println(" size " + list.size());
        }
    }
}
