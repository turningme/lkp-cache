package org.lkpnotice.infra.testjavac;

/**
 * Created by jpliu on 2020/12/16.
 */
public class TestJavacIf {
    public static void main(String[] args){
        int a = 1;
        int b = 2;
        int c = 3;
    }

    public void test1(int a , int b , int c ){

        if (a == 1 || b==2 || c ==3){
            System.out.println("cccc");
        }
    }


    public void test2(int a , int b , int c ){

        if (a == 1){
            System.out.println("cccc");
        }else if (b == 2){
            System.out.println("cccc");
        }else if (c == 3){
            System.out.println("cccc");
        }
    }
}
