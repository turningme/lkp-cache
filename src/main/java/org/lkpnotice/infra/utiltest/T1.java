package org.lkpnotice.infra.utiltest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpliu on 2020/7/24.
 */
public class T1 {

    public static void main(String[] args){
        List<String> l1 = new ArrayList<>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        l1.add("4");

        /////
        List<String> l2 = new ArrayList<>();
        l2.add("11");
        l2.add("22");
        l2.add("33");
        l2.add("44");

        System.out.println(l1);


        for (int i =0 ; i< l1.size() ;i++){
            l1.set(i,l2.get(i));
            System.out.println("  inner " + l1.get(i));
        }

        System.out.println(l1);

    }
}
