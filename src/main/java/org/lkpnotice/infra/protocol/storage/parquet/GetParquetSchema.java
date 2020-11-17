package org.lkpnotice.infra.protocol.storage.parquet;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.*;
/**
 * Created by jpliu on 2020/11/13.
 */
public class GetParquetSchema {
    public static MessageType getMessageTypeFromString (){
        String schemaString = "message Book {\n" +
                "  required binary bookName (UTF8);\n" +
                "  required boolean market;\n" +
                "  required double price;\n" +
                "  repeated group author {\n" +
                "    required binary name (UTF8);\n" +
                "    required int32 age;\n" +
                "  }\n" +
                "}";
        MessageType schema = MessageTypeParser.parseMessageType(schemaString);
        return schema;
    }

    public static MessageType getMessageTypeFromCode(){
        MessageType messageType = Types.buildMessage()
                .required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("bookName")
                .required(PrimitiveType.PrimitiveTypeName.BOOLEAN).named("market")
                .required(PrimitiveType.PrimitiveTypeName.DOUBLE).named("price")
                .requiredGroup()
                .required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("name")
                .required(PrimitiveType.PrimitiveTypeName.INT32).named("age")
                .named("author")
                .named("Book");
        System.out.println(messageType.toString());
        return messageType;
    }



    public static MessageType getMessageType(Path path,Configuration configuration) throws IOException {
        HadoopInputFile hadoopInputFile = HadoopInputFile.fromPath(path, configuration);
        ParquetFileReader parquetFileReader = ParquetFileReader.open(hadoopInputFile, ParquetReadOptions.builder().build());
        ParquetMetadata metaData = parquetFileReader.getFooter();
        MessageType schema = metaData.getFileMetaData().getSchema();
        //记得关闭
        parquetFileReader.close();
        return schema;
    }

/*    public static void main(String[] args) throws Exception{
        //本地文件
        String localPath = "file:///D:\\tmp\\parquet\\book.parquet";
        //hdfs文件
        String hdfsPath = "/tmp/parquet/book.parquet";

        Configuration localConfiguration = new Configuration();

        Configuration hdfsConfiguration = new Configuration();
//        hdfsConfiguration.set(FileSystem.FS_DEFAULT_NAME_KEY, "hdfs://192.168.8.206:9000");

        MessageType newMessageType = getMessageType(localPath,localConfiguration);
        System.out.println(newMessageType);
        System.out.println("--------------");
        newMessageType = getMessageType(hdfsPath,hdfsConfiguration);
        System.out.println(newMessageType);
//        getMessageTypeFromCode();
//        getMessageTypeFromString();
    }*/


}
