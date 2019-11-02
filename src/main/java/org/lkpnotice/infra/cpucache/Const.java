package org.lkpnotice.infra.cpucache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpliu on 2019/11/2.
 */
public class Const {
    static final Logger LOG = LoggerFactory.getLogger(Const.class);
    public static final int LOOP_COUNT_DEFAULT = 10;
    public static final String LOOP_COUNT_KEY = "loop.count";


    public static int getLoopCount(){
        int loopCntVal = LOOP_COUNT_DEFAULT;
        String loopCntStr = System.getProperty(LOOP_COUNT_KEY);
        if (null != loopCntStr){
            try {
                loopCntVal = Integer.parseInt(loopCntStr);
            }catch (Exception e){
                LOG.error("getLoopCount value is {}, set default value to {}  ",e , LOOP_COUNT_DEFAULT);
            }finally {
            }

        }

        LOG.info(" Final Loop count value is {} ", loopCntVal);
        return loopCntVal;
    }
}
