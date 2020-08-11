package org.lkpnotice.infra.reactiveSteamAkka;

/**
 * Created by jpliu on 2020/8/7.
 */
public class AkkaStreamIo {
    public static void main(String[] args){

    }



    static void t1(){
       /* import akka.stream.scaladsl.Framing

        val connections: Source[IncomingConnection, Future[ServerBinding]] =
        Tcp().bind(host, port)
        connections runForeach { connection =>
            println(s"New connection from: ${connection.remoteAddress}")

            val echo = Flow[ByteString]
                    .via(Framing.delimiter(
                            ByteString("\n"),
                            maximumFrameLength = 256,
                            allowTruncation = true))
                    .map(_.utf8String)
                    .map(_ + "!!!\n")
                    .map(ByteString(_))

            connection.handleWith(echo)
        }*/
    }


    static void t2(){
/*        val connection = Tcp().outgoingConnection("127.0.0.1", 8888)

        val replParser =
                Flow[String].takeWhile(_ != "q")
                        .concat(Source.single("BYE"))
                        .map(elem => ByteString(s"$elem\n"))

        val repl = Flow[ByteString]
                .via(Framing.delimiter(
                        ByteString("\n"),
                        maximumFrameLength = 256,
                        allowTruncation = true))
                .map(_.utf8String)
                .map(text => println("Server: " + text))
        .map(_ => readLine("> "))
        .via(replParser)

        connection.join(repl).run()*/
    }


}
