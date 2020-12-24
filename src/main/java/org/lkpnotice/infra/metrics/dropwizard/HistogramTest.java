package org.lkpnotice.infra.metrics.dropwizard;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

import org.eclipse.jetty.http.HttpParser;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by jpliu on 2020/12/22.
 */
public class HistogramTest {
    static final MetricRegistry metrics = new MetricRegistry();
    private static final Histogram responseSizes = metrics.histogram(name(HttpParser.RequestHandler.class, "response-sizes"));
    static Slf4jReporter slf4jReporter;

    public static void main(String[] args){
        slf4jReporter = Slf4jReporter.forRegistry(metrics)
                .build();

        runIt();
        slf4jReporter.report();
    }


    public static void runIt(){
        responseSizes.update(22);
    }
}
