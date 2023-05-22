package org.lkpnotice.infra.stringtest;

public class StringTest {
    public static void main(String[] args){
        String html = "<!DOCTYPE html>\n" +
                "<html  lang=\"es-es\" class=\"a-no-js a-touch a-mobile\" data-19ax5a9jf=\"mongoose\">\n" +
                " <!-- sp:feature:head-start -->\n" +
                "<head>";

        String url = "https://www.amazon.es/dp/B0062Y7D26/ref=cm_sw_r_oth_tai_H5CE5HHX1F93GWG6RPHT";
        String format = "<html  data-s2k-original-url=\"%s\" ";
        String newUrl = String.format(format, url);

        html = html.replaceFirst("<html ",newUrl);
        System.out.println(html);
    }
}
