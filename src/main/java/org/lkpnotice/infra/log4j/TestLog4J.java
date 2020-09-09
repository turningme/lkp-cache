package org.lkpnotice.infra.log4j;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpliu on 2020/9/9.
 *
 * add in startup arguments  -Dlog4j.debug
 * -Dlog4j.configuration=file:"/Users/jpliu/tmp/log4j.prop"
 *
 * http://logback.qos.ch/manual/mdc.html
 Mapped Diagnostic Context

 http://logback.qos.ch/manual/mdc.html  log4j slf4j
 又重温了一下 slf4j 的入口，动态绑定，运行时如果存在多个实现，会打印出来，然后会取决于类加载是哪一个入口 的类org.slf4j.impl.StaticLoggerBinder
 看了 slf4j log4j adaptor 代码，看到了扩展框架的另外一种姿势，  先实现具体的应用，分层，    api  适配器  ， 和具体实现，
 对于门面， api 层的进入具体入口的代码是通过一个指定类进入的或者绑定  ， 而代理模式 是一个对象内部实现不同，在生成代理对象的时候通过配置选择具体的被代理的事务对象～
 感觉应该对～

 */
public class TestLog4J {
    static final Logger LOG = LoggerFactory.getLogger(TestLog4J.class);

    public static void main(String[] args){
       // PropertyConfigurator.configure();
        LOG.info("test info ");

        LOG.debug("test debug ");
    }
}
