package org.lkpnotice.infra.reactiveSteamAkka;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.dispatch.Futures;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by jpliu on 2020/8/6.
 */
public class BasicsAndWorkingWithFlows {
    static final ActorSystem system = ActorSystem.create("QuickStart");

    public static void main(String[] args) {
        t0();
    }


    // nop
    static void t0(){

        int a = 1;
        int b = 2;
    }

    static void t1() {
        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        // note that the Future is scala.concurrent.Future
        final Sink<Integer, CompletionStage<Integer>> sink =
                Sink.<Integer, Integer>fold(0, (aggr, next) -> {
                    int a = aggr + next;
                    System.out.println("---- " + a);
                    return a;
                });

        // connect the Source to the Sink, obtaining a RunnableFlow
        final RunnableGraph<CompletionStage<Integer>> runnable = source.toMat(sink, Keep.right());

        // materialize the flow
        final CompletionStage<Integer> sum = runnable.run(system);
        sum.thenAccept(i -> {
            System.out.println("ttttt " + i);
        }).thenRun(() -> system.terminate());

    }


    static void t2() {
        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        source.map(x -> 0); // has no effect on source, since it's immutable
        source.runWith(Sink.fold(0, (agg, next) -> agg + next), system); // 55
    }

    static void t3() {
        // connect the Source to the Sink, obtaining a RunnableGraph
        final Sink<Integer, CompletionStage<Integer>> sink =
                Sink.<Integer, Integer>fold(0, (aggr, next) -> aggr + next);
        final RunnableGraph<CompletionStage<Integer>> runnable =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).toMat(sink, Keep.right());

        // get the materialized value of the FoldSink
        final CompletionStage<Integer> sum1 = runnable.run(system);
        final CompletionStage<Integer> sum2 = runnable.run(system);

        // sum1 and sum2 are different Futures!

    }


    static void t4() {
        //Defining sources, sinks and flows

        // Create a source from an Iterable
        List<Integer> list = new LinkedList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        Source.from(list);

        // Create a source form a Future
        Source.fromFuture(Futures.successful("Hello Streams!"));

        // Create a source from a single element
        Source.single("only one element");

        // an empty source
        Source.empty();

        // Sink that folds over the stream and returns a Future
        // of the final result in the MaterializedMap
        Sink.fold(0, (Integer aggr, Integer next) -> aggr + next);

        // Sink that returns a Future in the MaterializedMap,
        // containing the first element of the stream
        Sink.head();

        // A Sink that consumes a stream without doing anything with the elements
        Sink.ignore();

        // A Sink that executes a side-effecting call for every element of the stream
        Sink.foreach(System.out::println);

        system.terminate();
    }


    static void t5() {
        // Explicitly creating and wiring up a Source, Sink and Flow
        Source.from(Arrays.asList(1, 2, 3, 4))
                .via(Flow.of(Integer.class).map(elem -> elem * 10))
                .to(Sink.foreach(System.out::println)).run(system);

        // Starting from a Source
        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4)).map(elem -> elem * 100);
        source.to(Sink.foreach(System.out::println)).run(system);


        // Starting from a Sink
        final Sink<Integer, NotUsed> sink =
                Flow.of(Integer.class).map(elem -> elem * 1000).to(Sink.foreach(System.out::println));
        Source.from(Arrays.asList(1, 2, 3, 4)).to(sink).run(system);

        system.terminate();
    }


    static void t6() {
        Source.range(1, 3).map(x -> x + 1).async().map(x -> x * 2).to(Sink.ignore()).run(system);
        system.terminate();
    }

    static void t7() {
        // An empty source that can be shut down explicitly from the outside
        Source<Integer, CompletableFuture<Optional<Integer>>> source = Source.<Integer>maybe();

// A flow that internally throttles elements to 1/second, and returns a Cancellable
// which can be used to shut down the stream
        Flow<Integer, Integer, Cancellable> flow = null;//throttler;

// A sink that returns the first element of a stream in the returned Future
        Sink<Integer, CompletionStage<Integer>> sink = Sink.head();

// By default, the materialized value of the leftmost stage is preserved
        RunnableGraph<CompletableFuture<Optional<Integer>>> r1 = source.via(flow).to(sink);

// Simple selection of materialized values by using Keep.right
        RunnableGraph<Cancellable> r2 = source.viaMat(flow, Keep.right()).to(sink);
        RunnableGraph<CompletionStage<Integer>> r3 = source.via(flow).toMat(sink, Keep.right());

// Using runWith will always give the materialized values of the stages added
// by runWith() itself
        CompletionStage<Integer> r4 = source.via(flow).runWith(sink, system);
        CompletableFuture<Optional<Integer>> r5 = flow.to(sink).runWith(source, system);
        Pair<CompletableFuture<Optional<Integer>>, CompletionStage<Integer>> r6 =
                flow.runWith(source, sink, system);

// Using more complex combinations
        RunnableGraph<Pair<CompletableFuture<Optional<Integer>>, Cancellable>> r7 =
                source.viaMat(flow, Keep.both()).to(sink);

        RunnableGraph<Pair<CompletableFuture<Optional<Integer>>, CompletionStage<Integer>>> r8 =
                source.via(flow).toMat(sink, Keep.both());

        RunnableGraph<
                Pair<Pair<CompletableFuture<Optional<Integer>>, Cancellable>, CompletionStage<Integer>>>
                r9 = source.viaMat(flow, Keep.both()).toMat(sink, Keep.both());

        RunnableGraph<Pair<Cancellable, CompletionStage<Integer>>> r10 =
                source.viaMat(flow, Keep.right()).toMat(sink, Keep.both());

// It is also possible to map over the materialized values. In r9 we had a
// doubly nested pair, but we want to flatten it out

        RunnableGraph<Cancellable> r11 =
                r9.mapMaterializedValue(
                        (nestedTuple) -> {
                            CompletableFuture<Optional<Integer>> p = nestedTuple.first().first();
                            Cancellable c = nestedTuple.first().second();
                            CompletionStage<Integer> f = nestedTuple.second();

                            // Picking the Cancellable, but we could  also construct a domain class here
                            return c;
                        });
    }





    //////


    static void t8(){
        Source<String, ActorRef> matValuePoweredSource = Source.actorRef(100, OverflowStrategy.fail());

        Pair<ActorRef, Source<String, NotUsed>> actorRefSourcePair =
                matValuePoweredSource.preMaterialize(system);

        actorRefSourcePair.first().tell("Hello!", ActorRef.noSender());

        // pass source around for materialization
        actorRefSourcePair.second().runWith(Sink.foreach(System.out::println), system);

        system.terminate();
    }





    /////////////// stream liftcycle ,  materializer .
    final class RunWithMyself extends AbstractActor {

        Materializer mat = Materializer.createMaterializer(context());

        @Override
        public void preStart() throws Exception {
            Source.repeat("hello")
                    .runWith(
                            Sink.onComplete(
                                    tryDone -> {
                                        System.out.println("Terminated stream: " + tryDone);
                                    }),
                            mat);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(
                            String.class,
                            p -> {
                                // this WILL terminate the above stream as well
                                context().stop(self());
                            })
                    .build();
        }
    }




    final class RunForever extends AbstractActor {

        private final Materializer materializer;

        public RunForever(Materializer materializer) {
            this.materializer = materializer;
        }

        @Override
        public void preStart() throws Exception {
            Source.repeat("hello")
                    .runWith(
                            Sink.onComplete(
                                    tryDone -> {
                                        System.out.println("Terminated stream: " + tryDone);
                                    }),
                            materializer);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(
                            String.class,
                            p -> {
                                // will NOT terminate the stream (it's bound to the system!)
                                context().stop(self());
                            })
                    .build();
        }

    }



}
