package org.lkpnotice.infra.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by jpliu on 2020/12/22.
 */
public class TimerTest {
    static final MetricRegistry metrics = new MetricRegistry();

    private final Timer responses = metrics.timer(name(TimerTest.class, "responses"));

    public static void main(String[] args) throws InterruptedException {
        TimerTest tt = new TimerTest();
        tt.tt();
        tt.logInfo();
    }


    public void tt() throws InterruptedException {
        Timer.Context context = responses.time();
        Thread.sleep(100);
        context.stop();
    }

    public void logInfo(){
        //responses.
    }

}
