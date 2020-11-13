package org.lkpnotice.infra.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Created by jpliu on 2020/11/6.
 */
public class KafkaTest {

    private static final String TOPIC = "my-replicated-topic5"; //kafka创建的topic
    private static final String CONTENT = "This is a single message"; //要发送的内容
    private static final String BROKER_LIST = "127.0.0.1:9092"; //broker的地址和端口
    private static final String SERIALIZER_CLASS = "kafka.serializer.StringEncoder"; // 序列化类


    public static void main(String[] args) {
        // 构造一个java.util.Properties对象
        Properties props = new Properties();
        // 指定bootstrap.servers属性。必填，无默认值。用于创建向kafka broker服务器的连接。
        props.put("bootstrap.servers", "127.0.0.1:9092");
        // 指定key.serializer属性。必填，无默认值。被发送到broker端的任何消息的格式都必须是字节数组。
        // 因此消息的各个组件都必须首先做序列化，然后才能发送到broker。该参数就是为消息的key做序列化只用的。
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 指定value.serializer属性。必填，无默认值。和key.serializer类似。此被用来对消息体即消息value部分做序列化。
        // 将消息value部分转换成字节数组。
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //acks参数用于控制producer生产消息的持久性（durability）。参数可选值，0、1、-1（all）。
        props.put("acks", "-1");
        //props.put(ProducerConfig.ACKS_CONFIG, "1");
        //在producer内部自动实现了消息重新发送。默认值0代表不进行重试。
        props.put("retries", 3);
        //props.put(ProducerConfig.RETRIES_CONFIG, 3);
        //调优producer吞吐量和延时性能指标都有非常重要作用。默认值16384即16KB。
        props.put("batch.size", 323840);
        //props.put(ProducerConfig.BATCH_SIZE_CONFIG, 323840);
        //控制消息发送延时行为的，该参数默认值是0。表示消息需要被立即发送，无须关系batch是否被填满。
        props.put("linger.ms", 10);
        //props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        //指定了producer端用于缓存消息的缓冲区的大小，单位是字节，默认值是33554432即32M。
        props.put("buffer.memory", 33554432);
        //props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put("max.block.ms", 3000);

        Producer<String, String> producer = new KafkaProducer<>(props);


        for (int i = 0; i < 100; i++) {
            //构造好kafkaProducer实例以后，下一步就是构造消息实例。
            producer.send(new ProducerRecord<>("test_jp_1", Integer.toString(i), Integer.toString(i)));
            // 构造待发送的消息对象ProduceRecord的对象，指定消息要发送到的topic主题，分区以及对应的key和value键值对。
            // 注意，分区和key信息可以不用指定，由kafka自行确定目标分区。
            //ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("my-topic",
            //        Integer.toString(i), Integer.toString(i));
            // 调用kafkaProduce的send方法发送消息
            //producer.send(producerRecord);
        }
        System.out.println("消息生产结束......");
        // 关闭kafkaProduce对象
        producer.close();
        System.out.println("关闭生产者......");
    }
}
