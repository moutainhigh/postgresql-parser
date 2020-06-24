package com.fiberhome.pgparser;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @description: 生产者，往kafka中push数据
 * @author: ws
 * @time: 2020/4/29 10:54
 */
public class ProducerAPI {
    private static final Logger logger = LoggerFactory.getLogger(ProducerAPI.class);

//    String originPath = System.getProperty("user.dir") + File.separator + "jsontent_5";
//    String originPath = System.getProperty("user.dir") + File.separator + "data_50_json";
    String originPath = System.getProperty("user.dir") + File.separator  +  "data_10w.json";
    private static Properties props;
    private Producer<String, String> producer;

    static {
        props = new Properties();
        InputStream inputStream = ProducerAPI.class.getClassLoader().getResourceAsStream("config/producer.properties");
        try {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String topic) {
        //实例化producer对象
        producer = new KafkaProducer<String, String>(props);

        try {
//            String jsonStr = FileUtils.readFileToString(new File(originPath), "UTF-8");
            List<String> lines = FileUtils.readLines(new File(originPath), "UTF-8");

            long startTime = System.currentTimeMillis();
            for (String line : lines) {

                //消息封装
                ProducerRecord<String, String> record = new ProducerRecord(topic, line);
                producer.send(record);

            }
            long endTime = System.currentTimeMillis();
            logger.info(Thread.currentThread().getName() + "生产共耗时：" + (endTime - startTime) + "ms");

        } catch (IOException e) {
            logger.error("数据转换错误：" + e.getMessage());
        } finally {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            producer.close();
            logger.info("kafka消息生产结束");

        }


        //消息发送（生产）
        /*for (int i = 0; i < 10000; i++) {
            producer.send(record);
        }*/
//        producer.send(record);

        /*try {
            Thread.sleep(100);
            producer.close();
        } catch (InterruptedException e) {
            logger.info("kafka消息生产结束");
        }*/

    }


}
