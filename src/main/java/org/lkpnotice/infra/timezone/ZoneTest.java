package org.lkpnotice.infra.timezone;

import java.util.TimeZone;

/**
 * Created by jpliu on 2020/9/11.
 */
public class ZoneTest {
    public static void main(String[] args){
        System.out.println("len = " + TimeZone.getAvailableIDs().length);

        for (int i = 0; i < TimeZone.getAvailableIDs().length ; i++) {
            System.out.println("zoneId = " + TimeZone.getAvailableIDs()[i]);
        }
    }
}
