package org.lkpnotice.infra.benchmark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Created by jpliu on 2019/11/18.
 */
public class Test {

    static String[] stringArray;
    static ArrayList<String> arrayList = new ArrayList<>();
    static LinkedList<String> linkedList = new LinkedList<>();
    static HashSet<String> hashSet = new HashSet<>();
    static LinkedHashSet<String> linkedHashSet = new LinkedHashSet();
    static int NUM = 10_000_000;

    public static void main(String[] args){
        setup();


        execArrays(NUM);
        System.out.println();
        execArray(NUM);
        execLinkedList(NUM);
        execHashSet(NUM);
        execLinkedSet(NUM);


        System.out.println();
        execArrayNative(NUM);
        execLinkedListNative(NUM);
        execHashSetNative(NUM);
        execLinkedSetNative(NUM);
    }


    static void setup(){
        int NUM = 10;
        stringArray = new String[NUM];
        for (int i=0 ;i < NUM ; i++){
            String tmp = "name is " + i;
            arrayList.add(tmp);
            linkedList.add(tmp);
            hashSet.add(tmp);
            linkedHashSet.add(tmp);

            stringArray[i] = tmp;
        }
    }


    static void execArrays(int num){
        long start = System.nanoTime();
        for (String ele : stringArray) {
            int a = 1;
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }



    static void execArrayNative(int num){
        long start = System.nanoTime();
        for (String ele : arrayList) {
            int a = 1;
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }


    static void execArray(int num){
        long start = System.nanoTime();
        for (int i = 0; i < num; i++) {
            Iterator<String> iter = arrayList.iterator();
            while (iter.hasNext()){
                iter.next();
                int a = 1;
            }
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }

    static void execLinkedListNative(int num){
        long start = System.nanoTime();

        for (String ele: linkedList) {
                int a = 1;
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }

    static void execLinkedList(int num){
        long start = System.nanoTime();

        for (int i = 0; i < num; i++) {
            Iterator<String> iter = linkedList.iterator();
            while (iter.hasNext()){
                iter.next();
                int a = 1;
            }
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }


    static void execHashSet(int num){
        long start = System.nanoTime();

        for (int i = 0; i < num; i++) {
            Iterator<String> iter = hashSet.iterator();
            while (iter.hasNext()){
                iter.next();
                int a = 1;
            }
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }

    static void execHashSetNative(int num){
        long start = System.nanoTime();

        for (String ele:hashSet) {
            int a = 1 ;
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }

    static void execLinkedSetNative(int num){
        long start = System.nanoTime();

        for (Object ele: linkedHashSet) {
            int a = 1;
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }

    static void execLinkedSet(int num){
        long start = System.nanoTime();
        for (int i = 0; i < num; i++) {
            Iterator<String> iter = linkedHashSet.iterator();
            while (iter.hasNext()){
                iter.next();
                int a = 1;
            }
        }

        long end = System.nanoTime();
        System.out.println(" time = "  + (end-start) );
    }
}
