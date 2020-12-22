package org.lkpnotice.infra;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by jpliu on 2020/12/17.
 */
public class GetHostName {
    public static String getHostName() {
        if (System.getenv("COMPUTERNAME") != null) {
            return System.getenv("COMPUTERNAME");
        } else {
            return getHostNameForLiunx();
        }
    }

    public static String getHostNameForLiunx() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }


    public static void main(String[] args){
        System.out.println("**** " + getHostName());
    }
}
