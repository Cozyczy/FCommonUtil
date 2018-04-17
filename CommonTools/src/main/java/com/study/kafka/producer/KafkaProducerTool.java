package com.study.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * kafka 1.0 producer   1.֧���������  2.֧����ָ��partition������Ϣ
 *
 * producer���̰߳�ȫ�ģ����̹߳�����������ʵ��ͨ����ӵ�ж��ʵ�����졣
 * �ٷ�API ��http://kafka.apache.org/11/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
 */
public class KafkaProducerTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerTool.class);

    private BlockingDeque<ProducerRecord<byte[], byte[]> > msgBlockingDeque = null;
    private volatile static KafkaProducerTool kafkaProducerTool = null;

    private Producer<byte[], byte[]> producer;
    private String topic;
    private Properties props = new Properties();

    private static int queueSize = 1000;

    public static KafkaProducerTool getInstance(String brokerList, String topic,int queueSize) {
        if (kafkaProducerTool == null) {
            synchronized (KafkaProducerTool.class) {
                if (kafkaProducerTool == null) {
                    kafkaProducerTool = new KafkaProducerTool(brokerList, topic ,queueSize);
                }
                return kafkaProducerTool;
            }
        }
        return kafkaProducerTool;
    }

    public static KafkaProducerTool getInstance(String brokerList, String topic) {
        if (kafkaProducerTool == null) {
            synchronized (KafkaProducerTool.class) {
                if (kafkaProducerTool == null) {
                    kafkaProducerTool = new KafkaProducerTool(brokerList, topic ,queueSize);
                }
                return kafkaProducerTool;
            }
        }
        return kafkaProducerTool;
    }


    /**
     * ������Ϣ
     * @param key
     * @param value
     * @return
     */
    public boolean append(String key,byte[] value) {
        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord(topic,key.getBytes("UTF-8"),value);
            return msgBlockingDeque.offer(record, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("Append Message To Queue Error",e);
        }
        return false;
    }

    /**
     * ָ��parttion ������Ϣ
     * @param key
     * @param value
     * @param partition
     * @return
     */
    public boolean append(String key,byte[] value,int partition) {
        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord(topic,partition,key.getBytes("UTF-8"),value);
            return msgBlockingDeque.offer(record, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("Append Message To Queue Error", e);
        }
        return false;
    }

    private KafkaProducerTool(String brokerList, String topic,int queueSize){
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);//��Ϣ��������С
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);//send message without delay
        props.put(ProducerConfig.ACKS_CONFIG, "all");//��Ӧpartition��followerд�����غ�ŷ��سɹ���
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.study.kafka.producer.partition.ItemPartitioner");
        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("retries", 0);//������Ϣʧ�ܲ�����

        /**
         * �ݵ�producer ��producer.send���߼����ݵȵģ���������ͬ��Kafka��Ϣ��broker�˲����ظ�д����Ϣ��kafka��֤�ײ���־ֻ�־û�һ�Ρ�
         *               �ݵ��Կ��Լ���ؼ�������consumerϵͳʵ����Ϣȥ�صĹ�����������������������ƣ�
         *               1.����֤��������ݵ��� 2.����֤��Ựʵ���ݵ��ԣ�ͬһ��producer��������Ҳ����֤��
         * ֻ�����ݵ�produce��enable.idempotence=true
         */
        //props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        /**
         * �����͵�producer:����һ��Ӧ�÷�����Ϣ�����partitions����topics,transactional��֤ԭ���Ե�д�뵽�������
         * ʹ��transaction����Ҫ��֤retries != 0 && acks = all (����follower����ȷ�Ͻ��յ�����) && enable.idempotence=true
         */
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");

        this.topic = topic;
        msgBlockingDeque = new LinkedBlockingDeque<>(queueSize);
        initKafkaConfig();
    }

    private void initKafkaConfig() {
        producer = new KafkaProducer(props);
        Thread thread = new Thread(new SendMesToKafkaRunnable());
        thread.setDaemon(true);
        thread.setName("SendMesToKafkaRunnable");
        thread.start();
    }

    class SendMesToKafkaRunnable implements Runnable {
        @Override
        public void run() {

            while (true) {
                try {
                    try {
                        ProducerRecord<byte[], byte[]> record = msgBlockingDeque.poll(10, TimeUnit.MILLISECONDS);
                        if(record != null){
                            //send������async�ģ�futureģʽ����ÿ�ε�������һ����¼���ӵ�����ʱ���������ء���������producer�����и���ļ�¼������һ���ͣ���������ܡ�
                            producer.send(record);
                        }
                    } catch (KafkaException e) {
                        LOGGER.error("Producer Send Message To Kafka Error",e);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Poll From Queue Error",e);
                }
            }

        }
    }
}
