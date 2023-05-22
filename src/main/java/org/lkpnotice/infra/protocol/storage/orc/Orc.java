package org.lkpnotice.infra.protocol.storage.orc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.MapColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Orc {
    public static void main(String[] args) throws IOException {
        Path testFilePath = new Path("/tmp/advanced-example.orc");
        Configuration conf = new Configuration();

        TypeDescription schema =
                TypeDescription.fromString("struct<r_name:string," +
                        "r_comment:string,r_nations:map<string,string>>");

        Writer writer =
                OrcFile.createWriter(testFilePath,
                        OrcFile.writerOptions(conf).setSchema(schema));

        VectorizedRowBatch batch = schema.createRowBatch();
        BytesColumnVector first = (BytesColumnVector) batch.cols[0];
        BytesColumnVector second = (BytesColumnVector) batch.cols[1];


       //Define map. You need also to cast the key and value vectors
        MapColumnVector map = (MapColumnVector) batch.cols[2];
        BytesColumnVector mapKey = (BytesColumnVector) map.keys;
        BytesColumnVector mapValue = (BytesColumnVector) map.values;

        // Each map has 5 elements
        final int MAP_SIZE = 5;
        final int BATCH_SIZE = batch.getMaxSize();

        // Ensure the map is big enough
        mapKey.ensureSize(BATCH_SIZE * MAP_SIZE, false);
        mapValue.ensureSize(BATCH_SIZE * MAP_SIZE, false);

// add 1500 rows to file
        for(int r=0; r < 1500; ++r) {
            int row = batch.size++;

            first.setVal(row, (r + "xx ").getBytes(StandardCharsets.UTF_8));
            //first.vector[row] = (r + "xx ").getBytes(StandardCharsets.UTF_8);
            second.setVal(row,(r * 3 + "yy ").getBytes(StandardCharsets.UTF_8));
            //second.vector[row] = (r * 3 + "yy ").getBytes(StandardCharsets.UTF_8);

            map.offsets[row] = map.childCount;
            map.lengths[row] = MAP_SIZE;
            map.childCount += MAP_SIZE;

            for (int mapElem = (int) map.offsets[row];
                 mapElem < map.offsets[row] + MAP_SIZE; ++mapElem) {
                String key = "row " + r + "." + (mapElem - map.offsets[row]);
                mapKey.setVal(mapElem, key.getBytes(StandardCharsets.UTF_8));
                mapValue.setVal(mapElem,  (mapElem + "zz").getBytes(StandardCharsets.UTF_8));
                //mapValue.vector[mapElem] = (mapElem + "zz").getBytes(StandardCharsets.UTF_8);
            }
            if (row == BATCH_SIZE - 1) {
                writer.addRowBatch(batch);
                batch.reset();
            }
        }
        if (batch.size != 0) {
            writer.addRowBatch(batch);
            batch.reset();
        }
        writer.close();
    }
}
