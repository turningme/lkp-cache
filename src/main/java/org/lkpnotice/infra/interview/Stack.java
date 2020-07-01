package org.lkpnotice.infra.interview;

import java.util.LinkedList;

/**
 * Created by jpliu on 2020/7/1.
 */
public class Stack {
    LinkedList<Integer> linkedList = new LinkedList<>();


    synchronized void push(Integer in){
        synchronized (linkedList){
          linkedList.push(in);
            notify();
        }
    }


    synchronized Integer pop() throws InterruptedException {
        synchronized (linkedList){
            if (linkedList.size() <=0){
                wait();
            }
        }

        return linkedList.pop();
    }




    public static void main(String[] args) throws InterruptedException {
        Stack stack = new Stack();


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        int i = stack.pop();
                        System.out.println(" main runnable consume  " + i );
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println(e);
                }

            }
        });


        t1.start();


        stack.push(1);
        stack.push(2);
        stack.push(3);


        t1.join();

    }

}
