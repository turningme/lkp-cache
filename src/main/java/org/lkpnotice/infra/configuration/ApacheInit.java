package org.lkpnotice.infra.configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 * Created by jpliu on 2020/9/9.
 */
public class ApacheInit {
    public static void main(String[] args) throws IOException, ConfigurationException {
        HierarchicalINIConfiguration iniConfiguration = new HierarchicalINIConfiguration();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/lkpnotice/infra/configuration/test.ini");
        iniConfiguration.load(inputStream);


        PrintStream p = new PrintStream(System.out);

        Set<String> sets = iniConfiguration.getSections();
        for (String s:sets) {
            p.print(String.format("[%s]\n",s));
            SubnodeConfiguration subConf = iniConfiguration.getSection(s);
            Iterator<String> keys =  subConf.getKeys();
            while (keys.hasNext()){
                String key = keys.next();
                Object value = subConf.getProperty(key);
                p.print(String.format("%s=%s\n", StringUtils.replace(key,"..","."), value));
            }
        }


    }



    static void t1() throws ConfigurationException {
        HierarchicalINIConfiguration iniConfiguration = new HierarchicalINIConfiguration();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/lkpnotice/infra/configuration/test.ini");
//        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.ini");
        int a = 1;

        /*Properties properties = new Properties();
        properties.load(inputStream);
        properties.forEach((key,value)->{
            System.out.println("key ="+key + ", " + "value = " + value);
        });*/

        iniConfiguration.load(inputStream);

     /*   HierarchicalINIConfiguration iniConfigTarget = (HierarchicalINIConfiguration)iniConfiguration.clone();
        System.out.println("===================");

        FileOutputStream fout =  new FileOutputStream("/tmp/test.ini.test",false);
        iniConfigTarget.setDelimiterParsingDisabled(true);
        iniConfigTarget.setListDelimiter('.');
        iniConfigTarget.save(fout);

        fout.flush();
        fout.close();
        inputStream.close();
*/


        Iterator<String> keys = iniConfiguration.getKeys();
        while (keys.hasNext()){
            String s = keys.next();
            Object o = iniConfiguration.getProperty(s);
            System.out.println(s+"=" + o.toString());
        }

    }


}
