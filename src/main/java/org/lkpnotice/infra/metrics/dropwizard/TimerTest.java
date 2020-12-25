package org.lkpnotice.infra.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by jpliu on 2020/12/22.
 */
public class TimerTest {
    static final MetricRegistry metrics = new MetricRegistry();

    private final Timer responses = metrics.timer(name(TimerTest.class, "responses"));
    static Slf4jReporter slf4jReporter;

    public static void main(String[] args) throws InterruptedException {
        slf4jReporter = Slf4jReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        TimerTest tt = new TimerTest();
        tt.tt();

        slf4jReporter.report();
    }


    public void tt() throws InterruptedException {
        Timer.Context context = responses.time();
        Thread.sleep(100);
        context.stop();
    }



}
