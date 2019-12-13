package org.lkpnotice.infra.jmx;

import java.lang.management.ManagementFactory;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Created by jpliu on 2019/12/13.
 */
public class HelloAgent {
    public static void main(String[] args) throws JMException, Exception {
        // 通过工厂类获取MBeanServer，用来做MBean的容器
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        // ObjectName中的取名是有一定规范的，格式为：“域名：name=MBean名称”，其中域名和MBean的名称可以任意取。
        ObjectName helloName = new ObjectName("jmxBean:name=hello");
        //将Hello这个类注入到MBeanServer中，注入需要创建一个ObjectName类
        server.registerMBean(new Hello(), helloName);
        Thread.sleep(60*60*1000);
    }

}
