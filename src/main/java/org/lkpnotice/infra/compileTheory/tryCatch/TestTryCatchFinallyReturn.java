package org.lkpnotice.infra.compileTheory.tryCatch;

/**
 * Created by jpliu on 2020/5/22.
 */
public class TestTryCatchFinallyReturn {
    public static void main(String[] args){
        int result1 = testTryReturnFinallyReturn();
        System.out.println(result1);


        System.out.println(testTryReturnFinallyReturn2());
    }







    public static int testTryReturnFinallyReturn(){

        int i = 0;
        try {
            i = 1;
            return i;
        }finally {
            i = 2;
            return i;
        }
    }


    public static XX testTryReturnFinallyReturn2(){

        XX xx = new XX();
        try {
            xx.a = 1;
            xx.b = 1;
            return xx;
        }finally {
            xx.a = 10;
        }
    }


    static class XX {
        public  int a, b;

        @Override
        public String toString() {
            return "XX{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }
    }
}
