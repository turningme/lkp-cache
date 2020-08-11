package org.lkpnotice.infra;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jpliu on 2020/8/7.
 *
 * check why open jdk can not load  ext dir's  jar's MANEFEST.MF  with Classloader.getResources("MANEFEST/xxx")
 */
public class LoaderMain {
    public static void main(String[] args) {
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println(entry.getKey() + "-> " + entry.getValue());
        }

        printMeta();
    }

    private static void printMeta() {
        System.out.println("extensions: " + System.getProperty("java.ext.dirs"));

        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                    .getResources("META-INF/MANIFEST.MF");

            Class c = Thread.currentThread().getContextClassLoader().getParent().loadClass("sun.net.spi.nameservice.dns.DNSNameServiceDescriptor");
            System.out.println("ccc " + c);

            URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader().getParent();
            for (URL urll:urlClassLoader.getURLs()) {
                System.out.println("tttt " + urll);
            }

            resources = Thread.currentThread().getContextClassLoader().getParent().getResources("META-INF/MANIFEST.MF");



            while (resources.hasMoreElements()) {

                URL url = resources.nextElement();

                System.out.println("url: " + url);
           /*     Scanner sc = new Scanner(url.openStream());
                while (sc.hasNextLine()) {
                    System.out.println(sc.nextLine());
                }
                sc.close();*/
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }




}
