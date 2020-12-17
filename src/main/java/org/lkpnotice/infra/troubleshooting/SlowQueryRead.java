package org.lkpnotice.infra.troubleshooting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpliu on 2020/12/11.
 */
public class SlowQueryRead {
    static List<String>  buffer = new ArrayList<>();
    static boolean  reserved = false;

    public static void main(String[] args){
        try {
            readAndparse();

            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void readAndparse() throws IOException {
        String dir = "/Users/jpliu/troubleshoot";
        String fName = "mysqld-slow.log.20201209";

        File f = new File(dir,fName);

        FileReader  fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String tmp = null;
        while ((tmp=br.readLine())!= null){
            parseBuffered(tmp);
        }

        br.close();
    }



    static void parseBuffered(String line) {
        if (line.startsWith("# User@Host")) {
            if (reserved) {
                //output the buffered
                output(buffer);
            }

            buffer.clear();
            reserved = false;


            // match with host
            if (line.contains("10.254.13.148")) {
                reserved = true;
            }
        }

        buffer.add(line);
    }


    static PrintWriter pw = new PrintWriter(System.out);
    static void  output(List<String> input){
        pw.println();
        for (String s:input
             ) {
            pw.println(s);
        }
    }
}
