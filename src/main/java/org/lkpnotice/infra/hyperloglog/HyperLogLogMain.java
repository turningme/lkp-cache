package org.lkpnotice.infra.hyperloglog;


import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;
import net.agkn.hll.HLL;
import net.agkn.hll.HLLType;

/**
 * Created by jpliu on 2020/10/20.
 */
public class HyperLogLogMain {
    static final HashFunction HASH = Hashing.murmur3_128();



    public static void main(String[] args){
       // testFull();
        System.out.println(((1L << (((1 << 2) - 1) - 1)) - 1));
    }


    static void testEmpty(){
        Hasher hasher = HASH.newHasher();
        hasher.putObject("1234", new Funnel<String>() {
            @Override
            public void funnel(String from, PrimitiveSink into) {
                into.putString(from, Charsets.US_ASCII);
            }
        });

        HashCode hashcode = hasher.hash();

        HLL hll = new HLL(13, 6, 14, true, HLLType.EMPTY);
        hll.addRaw(hashcode.asLong());

        System.out.println(hll.cardinality());
        System.out.println(hll.toBytes().length);
    }


    static void testFull(){
        HLL hll = new HLL(6, 4, 14, true, HLLType.FULL);

        for (int i = 0; i < 1; i++) {
            Hasher hasher = HASH.newHasher();
            hasher.putObject("1234" + i, new Funnel<String>() {
                @Override
                public void funnel(String from, PrimitiveSink into) {
                    into.putString(from, Charsets.US_ASCII);
                }
            });

            HashCode hashcode = hasher.hash();
            hll.addRaw(hashcode.asLong());
        }


        System.out.println(hll.cardinality());
        System.out.println(hll.toBytes().length);
    }


    static void test1(){
        HLL hll = new HLL(11,5);

        for (int i = 0; i < 4; i++) {
            Hasher hasher = HASH.newHasher();
            hasher.putObject("1234" + i, new Funnel<String>() {
                @Override
                public void funnel(String from, PrimitiveSink into) {
                    into.putString(from, Charsets.US_ASCII);
                }
            });

            HashCode hashcode = hasher.hash();
            hll.addRaw(hashcode.asLong());
        }


        System.out.println(hll.cardinality());
        System.out.println(hll.toBytes().length);
    }
}
