package com.fiberhome.pgparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fiberhome.jdbc.JdbcTemplate;
import com.fiberhome.utils.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @description: pg消费kafka流程，insert方式
 * @author: ws
 * @time: 2020/4/30 10:26
 */
public class PollThreadHandler implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(PollThreadHandler.class);

    private static Properties properties;
    private KafkaConsumer<String, String> consumer;

    //存储record.value
    private List<String> buffer;
//    private StringBuilder sql;
    //存储字段名、字段值
//    private Map<String, String> valueMap;
    //配置表信息
    private Map<String, String> tableMap;

    private List<String> topics;

    private static final long POLL_TIME_OUT = 100;

    public PollThreadHandler(String kafkaIp, String kafkaTopic, String kafkaGroup) {
        topics = new ArrayList<>(30);
//        sql = new StringBuilder(5000);
//        valueMap = new HashMap<>(100);
        tableMap = new HashMap<>(200);

        properties = PropertiesUtil.getProperties("config/consumer.properties");

        properties.setProperty("bootstrap.servers", kafkaIp);
        properties.setProperty("topic", kafkaTopic);
        properties.setProperty("group.id", kafkaGroup);

        int maxPollRecords = Integer.parseInt(properties.getProperty("max.poll.records"));
        buffer = new ArrayList<>(maxPollRecords);    //容量与max.poll.records相同

        //实例化Consumer对象，加载properties配置文件
        consumer = new KafkaConsumer<>(properties);

        String[] topicArray = kafkaTopic.split(",");
        for (int i = 0, num= topicArray.length; i < num; i++) {
            topics.add(topicArray[i]);
        }

//        List<String> topics = Arrays.asList(kafkaTopic);
        //订阅group.id消费的topic
        consumer.subscribe(topics);
        logger.info("Start to consume: {}", kafkaTopic);

    }

    @Override
    public void run() {
        Properties pgProperties = PropertiesUtil.getProperties("config/pg-site.properties");
        try {
            String table_conf_path = pgProperties.getProperty("table_conf_path");
            List<String> lines = FileUtils.readLines(new File(table_conf_path), "UTF-8");
            logger.info("配置读取成功!");
            for (String line : lines) {
                JSONObject jsonObject = JSON.parseObject(line);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    String value = ((String) entry.getValue()).toLowerCase();
                    tableMap.put(key, value);
                }
            }

        } catch (IOException e) {
            logger.error("表配置文件读取失败！");
        }

        //轮询向kafka请求数据
        try {
            while(true) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_TIME_OUT);
                if ( records.count() != 0) {
//                    logger.info("records.count()：{}", records.count());
                    processPoll(records, tableMap, pgProperties);
                }
                //释放records对象
                records = null;
            }
        } catch (Exception e) {
            logger.error("Unexpected error", e.getMessage());
        } finally {
            consumer.commitSync();
            consumer.close();
            logger.info("consumer closed!");
        }

    }

    private void processPoll(ConsumerRecords<String, String> records, Map<String, String> tableMap, Properties pgProperties) {
        //解析每条记录
        for (ConsumerRecord<String, String> record : records) {
            if (!record.value().isEmpty()) {
                buffer.add(record.value());
            }
        }
        if(!buffer.isEmpty()) {
//            logger.info("单次poll缓存的数据量为：{}", buffer.size());
            //获取事务提交方式
            String autoCommit = pgProperties.getProperty("autoCommit");
            JdbcTemplate.getInstance().insertPg(buffer, tableMap, autoCommit);
            //采用异步+同步提交方式
            consumer.commitAsync();
            buffer.clear();
        }
    }

}
