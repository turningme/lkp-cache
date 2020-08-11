package org.lkpnotice.infra.reactiveSteamAkka;

import akka.stream.*;
import akka.stream.javadsl.*;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.util.ByteString;

import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;


/**
 * Created by jpliu on 2020/8/5.
 */
public class Main {

    public static void main(String[] argv) {
        test4();
    }


    static void test1(){
        final ActorSystem system = ActorSystem.create("QuickStart");
        // Code here
        final Source<Integer, NotUsed> source = Source.range(1, 100);

        final CompletionStage<Done> done = source.runForeach(i -> System.out.println(i), system);

        done.thenRun(() -> system.terminate());
    }


    static void test2(){
        final ActorSystem system = ActorSystem.create("QuickStart");
        // Code here
        final Source<Integer, NotUsed> source = Source.range(1, 100);
        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<IOResult> result =
                factorials
                        .map(num -> ByteString.fromString(num.toString() + "\n"))
                        .runWith(FileIO.toPath(Paths.get("factorials.txt")), system);

        result.thenRun(()->system.terminate());
    }


    static void test3(){
        final ActorSystem system = ActorSystem.create("QuickStart");
        Source.from(Arrays.asList(1, 2, 3))
                .map(
                        i -> {
                            System.out.println("A: " + i);
                            return i;
                        })
                .async()
                .map(
                        i -> {
                            System.out.println("B: " + i);
                            return i;
                        })
                .async()
                .map(
                        i -> {
                            System.out.println("C: " + i);
                            return i;
                        })
                .async()
                .runWith(Sink.ignore(), system).thenRun(()->system.terminate());
    }


    static void test4(){
        final ActorSystem system = ActorSystem.create("QuickStart");
        Source.range(1,10)
                .map(
                        i -> {
                            System.out.println("A: " + i);
                            return i;
                        })
                .map(
                        i -> {
                            System.out.println("B: " + i);
                            return i;
                        })
                .map(
                        i -> {
                            System.out.println("C: " + i);
                            return i;
                        })
                .runWith(Sink.ignore(), system).thenRun(()->system.terminate());
    }
}
