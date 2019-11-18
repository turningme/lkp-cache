package org.lkpnotice.infra.singletone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpliu on 2019/11/18.
 */
public class VolatileTest {
    public static Integer mutex = new Integer(0);
    public static String a1 ;
    public static String a2 ;
    public static String a3 ;
    public static String a4 ;



    private VolatileTest(){

    }

    public void printInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(a1).append("  ");
        stringBuffer.append(a2).append("  ");
        stringBuffer.append(a3).append("  ");
        stringBuffer.append(a4).append("  ");

        System.out.println( stringBuffer.toString());
    }


    private static VolatileTest volatileTest;
    private static VolatileTest getInstance(){
        if (volatileTest == null){
            synchronized (mutex){
                if (volatileTest == null){
                    volatileTest = new VolatileTest();
                }
            }
        }
        return volatileTest;
    }


    public static void main(String[] args) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        for (int i=0; i< 20 ; i++){
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    VolatileTest.getInstance().printInfo();
                }
            }, "name" + i));

        }

        for (Thread t : threadList
                ) {
            t.start();
        }

        for (Thread t : threadList
                ) {
            t.join();
        }

    }

}
