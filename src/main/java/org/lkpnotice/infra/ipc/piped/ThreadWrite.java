package org.lkpnotice.infra.ipc.piped;

import java.io.IOException;
import java.io.PipedWriter;

/**
 * Created by jpliu on 2020/11/20.
 */
public class ThreadWrite  extends Thread{
    private PipedWriter out;

    public ThreadWrite(PipedWriter out){
        super();
        this.out = out;
    }
    @Override
    public void run(){
        try{
            System.out.println("write:");
            for(int i=0;i<300;i++){
                String outData = ""+(i+1);
                out.write(outData);
                System.out.print(outData);
            }
            System.out.println();
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
