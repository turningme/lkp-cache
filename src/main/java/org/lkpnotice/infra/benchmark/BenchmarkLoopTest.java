package org.lkpnotice.infra.benchmark;

/**
 * Created by jpliu on 2019/11/18.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms4G", "-Xmx4G"})
@Warmup(iterations = 3)
@Measurement(iterations = 20)
public class BenchmarkLoopTest {

    @Param({"20"})
    private int N;

    private ArrayList<String> arrayList = new ArrayList<>();
    private LinkedList<String> linkedList = new LinkedList<>();
    private HashSet<String> hashSet = new HashSet<>();

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BenchmarkLoopTest.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        createData();
    }

    @Benchmark
    public void loopForArray1(Blackhole bh) {
        for (String e:arrayList
             ) {
            bh.consume(e);
        }
    }


    @Benchmark
    public void loopForHashSet1(Blackhole bh) {

        for (String e:hashSet
             ) {
            bh.consume(e);
        }
    }

/*    @Benchmark
    public void loopForArray(Blackhole bh) {
        Iterator<String> iter = linkedList.iterator();
        while(iter.hasNext()){
            bh.consume(iter.next());
        }
    }*/

/*    @Benchmark
    public void loopForLinkedList(Blackhole bh) {
        Iterator<String> iter = arrayList.iterator();
        while(iter.hasNext()){
            bh.consume(iter.next());
        }
    }*/

  /*  @Benchmark
    public void loopForHashSet(Blackhole bh) {
        Iterator<String> iter = hashSet.iterator();
        while(iter.hasNext()){
            bh.consume(iter.next());
        }
    }*/

    private void createData() {
        for (int i = 0; i < N; i++) {
            arrayList.add("Number : " + i);
            linkedList.add("Number : " + i);
            hashSet.add("Number : " + i);
        }
    }

}