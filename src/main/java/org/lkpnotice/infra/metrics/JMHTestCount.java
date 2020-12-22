package org.lkpnotice.infra.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;
import org.lkpnotice.infra.metrics.seReport.Report;
import org.lkpnotice.infra.metrics.seReport.ReportItems;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Created by jpliu on 2020/12/22.
 */

@BenchmarkMode(Mode.AverageTime) //测试平均执行时间
// iterations：预热迭代次数，time每次迭代用时（原理：任务丢到线程池后，sleep指定的time，isDone = true,线程来判断isDone）
@Warmup(iterations = 1, time = 4)
//iterations：测量迭代次数，time每次迭代用时，batchSize：相当于给函数加了一个for循环（整个for循环完成的时间要>time），整个for循环算一个operation
@Measurement(iterations = 3, time = 3, batchSize = 3)
@Fork(2) //总执行两轮
@Threads(1) //线程池线程数
@OutputTimeUnit(TimeUnit.MILLISECONDS) //结果输出单位
public class JMHTestCount {


 /*   @Benchmark
    public void testStringAdd() {
        try { Thread.sleep(1000 * 1); } catch (InterruptedException e) { e.printStackTrace(); }
    }*/



    @Benchmark
    public long testIReportCounter(){
        for (int i = 0 ; i< 1000_00 ;i ++){
            Report.campaignsCollections.addEventCount(ReportItems.AD_ID_COUNT, 1);
        }

        return  Report.campaignsCollections.getElapsedTime();
    }


    static final MetricRegistry metrics = new MetricRegistry();
    static Meter requests = metrics.meter("requests");
    @Benchmark
    public long testDropWizardCounter(){
        for (int i = 0 ; i< 1000_00 ;i ++) {
            requests.mark(1);
        }

        return requests.getCount();
    }


    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(JMHTestCount.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
