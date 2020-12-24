package org.lkpnotice.infra.metrics.dropwizard;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

import java.util.concurrent.TimeUnit;

/**
 * Created by jpliu on 2020/12/22.
 */
public class GetStarted {
    static final MetricRegistry metrics = new MetricRegistry();
    static  Slf4jReporter slf4jReporter;
    public static void main(String args[]) {
        startReport();
        Meter requests = metrics.meter("requests");
        requests.mark();
        requests.mark();
        wait5Seconds();

//        System.out.println("finished - " + metrics.meter("requests").getCount());
        slf4jReporter.report();
    }

    static void startReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .outputTo(System.out)
           /*     .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)*/
                .build();
//        reporter.start(1, TimeUnit.SECONDS);


        slf4jReporter = Slf4jReporter.forRegistry(metrics)
                .build();
//        slf4jReporter.start(1, TimeUnit.SECONDS);

    }

    static void wait5Seconds() {
        try {
            Thread.sleep(5*1000);
        }
        catch(InterruptedException e) {}
    }
}
