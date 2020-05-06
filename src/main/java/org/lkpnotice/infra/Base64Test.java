package org.lkpnotice.infra;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * Created by jpliu on 2020/4/25.
 */
public class Base64Test {
    public static void main(String[] args){
       // String text = "lkpnotice@163.com";
         String text = "dove_999";
        //编码
        String encode = Base64.getEncoder()
                .encodeToString(text.getBytes(StandardCharsets.UTF_8));
        System.out.println(encode);

        //解码
        String decode = new String(Base64.getDecoder().decode(encode), StandardCharsets.UTF_8);
        System.out.println(decode);
    }
}
