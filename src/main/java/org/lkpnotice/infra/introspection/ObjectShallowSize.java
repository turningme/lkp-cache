package org.lkpnotice.infra.introspection;

import java.lang.instrument.Instrumentation;

/**
 * Created by jpliu on 2020/5/6.
 */
public class ObjectShallowSize {
    private static Instrumentation inst;

    public static void premain(String agentArgs, Instrumentation instP){
        inst = instP;
    }

    public static long sizeOf(Object obj){
        return inst.getObjectSize(obj);
    }

    public static void main(String[] args){
        System.out.println(ObjectShallowSize.sizeOf(new ObjectA()));
    }



    private static class ObjectA {
        String str;  // 4
        int i1; // 4
        byte b1; // 1
        byte b2; // 1
        int i2;  // 4
        ObjectB obj; //4
        byte b3;  // 1
    }
    private static class ObjectB {

    }

}
