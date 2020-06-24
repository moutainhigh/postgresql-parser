package com.fiberhome.pgparser;

import com.fiberhome.utils.PropertiesUtil;

import java.util.Properties;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 启动类
 * @author: ws
 * @time: 2020/4/29 14:26
 */
public class ThreadMain {
    private static Properties properties;
    private static ThreadPoolExecutor threadPool;

    public static void main(String[] args) {
        //从pg-site中读取配置
        properties = PropertiesUtil.getProperties("config/pg-site.properties");

        String kafkaIp = properties.getProperty("bootstrap.servers");
        String kafkaTopic = properties.getProperty("kafka.topic");
        String kafkaGroup = properties.getProperty("kafka.group");
        String threadNumber = properties.getProperty("thread_number");
        int threads = Integer.parseInt(threadNumber.trim());

        threadPool = new ThreadPoolExecutor(threads,threads,1, TimeUnit.MINUTES, new SynchronousQueue<>());

        //多消费线程模式1
        for (int i = 0; i < threads; i++) {
            threadPool.execute(new PollThreadHandler(kafkaIp, kafkaTopic, kafkaGroup));
//            threadPool.execute(new PgStreamParserImpl(kafkaIp, kafkaTopic, kafkaGroup));
        }

        //多消费线程模式2
        /*List<PollThreadHandler> consumers = new ArrayList<>();

        for (int i = 0; i < threadNumber; i++) {
            PollThreadHandler consumer = new PollThreadHandler(topic, groupId);
//            consumers.add(consumer);
            threadPool.submit(consumer);
        }*/
    }

}
