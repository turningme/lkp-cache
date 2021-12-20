package org.lkpnotice.infra.headlessbrowser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

public class PhantomJsDriverFactory {
    private int pageLoadTimeoutInSeconds;
    private int scriptTimeoutInSeconds;
    DesiredCapabilities capabilities;

    public PhantomJsDriverFactory(int pageLoadTimeoutInSeconds, int scriptTimeoutInSeconds,
                                  DesiredCapabilities capabilities) {
        this.pageLoadTimeoutInSeconds = pageLoadTimeoutInSeconds;
        this.scriptTimeoutInSeconds = scriptTimeoutInSeconds;
        this.capabilities = capabilities;
//        capabilities.set
//        PhantomJSDriverService tt = PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

        checkFields();
    }

    private void checkFields() {
        if(pageLoadTimeoutInSeconds <= 0) {
            throw new IllegalArgumentException("Page load timeout must be greater than 0.");
        }
        if(scriptTimeoutInSeconds <= 0) {
            throw new IllegalArgumentException("Script timeout must be greater than 0.");
        }
    }

    private void setTimeouts(PhantomJSDriver driver) {
        WebDriver.Options opt = driver.manage();
        WebDriver.Timeouts timeouts = opt.timeouts();
        timeouts.pageLoadTimeout(pageLoadTimeoutInSeconds, TimeUnit.SECONDS);
        timeouts.setScriptTimeout(scriptTimeoutInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Returns a new PhantomJSDriver with the correct capabilities and
     * page load timeout
     */
    public PhantomJSDriver getInstance() {
        PhantomJSDriver driver = new PhantomJSDriver(capabilities);
        setTimeouts(driver);
        return driver;
    }


    public static PhantomJsDriverFactory getStaticInstance(){
        int pageLoadTimeout = 30;
        int scriptTimeout = 45;
        DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();
        desiredCapabilities.setCapability(org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"/apollo/env/WebContentExtractionService/bin/phantomjs");
        desiredCapabilities.setCapability(org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_CLI_ARGS," --web-security=false --ssl-protocol=any --ignore-ssl-errors=yes ");

        PhantomJsDriverFactory phantomJsDriverFactory = new PhantomJsDriverFactory(pageLoadTimeout, scriptTimeout,desiredCapabilities);
        return phantomJsDriverFactory;
    }
}
