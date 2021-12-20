package org.lkpnotice.infra.introspection;

import com.carrotsearch.sizeof.RamUsageEstimator;
/*import net.sourceforge.sizeof.SizeOf;*/

import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by jpliu on 2020/5/6.
 */
public class Test {
    public static void main(String[] args){
        test0();
        test2();
    }


    static void test0(){
        /*System.out.println(SizeOf.deepSizeOf(new HashSet<>()));
        System.out.println(SizeOf.deepSizeOf(new ConcurrentSkipListSet<>()));*/
    }


    static void test1(){
/*        System.out.println(SizeOf.deepSizeOf(new HashSet<>()));
        System.out.println(SizeOf.deepSizeOf(new ConcurrentSkipListSet<>()));*/
    }

    static void test2(){
/*        System.out.println(RamUsageEstimator.sizeOf(new HashSet<>()));
        System.out.println(RamUsageEstimator.sizeOf(new ConcurrentSkipListSet<>()));*/
    }


    static void test3(){
    /*    System.out.println(SizeOf.deepSizeOf(new A()));
        System.out.println(SizeOf.deepSizeOf(new C()));*/
    }



    static class A {
        int a=0;
    }

    static class B {
        int b=0;
    }

    static class C extends B{
    }
}
