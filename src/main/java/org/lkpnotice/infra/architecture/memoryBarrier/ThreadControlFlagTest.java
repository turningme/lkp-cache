package org.lkpnotice.infra.architecture.memoryBarrier;

import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2020/5/22.
 * I don't implement it
 */
public class ThreadControlFlagTest {



    public static void main(String[] args) throws InterruptedException {
        testSharedFlag();
    }

    static void testSharedFlag() throws InterruptedException {
        MockTask mockTask = new MockTask();
        new Thread(mockTask).start();



        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("first time mockTask.setStopIt(true)");
                mockTask.setStopIt(true);
            }
        }).start();


/*        TimeUnit.SECONDS.sleep(5);
        System.out.println("second time mockTask.setStopIt(true)");
        mockTask.setStopIt(true);*/

    }

    static class MockTask implements Runnable{
        boolean stopIt = false;
        @Override
        public void run() {
            System.out.println("start MockTask runner ");
            int count = 0;
            while (!stopIt){
                count ++;
//                System.out.println("in MockTask runner ");
              /*  try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }*/

                if (count % 100000 == 0){
                    System.out.print("mode 100000 branch");
                    count = 0;
                }

            }

            System.out.println("end MockTask runner ");
        }

        public boolean isStopIt() {
            return stopIt;
        }

        public void setStopIt(boolean stopIt) {
            this.stopIt = stopIt;
        }
    }
}
