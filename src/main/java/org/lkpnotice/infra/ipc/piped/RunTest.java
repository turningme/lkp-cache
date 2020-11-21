package org.lkpnotice.infra.ipc.piped;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

/**
 * Created by jpliu on 2020/11/20.
 */
public class RunTest {
    public static void main(String[] args){
        try{

            PipedReader pipedReader = new PipedReader();
            PipedWriter pipedWriter = new PipedWriter();

            //inputStream.connect(outputStream);
            pipedWriter.connect(pipedReader);   //将pipedWriter和pipeRead通过connect相连

            ThreadRead threadRead = new ThreadRead(pipedReader);    //启动读线程
            threadRead.start();

            Thread.sleep(2000);

            ThreadWrite threadWrite = new ThreadWrite(pipedWriter);  //启动写线程
            threadWrite.start();

        }catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
