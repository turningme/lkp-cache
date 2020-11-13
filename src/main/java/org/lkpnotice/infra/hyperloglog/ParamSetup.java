package org.lkpnotice.infra.hyperloglog;

import net.agkn.hll.HLL;

/**
 * Created by jpliu on 2020/11/1.
 */
public class ParamSetup {
    // minimum and maximum values for the log-base-2 of the number of registers
    // in the HLL
    public static final int MINIMUM_LOG2M_PARAM = 4;
    public static final int MAXIMUM_LOG2M_PARAM = 30;

    // minimum and maximum values for the register width of the HLL
    public static final int MINIMUM_REGWIDTH_PARAM = 1;
    public static final int MAXIMUM_REGWIDTH_PARAM = 8;

    // minimum and maximum values for the 'expthresh' parameter of the
    // constructor that is meant to match the PostgreSQL implementation's
    // constructor and parameter names
    public static final int MINIMUM_EXPTHRESH_PARAM = -1;
    public static final int MAXIMUM_EXPTHRESH_PARAM = 18;
    public static final int MAXIMUM_EXPLICIT_THRESHOLD = (1 << (MAXIMUM_EXPTHRESH_PARAM - 1)/*per storage spec*/);

    /**
     *@see <a href='http://research.neustar.biz/2013/01/24/hyperloglog-googles-take-on-engineering-hll/'>Blog post with section on 2^L</a>
     */
    private static final double[] TWO_TO_L = new double[(HLL.MAXIMUM_REGWIDTH_PARAM + 1) * (HLL.MAXIMUM_LOG2M_PARAM + 1)];
    private static final String[] TWO_TO_L_STRING = new String[(HLL.MAXIMUM_REGWIDTH_PARAM + 1) * (HLL.MAXIMUM_LOG2M_PARAM + 1)];

    /**
     * Spacing constant used to compute offsets into {@link }.
     */
    private static final int REG_WIDTH_INDEX_MULTIPLIER = HLL.MAXIMUM_LOG2M_PARAM + 1;


    public static void main(String[] args) {
        /*System.out.printf("%10s","ttt");
        System.out.printf("%10s","ttt");*/

        test1();
    }

    static void test1(){
        for(int regWidth = HLL.MINIMUM_REGWIDTH_PARAM; regWidth <= HLL.MAXIMUM_REGWIDTH_PARAM; regWidth++) {
            for(int log2m = HLL.MINIMUM_LOG2M_PARAM ; log2m <= HLL.MAXIMUM_LOG2M_PARAM; log2m++) {
                int maxRegisterValue = (1 << regWidth) - 1;

                // Since 1 is added to p(w) in the insertion algorithm, only
                // (maxRegisterValue - 1) bits are inspected hence the hash
                // space is one power of two smaller.
                final int pwBits = (maxRegisterValue - 1);
                final int totalBits = (pwBits + log2m);
                final double twoToL = Math.pow(2, totalBits);
                String temp = String.format("2^((1<<(%s)-1)+%s)",regWidth,log2m);
                TWO_TO_L_STRING[(REG_WIDTH_INDEX_MULTIPLIER * regWidth) + log2m] = temp;
                TWO_TO_L[(REG_WIDTH_INDEX_MULTIPLIER * regWidth) + log2m] = twoToL;
            }
        }

        System.out.printf("%25s", "regwidth\\log2m");
        for (int log2m = HLL.MINIMUM_LOG2M_PARAM; log2m <= HLL.MAXIMUM_LOG2M_PARAM; log2m++) {

            System.out.printf("%25s", "log2m="+log2m);
        }
        System.out.printf("\n");


        for(int regWidth = HLL.MINIMUM_REGWIDTH_PARAM; regWidth <= HLL.MAXIMUM_REGWIDTH_PARAM; regWidth++) {
            System.out.printf("%25s", "regwidth="+regWidth);
            for (int log2m = HLL.MINIMUM_LOG2M_PARAM; log2m <= HLL.MAXIMUM_LOG2M_PARAM; log2m++) {
                System.out.printf("%25s",TWO_TO_L_STRING[(REG_WIDTH_INDEX_MULTIPLIER * regWidth) + log2m]);
            }
            System.out.printf("\n");
        }

    }
}
