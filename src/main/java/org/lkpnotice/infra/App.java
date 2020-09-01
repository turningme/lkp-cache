package org.lkpnotice.infra;

import java.util.HashSet;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        byte[] x = new byte[]{1,2,3};
        byte[] y = new byte[]{1,2,3};

        //System.out.println(Arrays.equals(x,y));
        Set<byte[]> byteSet = new HashSet();
        byteSet.add(x);

        System.out.println(byteSet.contains(x));
        System.out.println(byteSet.contains(y));

        //System.load();
        //System.loadLibrary();
    }
}
