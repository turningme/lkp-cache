package org.lkpnotice.infra.time;

/**
 * Created by jpliu on 2020/7/30.
 */
public class TimeMain {
    static final int LOOP_TIMES = 100_000_000;

    public static void main(String[] args){
        for (int i=0;  i< 5 ; i++){
            testNanoSeconds();
        }

        for (int i=0;  i< 5 ; i++){
            testMillSeconds();
        }
    }



    public static void testMillSeconds(){
        long startTime = System.nanoTime();

        for(int i = 0; i < LOOP_TIMES; i++) {
            long test = System.currentTimeMillis();
        }

        long endTime = System.nanoTime();

        System.out.println("Total time: "+(endTime-startTime)/1000_000);
    }

    public static void testNanoSeconds(){
        long startTime = System.nanoTime();

        for(int i = 0; i < LOOP_TIMES; i++) {
            long test = System.nanoTime();
        }

        long endTime = System.nanoTime();

        System.out.println("Total time: "+(endTime-startTime)/1000_000);
    }
}
