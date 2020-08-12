package org.lkpnotice.infra.internetDomain;

import com.google.common.net.InternetDomainName;

/**
 * Created by jpliu on 2020/8/11.
 */
public class Main {
    public static void main(String[] args){
        String topDomain = InternetDomainName.from("www.baidu.com").topPrivateDomain().topPrivateDomain().toString();
        System.out.println("tttt -> "  + topDomain);
    }
}
