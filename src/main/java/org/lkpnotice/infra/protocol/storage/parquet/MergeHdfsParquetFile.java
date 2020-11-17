package org.lkpnotice.infra.protocol.storage.parquet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;

/**
 * Created by jpliu on 2020/11/15.
 */
public class MergeHdfsParquetFile {
    private static FileSystem fileSystem;

    static {
        System.setProperty("HADOOP_USER_NAME","root");
        try {
            fileSystem = FileSystem.get(getConfiguration());
        } catch (IOException e) {
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        mergeParquet("/tmp/merge");
    }


    private static void mergeParquet(String dir) throws Exception {
        MessageType messageType = checkSchemaSame(dir);
        if(messageType == null){//MessageType不一致
            return;
        }
        List<Path> parquetPaths = getParquetPaths(dir);

        String dest = dir + "/merge-" + System.currentTimeMillis() + ".parquet";
        Path destPath = new Path(dest);
        ParquetWriter parquetWriter = getParquetWriter(messageType, destPath);
        ParquetReader<Group> parquetReader;
        Group book;
        for(Path path : parquetPaths) {
            parquetReader = getParquetReader(path);
            while ((book = parquetReader.read()) != null) {
                parquetWriter.write(book);
            }
        }
        parquetWriter.close();
        if(fileSystem.exists(destPath)){
            FileStatus fileStatus = fileSystem.getFileStatus(destPath);
            if(fileStatus.getLen() <= 1024){
                System.err.println(dir + "files len to small pleach ack need delete");
            }else {
                for(Path path : parquetPaths){
                    fileSystem.delete(path,false);
                }
            }
        }
    }

    public static List<Path> getParquetPaths(String dir) throws Exception {
        Path dirPath = new Path(dir);
        RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator = fileSystem.listFiles(dirPath, false);
        List<Path> fileList = new ArrayList<Path>();
        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus next = locatedFileStatusRemoteIterator.next();
            Path path = next.getPath();
            FileStatus fileStatus = fileSystem.getFileStatus(path);
            if(fileStatus.isFile() && path.getName().endsWith(".parquet")) {//如果是parquet文件
                fileList.add(path);
            }
        }
        return fileList;
    }

    private static MessageType checkSchemaSame(String dir) throws Exception {
        List<MessageType> groupTypes = getMessageType(dir);
        int size = groupTypes.size();
        if(size == 0 || size == 1){//0个和1个都不处理
            return null;
        }
        MessageType groupType = groupTypes.get(0);
        for(MessageType gt : groupTypes){
            if(!groupType.equals(gt)){
                return null;
            }
        }
        return groupType;
    }

    private static List<MessageType> getMessageType(String dir) throws Exception {
        List<Path> parquetPaths = getParquetPaths(dir);
        LinkedList<MessageType> groupTypes = new LinkedList<>();
        for(Path path : parquetPaths){
            groupTypes.add(getMessageType(path));
        }
        return groupTypes;
    }

    public static MessageType getMessageType(Path path) throws IOException {
        HadoopInputFile hadoopInputFile = HadoopInputFile.fromPath(path, getConfiguration());
        ParquetFileReader parquetFileReader = ParquetFileReader.open(hadoopInputFile, ParquetReadOptions.builder().build());
        ParquetMetadata metaData = parquetFileReader.getFooter();
        MessageType schema = metaData.getFileMetaData().getSchema();
        parquetFileReader.close();
        return schema;
    }

    private static Configuration getConfiguration(){
        Configuration configuration = new Configuration();
        //path中就不用加hdfs://127.0.0.1:9000了
        configuration.set(FileSystem.FS_DEFAULT_NAME_KEY, "hdfs://127.0.0.1:9000");
        return configuration;
    }

    public static ParquetReader getParquetReader(Path path) throws IOException {
        GroupReadSupport readSupport = new GroupReadSupport();
        ParquetReader.Builder<Group> builder = ParquetReader.builder(readSupport, path);
        builder.withConf(getConfiguration());
        ParquetReader<Group> parquetReader =builder.build();
        return parquetReader;
    }

    public static ParquetWriter getParquetWriter(MessageType schema, Path path) throws IOException {
        ExampleParquetWriter.Builder writebuilder = ExampleParquetWriter.builder(path);
        writebuilder.withWriteMode(ParquetFileWriter.Mode.CREATE);
        writebuilder.withCompressionCodec(CompressionCodecName.SNAPPY);
        writebuilder.withConf(getConfiguration());
        writebuilder.withType(schema);
        ParquetWriter writer = writebuilder.build();
        return writer;
    }
}
