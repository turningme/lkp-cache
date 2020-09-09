package org.lkpnotice.infra.log4j;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpliu on 2020/9/9.
 *
 * add in startup arguments  -Dlog4j.debug
 * -Dlog4j.configuration=file:"/Users/jpliu/tmp/log4j.prop"
 */
public class TestLog4J {
    static final Logger LOG = LoggerFactory.getLogger(TestLog4J.class);

    public static void main(String[] args){
       // PropertyConfigurator.configure();
        LOG.info("test info ");

        LOG.debug("test debug ");
    }
}
