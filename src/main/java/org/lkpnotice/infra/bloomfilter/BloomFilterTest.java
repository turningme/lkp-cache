package org.lkpnotice.infra.bloomfilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;

/**
 * Created by jpliu on 2020/10/23.
 */
public class BloomFilterTest {


    public static void main(String[] args){
        Funnel<CharSequence> f = Funnels.stringFunnel(Charset.forName("UTF-8"));
        BloomFilter bf = BloomFilter.create(f, 256);


        System.out.println(bf.put("x1"));
        System.out.println(bf.put("x1"));
        System.out.println(bf.mightContain("x1"));

        bf.put("x2");
        bf.put("x3");
        bf.put("x4");

     /*   System.out.println(bf.approximateElementCount());
        System.out.println(bf.expectedFpp());*/
    }

}
