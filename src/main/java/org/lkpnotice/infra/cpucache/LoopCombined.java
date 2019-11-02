package org.lkpnotice.infra.cpucache;


import org.slf4j.LoggerFactory;

/**
 * Created by jpliu on 2019/11/2.
 * doc:  https://hackernoon.com/programming-how-to-improve-application-performance-by-understanding-the-cpu-cache-levels-df0e87b70c90
 * from my own experiment like that PoC, I found it right to
 */
public class LoopCombined {
    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LoopCombined.class);

    public static void main(String[] args) {
        applyFun();
    }


    public static void applyFun() {
        int loops = Const.getLoopCount();
        StringBuilder stringBuilderA = new StringBuilder();
        StringBuilder stringBuilderB = new StringBuilder();
        String appendStr = "asfdsafads fadsfffaàdddffafdafadsfadsw3erfgasrfdasfdsafasfdsafadshatfasdfvasdfdafdsafdsafasdfsafadsfsadfsafasfdsaf";


        long timediffA = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            stringBuilderA.append(appendStr);
            stringBuilderB.append(appendStr);
        }
        timediffA = System.nanoTime() - timediffA;


        LOG.info("time of LoopCombined = {}   ", timediffA);

    }
}
