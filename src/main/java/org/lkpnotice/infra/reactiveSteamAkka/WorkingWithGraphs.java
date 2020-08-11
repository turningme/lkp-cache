package org.lkpnotice.infra.reactiveSteamAkka;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.japi.tuple.Tuple3;
import akka.stream.Attributes;
import akka.stream.ClosedShape;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.Graph;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.SinkShape;
import akka.stream.SourceShape;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Zip;
import akka.stream.javadsl.ZipWith;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Created by jpliu on 2020/8/6.
 */
public class WorkingWithGraphs {
    static final ActorSystem system = ActorSystem.create("QuickStart");

    public static void main(String[] args) {
        t15();
    }


    //Graph Dsl
    static void t1() {
        final Source<Integer, NotUsed> in = Source.from(Arrays.asList(1, 2, 3, 4, 5));
        final Sink<List<String>, CompletionStage<List<String>>> sink = Sink.head();
        final Flow<Integer, Integer, NotUsed> f1 = Flow.of(Integer.class).map(elem -> elem + 10);
        final Flow<Integer, Integer, NotUsed> f2 = Flow.of(Integer.class).map(elem -> elem + 20);
        final Flow<Integer, String, NotUsed> f3 = Flow.of(Integer.class).map(elem -> elem.toString());
        final Flow<Integer, Integer, NotUsed> f4 = Flow.of(Integer.class).map(elem -> elem + 30);

        final RunnableGraph<CompletionStage<List<String>>> result =
                RunnableGraph.fromGraph(
                        GraphDSL // create() function binds sink, out which is sink's out port and builder DSL
                                .create( // we need to reference out's shape in the builder DSL below (in to()
                                         // function)
                                         sink, // previously created sink (Sink)
                                         (builder, out) -> { // variables: builder (GraphDSL.Builder) and out (SinkShape)
                                             final UniformFanOutShape<Integer, Integer> bcast =
                                                     builder.add(Broadcast.create(2));
                                             final UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(2));

                                             final Outlet<Integer> source = builder.add(in).out();
                                             builder
                                                     .from(source)
                                                     .via(builder.add(f1))
                                                     .viaFanOut(bcast)
                                                     .via(builder.add(f2))
                                                     .viaFanIn(merge)
                                                     .via(builder.add(f3.grouped(1000)))
                                                     .to(out); // to() expects a SinkShape
                                             builder.from(bcast).via(builder.add(f4)).toFanIn(merge);
                                             return ClosedShape.getInstance();
                                         }));

    }


    //parallel streaming
    static void t2() {
        final Sink<Integer, CompletionStage<Integer>> topHeadSink = Sink.head();
        final Sink<Integer, CompletionStage<Integer>> bottomHeadSink = Sink.head();
        final Flow<Integer, Integer, NotUsed> sharedDoubler =
                Flow.of(Integer.class).map(elem -> elem * 2);

        final RunnableGraph<Pair<CompletionStage<Integer>, CompletionStage<Integer>>> g =
                RunnableGraph.<Pair<CompletionStage<Integer>, CompletionStage<Integer>>>fromGraph(
                        GraphDSL.create(
                                topHeadSink, // import this sink into the graph
                                bottomHeadSink, // and this as well
                                Keep.both(),
                                (b, top, bottom) -> {
                                    final UniformFanOutShape<Integer, Integer> bcast = b.add(Broadcast.create(2));

                                    b.from(b.add(Source.single(1)))
                                            .viaFanOut(bcast)
                                            .via(b.add(sharedDoubler))
                                            .to(top);
                                    b.from(bcast).via(b.add(sharedDoubler)).to(bottom);
                                    return ClosedShape.getInstance();
                                }));
    }


    // list streaming
    static void t3() {

        // create the source
        final Source<String, NotUsed> in = Source.from(Arrays.asList("ax", "bx", "cx"));
// generate the sinks from code
        List<String> prefixes = Arrays.asList("a", "b", "c");
        final List<Sink<String, CompletionStage<String>>> list = new ArrayList<>();
        for (String prefix : prefixes) {
            final Sink<String, CompletionStage<String>> sink =
                    Flow.of(String.class)
                            .filter(str -> str.startsWith(prefix))
                            .toMat(Sink.head(), Keep.right());
            list.add(sink);
        }

        final RunnableGraph<List<CompletionStage<String>>> g =
                RunnableGraph.fromGraph(
                        GraphDSL.create(
                                list,
                                (GraphDSL.Builder<List<CompletionStage<String>>> builder,
                                        List<SinkShape<String>> outs) -> {
                                    final UniformFanOutShape<String, String> bcast =
                                            builder.add(Broadcast.create(outs.size()));

                                    final Outlet<String> source = builder.add(in).out();
                                    builder.from(source).viaFanOut(bcast);

                                    for (SinkShape<String> sink : outs) {
                                        builder.from(bcast).to(sink);
                                    }

                                    return ClosedShape.getInstance();
                                }));
        List<CompletionStage<String>> result = g.run(system);

    }


    /// partial stream
    static void t4() {
        final Graph<FanInShape2<Integer, Integer, Integer>, NotUsed> zip =
                ZipWith.create((Integer left, Integer right) -> Math.max(left, right));

        final Graph<UniformFanInShape<Integer, Integer>, NotUsed> pickMaxOfThree =
                GraphDSL.create(
                        builder -> {
                            final FanInShape2<Integer, Integer, Integer> zip1 = builder.add(zip);
                            final FanInShape2<Integer, Integer, Integer> zip2 = builder.add(zip);

                            builder.from(zip1.out()).toInlet(zip2.in0());
                            // return the shape, which has three inputs and one output
                            return new UniformFanInShape<Integer, Integer>(
                                    zip2.out(), new Inlet[]{zip1.in0(), zip1.in1(), zip2.in1()});
                        });

        final Sink<Integer, CompletionStage<Integer>> resultSink = Sink.<Integer>head();

        final RunnableGraph<CompletionStage<Integer>> g =
                RunnableGraph.<CompletionStage<Integer>>fromGraph(
                        GraphDSL.create(
                                resultSink,
                                (builder, sink) -> {
                                    // import the partial graph explicitly
                                    final UniformFanInShape<Integer, Integer> pm = builder.add(pickMaxOfThree);

                                    builder.from(builder.add(Source.single(1))).toInlet(pm.in(0));
                                    builder.from(builder.add(Source.single(2))).toInlet(pm.in(1));
                                    builder.from(builder.add(Source.single(3))).toInlet(pm.in(2));
                                    builder.from(pm.out()).to(sink);
                                    return ClosedShape.getInstance();
                                }));

        final CompletionStage<Integer> max = g.run(system);
    }


    static void t5() {
        // first create an indefinite source of integer numbers
        class Ints implements Iterator<Integer> {
            private int next = 0;

            @Override
            public boolean hasNext() {
                if (next < 20) {
                    return true;
                }
                return false;
            }

            @Override
            public Integer next() {
                return next++;
            }
        }
        final Source<Integer, NotUsed> ints = Source.fromIterator(() -> new Ints());

        final Source<Pair<Integer, Integer>, NotUsed> pairs =
                Source.fromGraph(
                        GraphDSL.create(
                                builder -> {
                                    final FanInShape2<Integer, Integer, Pair<Integer, Integer>> zip =
                                            builder.add(Zip.create());

                                    builder.from(builder.add(ints.filter(i -> i % 2 == 0))).toInlet(zip.in0());
                                    builder.from(builder.add(ints.filter(i -> i % 2 == 1))).toInlet(zip.in1());

                                    return SourceShape.of(zip.out());
                                }));

        pairs.map(f -> {
            TimeUnit.SECONDS.sleep(2);
            System.out.println(f.first() + "," + f.second() + " [t" + Thread.currentThread().getName());
            return 0;
        }).run(system);

        system.terminate();
    }


    /////
    static void t6() {
        final Flow<Integer, Pair<Integer, String>, NotUsed> pairs =
                Flow.fromGraph(
                        GraphDSL.create(
                                b -> {
                                    final UniformFanOutShape<Integer, Integer> bcast = b.add(Broadcast.create(2));
                                    final FanInShape2<Integer, String, Pair<Integer, String>> zip =
                                            b.add(Zip.create());

                                    b.from(bcast).toInlet(zip.in0());
                                    b.from(bcast)
                                            .via(b.add(Flow.of(Integer.class).map(i -> i.toString())))
                                            .toInlet(zip.in1());

                                    return FlowShape.of(bcast.in(), zip.out());
                                }));

        Source.single(1).via(pairs).runWith(Sink.<Pair<Integer, String>>head(), system);
    }


    static void t7() {
        Source<Integer, NotUsed> source1 = Source.single(1);
        Source<Integer, NotUsed> source2 = Source.single(2);

        final Source<Integer, NotUsed> sources =
                Source.combine(source1, source2, new ArrayList<>(), i -> Merge.<Integer>create(i));
        sources.runWith(Sink.<Integer, Integer>fold(0, (a, b) -> a + b), system);

    }


    /// remote
    static void t8() {
        /*Sink<Integer, NotUsed> sendRemotely = Sink.actorRef(actorRef, "Done");
        Sink<Integer, CompletionStage<Done>> localProcessing =
                Sink.<Integer>foreach(
                        a -> {
          *//*do something useful*//*
                        });
        Sink<Integer, NotUsed> sinks =
                Sink.combine(sendRemotely, localProcessing, new ArrayList<>(), a -> Broadcast.create(a));

        Source.<Integer>from(Arrays.asList(new Integer[] {0, 1, 2})).runWith(sinks, system);*/
    }


    static void t9() {
        final Sink<Integer, CompletionStage<Integer>> foldSink =
                Sink.<Integer, Integer>fold(
                        0,
                        (a, b) -> {
                            return a + b;
                        });

        final Flow<CompletionStage<Integer>, Integer, NotUsed> flatten =
                Flow.<CompletionStage<Integer>>create().mapAsync(4, x -> x);

        final Flow<Integer, Integer, CompletionStage<Integer>> foldingFlow =
                Flow.fromGraph(
                        GraphDSL.create(
                                foldSink,
                                (b, fold) -> {
                                    return FlowShape.of(
                                            fold.in(), b.from(b.materializedValue()).via(b.add(flatten)).out());
                                }));


    }


    static void t10() {
        Source.single(0)
                .map(i -> i + 1)
                .filter(i -> i != 0)
                .map(i -> i - 2)
                .to(Sink.fold(0, (acc, i) -> acc + i));
    }


    static void t11() {
        final Source<Integer, NotUsed> nestedSource =
                Source.single(0) // An atomic source
                        .map(i -> i + 1) // an atomic processing stage
                        .named("nestedSource"); // wraps up the current Source and gives it a name

        final Flow<Integer, Integer, NotUsed> nestedFlow =
                Flow.of(Integer.class)
                        .filter(i -> i != 0) // an atomic processing stage
                        .map(i -> i - 2) // another atomic processing stage
                        .named("nestedFlow"); // wraps up the Flow, and gives it a name

        final Sink<Integer, NotUsed> nestedSink =
                nestedFlow
                        .to(Sink.fold(0, (acc, i) -> acc + i)) // wire an atomic sink to the nestedFlow
                        .named("nestedSink"); // wrap it up

// Create a RunnableGraph
        final RunnableGraph<NotUsed> runnableGraph = nestedSource.to(nestedSink);
    }


    static void t12() {
        final Source<Integer, NotUsed> nestedSource =
                Source.single(0).map(i -> i + 1).named("nestedSource"); // Wrap, no inputBuffer set

        final Flow<Integer, Integer, NotUsed> nestedFlow =
                Flow.of(Integer.class)
                        .filter(i -> i != 0)
                        .via(
                                Flow.of(Integer.class)
                                        .map(i -> i - 2)
                                        .withAttributes(Attributes.inputBuffer(4, 4)))
                        .named("nestedFlow"); // Wrap, no inputBuffer set

        final Sink<Integer, NotUsed> nestedSink =
                nestedFlow
                        .to(Sink.fold(0, (acc, i) -> acc + i)) // wire an atomic sink to the nestedFlow
                        .withAttributes(
                                Attributes.name("nestedSink").and(Attributes.inputBuffer(3, 3))); // override
    }

    static void t13() {
        Source.from(Arrays.asList(1, 2, 3))
                .map(
                        i -> {
                            System.out.println("A: " + i + " [t" + Thread.currentThread().getName());
                            return i;
                        })
                .async()
                .map(
                        i -> {
                            System.out.println("B: " + i + " [t" + Thread.currentThread().getName());
                            return i;
                        })
                .async()
                .map(
                        i -> {
                            System.out.println("C: " + i + " [t" + Thread.currentThread().getName());
                            return i;
                        })
                .async()
                .runWith(Sink.ignore(), system);
    }


    static void t14() {
        final Flow<Integer, Integer, NotUsed> flow1 =
                Flow.of(Integer.class)
                        .map(elem -> elem * 2)
                        .async()
                        .addAttributes(Attributes.inputBuffer(1, 1)); // the buffer size of this map is 1
        final Flow<Integer, Integer, NotUsed> flow2 =
                flow1
                        .via(Flow.of(Integer.class).map(elem -> elem / 2))
                        .async(); // the buffer size of this map is the value from the surrounding graph it is
// used in
        final RunnableGraph<NotUsed> runnableGraph =
                Source.range(1, 10).via(flow1).to(Sink.foreach(elem -> System.out.println(elem)));

        final RunnableGraph<NotUsed> withOverridenDefaults =
                runnableGraph.withAttributes(Attributes.inputBuffer(64, 64));
    }

    static void t15(){
        final Duration oneSecond = Duration.ofSeconds(1);
        final Source<String, Cancellable> msgSource = Source.tick(oneSecond, oneSecond, "message!");
        final Source<String, Cancellable> tickSource =
                Source.tick(oneSecond.multipliedBy(3), oneSecond.multipliedBy(3), "tick");
        final Flow<String, Integer, NotUsed> conflate =
                Flow.of(String.class).conflateWithSeed(first -> 1, (count, elem) -> count + 1);

        RunnableGraph.fromGraph(
                GraphDSL.create(
                        b -> {
                            // this is the asynchronous stage in this graph
                            final FanInShape2<String, Integer, Integer> zipper =
                                    b.add(ZipWith.create((String tick, Integer count) -> count).async());
                            b.from(b.add(msgSource)).via(b.add(conflate)).toInlet(zipper.in1());
                            b.from(b.add(tickSource)).toInlet(zipper.in0());
                            b.from(zipper.out()).to(b.add(Sink.foreach(elem -> System.out.println(elem))));
                            return ClosedShape.getInstance();
                        }))
                .run(system);
    }





    static void t16(){
        final Flow<Double, Tuple3<Double, Double, Integer>, NotUsed> statsFlow =
                Flow.of(Double.class)
                        .conflateWithSeed(
                                elem -> Collections.singletonList(elem),
                                (acc, elem) -> {
                                    return Stream.concat(acc.stream(), Collections.singletonList(elem).stream())
                                            .collect(Collectors.toList());
                                })
                        .map(
                                s -> {
                                    final Double mean = s.stream().mapToDouble(d -> d).sum() / s.size();
                                    final DoubleStream se = s.stream().mapToDouble(x -> Math.pow(x - mean, 2));
                                    final Double stdDev = Math.sqrt(se.sum() / s.size());
                                    return new Tuple3<>(stdDev, mean, s.size());
                                });
    }

}
