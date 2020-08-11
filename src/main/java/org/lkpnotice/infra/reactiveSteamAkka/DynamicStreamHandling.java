package org.lkpnotice.infra.reactiveSteamAkka;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.japi.Pair;
import akka.stream.DelayOverflowStrategy;
import akka.stream.KillSwitches;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.BroadcastHub;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.MergeHub;
import akka.stream.javadsl.PartitionHub;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by jpliu on 2020/8/7.
 */
public class DynamicStreamHandling {
    static final ActorSystem system = ActorSystem.create("QuickStart");


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        t5();
    }


    //completion
    static void t1() throws InterruptedException, ExecutionException, TimeoutException {
        final Source<Integer, NotUsed> countingSrc =
                Source.from(new ArrayList<>(Arrays.asList(1, 2, 3, 4)))
                        .delay(Duration.ofSeconds(1), DelayOverflowStrategy.backpressure());
        final Sink<Integer, CompletionStage<Integer>> lastSnk = Sink.last();

        final Pair<UniqueKillSwitch, CompletionStage<Integer>> stream =
                countingSrc
                        .viaMat(KillSwitches.single(), Keep.right())
                        .toMat(lastSnk, Keep.both())
                        .run(system);

        final UniqueKillSwitch killSwitch = stream.first();
        final CompletionStage<Integer> completionStage = stream.second();

        //doSomethingElse();


        final int finalCount = completionStage.toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals(4, finalCount);
        System.out.println("---- " + finalCount);
        killSwitch.shutdown();

    }


    //abort
    static void t2() throws InterruptedException, ExecutionException, TimeoutException {
        final Source<Integer, NotUsed> countingSrc =
                Source.from(new ArrayList<>(Arrays.asList(1, 2, 3, 4)))
                        .delay(Duration.ofSeconds(1), DelayOverflowStrategy.backpressure());
        final Sink<Integer, CompletionStage<Integer>> lastSnk = Sink.last();

        final Pair<UniqueKillSwitch, CompletionStage<Integer>> stream =
                countingSrc
                        .viaMat(KillSwitches.single(), Keep.right())
                        .toMat(lastSnk, Keep.both())
                        .run(system);

        final UniqueKillSwitch killSwitch = stream.first();
        final CompletionStage<Integer> completionStage = stream.second();

        final Exception error = new Exception("boom!");
        killSwitch.abort(error);

        final int result = completionStage.toCompletableFuture().exceptionally(e -> -1).get(10, TimeUnit.SECONDS);
        assertEquals(-1, result);
    }


    static void t3() {
        // A simple consumer that will print to the console for now
        Sink<String, CompletionStage<Done>> consumer = Sink.foreach(System.out::println);

        // Attach a MergeHub Source to the consumer. This will materialize to a
        // corresponding Sink.
        RunnableGraph<Sink<String, NotUsed>> runnableGraph = MergeHub.of(String.class, 16).to(consumer);

        // By running/materializing the consumer we get back a Sink, and hence
        // now have access to feed elements into it. This Sink can be materialized
        // any number of times, and every element that enters the Sink will
        // be consumed by our consumer.
        Sink<String, NotUsed> toConsumer = runnableGraph.run(system);


        // materialize more than one times ? ,  so  singleton not guaranteed ?
        Source.single("Hello!").runWith(toConsumer, system);
        Source.single("Hub!").runWith(toConsumer, system);

    }


    static void t4() {
        // A simple producer that publishes a new "message" every second
        Source<String, Cancellable> producer =
                Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(1), "New message");

        // Attach a BroadcastHub Sink to the producer. This will materialize to a
        // corresponding Source.
        // (We need to use toMat and Keep.right since by default the materialized
        // value to the left is used)
        RunnableGraph<Source<String, NotUsed>> runnableGraph =
                producer.toMat(BroadcastHub.of(String.class, 256), Keep.right());

        // By running/materializing the producer, we get back a Source, which
        // gives us access to the elements published by the producer.
        Source<String, NotUsed> fromProducer = runnableGraph.run(system);

        // Print out messages from the producer in two independent consumers
        fromProducer.runForeach(msg -> System.out.println("consumer1: " + msg), system);
        fromProducer.runForeach(msg -> System.out.println("consumer2: " + msg), system);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        system.terminate();
    }


    //Combining dynamic operators to build a simple Publish-Subscribe service


    //PARTITION HUB

    static void t5() {
        // A simple producer that publishes a new "message-n" every second
        Source<String, Cancellable> producer =
                Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(1), "message")
                        .zipWith(Source.range(0, 100), (a, b) -> a + "-" + b);

        // Attach a PartitionHub Sink to the producer. This will materialize to a
        // corresponding Source.
        // (We need to use toMat and Keep.right since by default the materialized
        // value to the left is used)
        RunnableGraph<Source<String, NotUsed>> runnableGraph =
                producer.toMat(
                        PartitionHub.of(String.class, (size, elem) -> Math.abs(elem.hashCode() % size), 2, 256),
                        Keep.right());

        // By running/materializing the producer, we get back a Source, which
        // gives us access to the elements published by the producer.
        Source<String, NotUsed> fromProducer = runnableGraph.run(system);

        // Print out messages from the producer in two independent consumers
        fromProducer.runForeach(msg -> System.out.println("consumer1: " + msg), system);
        fromProducer.runForeach(msg -> System.out.println("consumer2: " + msg), system);
    }




    static void t6(){
/*        // A simple producer that publishes a new "message-n" every second
        Source<String, Cancellable> producer =
                Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(1), "message")
                        .zipWith(Source.range(0, 100), (a, b) -> a + "-" + b);

    // Attach a PartitionHub Sink to the producer. This will materialize to a
    // corresponding Source.
    // (We need to use toMat and Keep.right since by default the materialized
    // value to the left is used)
        RunnableGraph<Source<String, NotUsed>> runnableGraph =
                producer.toMat(
                        PartitionHub.ofStateful(String.class, (size, elem)  -> Math.abs(elem % size), 2, 256),
                        Keep.right());

    // By running/materializing the producer, we get back a Source, which
    // gives us access to the elements published by the producer.
        Source<String, NotUsed> fromProducer = runnableGraph.run(system);

    // Print out messages from the producer in two independent consumers
        fromProducer.runForeach(msg -> System.out.println("consumer1: " + msg), system);
        fromProducer.runForeach(msg -> System.out.println("consumer2: " + msg), system);*/
    }
}
