package org.lkpnotice.infra.metrics.dropwizard;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import org.eclipse.jetty.http.HttpParser;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by jpliu on 2020/12/22.
 */
public class HistogramTest {
    static final MetricRegistry metrics = new MetricRegistry();
    private final Histogram responseSizes = metrics.histogram(name(HttpParser.RequestHandler.class, "response-sizes"));

    public static void main(String[] args){


    }


    public void runIt(){
        responseSizes.update(22);
    }
}
