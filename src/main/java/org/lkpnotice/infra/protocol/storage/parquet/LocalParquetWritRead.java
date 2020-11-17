package org.lkpnotice.infra.protocol.storage.parquet;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;

/**
 * Created by jpliu on 2020/11/13.
 */
public class LocalParquetWritRead {
    public static final String DATA_PATH = "/tmp/parquet_test/book.parquet";

    private static String schemaStr = "message Book {\n" +
            "  required binary bookName (UTF8);\n" +
            "  required boolean market;\n" +
            "  required double price;\n" +
            "  repeated group author {\n" +
            "    required binary name (UTF8);\n" +
            "    required int32 age;\n" +
            "  }\n" +
            "}";


    private final static MessageType schema = MessageTypeParser.parseMessageType(schemaStr);


    public static void main(String[] args) throws IOException {
//       write();
        read();
    }


    public static void write() throws IOException {
        Path path = new Path(DATA_PATH);
        Configuration configuration = new Configuration();
        ExampleParquetWriter.Builder builder = ExampleParquetWriter
                .builder(path).withWriteMode(ParquetFileWriter.Mode.CREATE)
                .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_1_0)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withConf(configuration)
                .withType(schema);

        ParquetWriter<Group> writer = builder.build();
        SimpleGroupFactory groupFactory = new SimpleGroupFactory(schema);

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Group group = groupFactory.newGroup();
            group.append("bookName","bookName" + i)
                    .append("market",random.nextBoolean())
                    .append("price",random.nextDouble())
                    .addGroup("author")
                    .append("name","aname" + i)
                    .append("age",18 + random.nextInt(72));
            writer.write(group);
        }
        writer.close();
    }

    public static void read() throws IOException {
        Path path = new Path(DATA_PATH);
        ParquetReader.Builder<Group> builder = ParquetReader.builder(new GroupReadSupport(), path);
        ParquetReader<Group> reader = builder.build();
        Group group;
        while ((group = reader.read()) != null){
            System.out.println("schema:" + group.getType().toString());
            System.out.println(group.getString("bookName",0));
            System.out.println(group.getBoolean("market",0));
            System.out.println(group.getDouble("price",0));

            Group author = group.getGroup("author", 0);
            System.out.println(author.getString("name",0));
            System.out.println(author.getInteger("age",0));
        }
    }




}
