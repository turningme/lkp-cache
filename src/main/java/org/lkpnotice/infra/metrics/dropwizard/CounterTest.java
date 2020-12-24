package org.lkpnotice.infra.metrics.dropwizard;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 * Created by jpliu on 2020/12/23.
 */
public class CounterTest {
    static final MetricRegistry metrics = new MetricRegistry();
    static Slf4jReporter slf4jReporter;

    public static void main(String[] args){
        slf4jReporter = Slf4jReporter.forRegistry(metrics)
                .build();
        final Counter evictions = metrics.counter("test-counter");
        evictions.inc();
        evictions.inc(10);


        slf4jReporter.report();
    }
}
