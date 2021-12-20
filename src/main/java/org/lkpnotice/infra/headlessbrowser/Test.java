package org.lkpnotice.infra.headlessbrowser;

import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Test {
    public static void main(String[] args){
        String logic = "/apollo/env/WebContentExtractionService/extraction-logic/logic.js";
        String input = "/tmp/ss.html";

        try {
            System.out.println(" Hello World ");
            System.out.println(" logic =  " + logic);
            System.out.println(" input  = " + input);
            LogicLoader extractionLogic = new LogicLoader(logic);

            URL testurl = new File(input).toURL();
            PhantomJsDriverFactory phantomJsDriverFactory = PhantomJsDriverFactory.getStaticInstance();
            System.out.println(String.join(", " , args));

            PhantomJSDriver phantomJSDriver =  phantomJsDriverFactory.getInstance();

            System.out.println("Loading url " + testurl.toString() + " into PhantomJS");
            phantomJSDriver.get(testurl.toString());
            System.out.println("Successfully loaded url " + testurl.toString() + " into PhantomJS");

            Map<String, String> result = (Map<String, String>) phantomJSDriver.executePhantomJS(extractionLogic.getLogic());
            if (result != null){
                System.out.println("print result ");
                StringBuilder stringBuilder = new StringBuilder();

                for (String key:result.keySet()) {
                    stringBuilder.append("key = ").append(key);
                    stringBuilder.append(" , value = ").append(result.get(key));
                }
                System.out.println("key length " + result.size());
                System.out.println(stringBuilder.toString());
            }else {
                System.out.println(" The result is null ");
            }



        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("exception " + e.toString());
        }


    }
}
