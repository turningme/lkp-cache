package org.lkpnotice.infra;

/**
 * Created by jpliu on 2020/6/28.
 */
public class StringUtils {
    public static void main(String[] args){
        String abc= "201505.0";
        System.out.println(abc.contains("."));
        System.out.println(abc.substring(0,abc.indexOf(".")));
    }
}
