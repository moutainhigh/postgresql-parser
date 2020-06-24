package com.fiberhome.pgparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fiberhome.jdbc.JdbcTemplate;
import com.fiberhome.jdbc.JdbcTemplate2;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: pg消费kafka流程，copy方式
 * @author: ws
 * @time: 2020/4/30 10:26
 */
public class PgStreamParserImpl implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(PgStreamParserImpl.class);

    private static Properties properties;
    private KafkaConsumer<String, String> consumer;

    //存储record.value
    private List<String> buffer;
    //读取库中元数据
    private Map<String, String> metaDataMap;

    private List<String> topics;

    private static final long POLL_TIME_OUT = 100;

    /*private String sql = "with T as (\n" +
            "select M.tabname,M.attname,M.attnum,case when attrdef is null or position('nextval' in attrdef)=0 then 'f' else 't' end isseq\n" +
            "from ( \n" +
            "SELECT a.attrelid::regclass as tabname,a.attname,pg_catalog.format_type(a.atttypid, a.atttypmod),a.attnum,\n" +
            "( SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid, true) for 128) \n" +
            "   FROM pg_catalog.pg_attrdef d\n" +
            "   WHERE d.adrelid = a.attrelid AND d.adnum = a.attnum AND a.atthasdef ) as attrdef\n" +
            "  FROM pg_catalog.pg_attribute a \n" +
            " WHERE a.attrelid in (select oid from pg_class where relname in ('ws_test','ws_test1')) AND a.attnum > 0 AND NOT a.attisdropped \n" +
            " ) M \n" +
            " order by M.tabname,M.attnum \n" +
            ")\n" +
            "select T.tabname,string_agg(T.attname,',') as cols from T where T.isseq='f' group by T.tabname";*/

    /*private String sql = "with T as (\n" +
            "select M.tabname,M.attname,M.attnum,case when attrdef is null or position('nextval' in attrdef)=0 then 'f' else 't' end isseq\n" +
            "from ( \n" +
            "SELECT a.attrelid::regclass as tabname,a.attname,pg_catalog.format_type(a.atttypid, a.atttypmod),a.attnum,\n" +
            "( SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid, true) for 128) \n" +
            "   FROM pg_catalog.pg_attrdef d\n" +
            "   WHERE d.adrelid = a.attrelid AND d.adnum = a.attnum AND a.atthasdef ) as attrdef\n" +
            "  FROM pg_catalog.pg_attribute a \n" +
            " WHERE a.attrelid in (select oid from pg_class where relname in ('nb_tab_appstore','nb_tab_engine','nb_tab_map','nb_tab_mblog','nb_tab_mobilekeeper','nb_tab_netbank','nb_tab_other','nb_tab_p2p','nb_tab_proxy','nb_tab_rent','nb_tab_http','nb_tab_express','nb_tab_https','nb_tab_marry','nb_tab_auth','nb_tab_blog','nb_tab_ftp','nb_tab_im','nb_tab_communication','nb_tab_game','nb_tab_inputmethod','nb_tab_massphonebook','nb_tab_mms','nb_tab_multimediaimage','nb_tab_news','nb_tab_overnet','nb_tab_payment','nb_tab_remotectrl','nb_tab_devicedata','nb_tab_goods','nb_tab_location','nb_tab_email','nb_tab_hotel','nb_tab_resume','nb_tab_sms','nb_tab_sns','nb_tab_streammedia','nb_tab_taxi','nb_tab_telnet','nb_tab_terminalinfo','nb_tab_track','nb_tab_train','nb_tab_video','nb_tab_voice','nb_tab_voip','nb_tab_vpn','nb_tab_webbbs','nb_tab_webchat','nb_tab_webshare'\n" +
            ")) AND a.attnum > 0 AND NOT a.attisdropped \n" +
            " ) M \n" +
            " order by M.tabname,M.attnum \n" +
            ")\n" +
            "select T.tabname,string_agg(T.attname,',') as cols from T where T.isseq='f' group by T.tabname";*/

    public PgStreamParserImpl(String kafkaIp, String kafkaTopic, String kafkaGroup) {
        //读取库中元数据信息
        Properties pgProperties = PropertiesUtil.getProperties("config/pg-site.properties");
        String tableName = (String) pgProperties.get("table_name");
        String sql = "with T as (\n" +
                "select M.tabname,M.attname,M.attnum,case when attrdef is null or position('nextval' in attrdef)=0 then 'f' else 't' end isseq\n" +
                "from ( \n" +
                "SELECT a.attrelid::regclass as tabname,a.attname,pg_catalog.format_type(a.atttypid, a.atttypmod),a.attnum,\n" +
                "( SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid, true) for 128) \n" +
                "   FROM pg_catalog.pg_attrdef d\n" +
                "   WHERE d.adrelid = a.attrelid AND d.adnum = a.attnum AND a.atthasdef ) as attrdef\n" +
                "  FROM pg_catalog.pg_attribute a \n" +
                " WHERE a.attrelid in (select oid from pg_class where relname in (" +
                tableName + "\n" +
                ")) AND a.attnum > 0 AND NOT a.attisdropped \n" +
                " ) M \n" +
                " order by M.tabname,M.attnum \n" +
                ")\n" +
                "select T.tabname,string_agg(T.attname,',') as cols from T where T.isseq='f' group by T.tabname";

        metaDataMap = JdbcTemplate.getInstance().getMetaData(sql);

        topics = new ArrayList<>(30);

        PgStreamParserImpl.properties = PropertiesUtil.getProperties("config/consumer.properties");
        PgStreamParserImpl.properties.setProperty("bootstrap.servers", kafkaIp);
        PgStreamParserImpl.properties.setProperty("topic", kafkaTopic);
        PgStreamParserImpl.properties.setProperty("group.id", kafkaGroup);
        int maxPollRecords = Integer.parseInt(PgStreamParserImpl.properties.getProperty("max.poll.records"));
        buffer = new ArrayList<>(maxPollRecords);    //容量与max.poll.records相同

        //实例化Consumer对象，加载properties配置文件
        consumer = new KafkaConsumer<>(PgStreamParserImpl.properties);

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
        //轮询向kafka请求数据
        try {
            while(true) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_TIME_OUT);
                if ( records.count() != 0) {
//                    logger.info("records.count()：{}", records.count());
                    processPoll(records, metaDataMap);
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

    private void processPoll(ConsumerRecords<String, String> records, Map<String, String> metaDataMap) {
        //解析每条记录
        for (ConsumerRecord<String, String> record : records) {
//            System.out.println("线程为：" + Thread.currentThread().getName() + ",partition = " + record.partition() + ",offset = " + record.offset() + ",record.value() = " + record.value() + "--consumer = " + consumer.toString());
            if (!record.value().isEmpty()) {
                buffer.add(record.value());
            }
        }
        if(!buffer.isEmpty()) {

            copyToPg(buffer, metaDataMap);

            //采用异步+同步提交方式
            consumer.commitAsync();
            buffer.clear();
        }
    }


    /**
     * 利用copy方式入库
     * @param buffer
     * @param metaDataMap
     */
    public void copyToPg(List<String> buffer, Map<String, String> metaDataMap) {
        //***********************************处理数据及sql拼接*********************************************
        //存储key:表名(小写),value:sql
        Map<String, String> sqlMap = new ConcurrentHashMap<>(500);
        //存储key:表名(小写),value数据
        Map<Object, StringBuilder> valueMap = new ConcurrentHashMap<>(1000);
        StringBuilder dataBuilder = null;

        StringBuilder copySql = new StringBuilder(5000);
        for (int i = 0; i < buffer.size(); i++) {
            dataBuilder = new StringBuilder(10*1024);    //数据行长10KB
            //拼接sql
            String jsonData = (String) JSON.toJSON(buffer.get(i));
            JSONObject jsonObject = JSON.parseObject(jsonData); //3个key value

            //注意tablename要小写 否则containsKey取不到
            String tablename = jsonObject.get("tablename").toString().toLowerCase();

            //首次获取到的tablename数据存入sqlMap，bcp存入valueMap
            if(! sqlMap.containsKey(tablename)) {
                //1.拼接sql并存入sqlMap
                if (null != tablename) {
                    copySql.append("copy ")
                            .append(tablename)
                            .append("(")
                            .append(metaDataMap.get(tablename))
                            .append(") from stdin with (DELIMITER E'\\t',NULL '',ENCODING utf8)");
                }
                sqlMap.put(String.valueOf(tablename), String.valueOf(copySql));

                /*if (! copySql.toString().isEmpty()){
                    System.out.println(copySql.toString());
                }*/
                copySql.setLength(0);

                //2.获取dataBuilder并存入valueMap
                //得到data对应的value，如：{"sex":"男","id":"1","age":"18"}
                String dataStr = jsonObject.get("data").toString();
                //得到字段名，如：id,sex,age,province,qq,wx,jcontent
//                String fieldArray = metaDataMap.get(tablename).toUpperCase();       //这里要改成大写，但是注意 不通用
                String fieldArray = metaDataMap.get(tablename);     //得到元数据中小写的字段名拼接
                //json格式的dataStr,{"sex":"男","id":"1","age":"18"}
                JSONObject parseObject = JSON.parseObject(dataStr);     //parseObject 的key是大写 而field[j]值是小写

                //得到字段数组
                String[] field = fieldArray.split(",");     //小写字段数组
                //拼接bcp及jcontent字段数据
                for (int j = 0; j < field.length; j++) {
                    if(parseObject.containsKey(field[j].toUpperCase()) || parseObject.containsKey(field[j].toLowerCase())) {
                        dataBuilder.append(parseObject.get(field[j].toUpperCase())).append("\t");       //这个如何确认field字段大小写和parserObject中一致?
                    } else {
                        if (! "jcontent".equalsIgnoreCase(field[j])) {      //忽略大小写
                            dataBuilder.append("\t");
                        }
                    }
                }
                if (Arrays.asList(field).contains("jcontent") || Arrays.asList(field).contains("JCONTENT")) {
                    dataBuilder.append(dataStr).append("\n");
                } else {
                    dataBuilder.deleteCharAt(dataBuilder.length()-1);//此时dataaBuilder长度远超过field.length
                    dataBuilder.append("\n");
                }
//                System.out.println("dataBuilder=" + dataBuilder.toString());

                //将dataBuilder存入valueMap
                valueMap.put(tablename, dataBuilder);

            } else {    //将后续重复的bcp追加到valueMap中

                //2.获取dataBuilder并存入valueMap
                //得到data对应的value，如：{"sex":"男","id":"1","age":"18"}
                String dataStr = jsonObject.get("data").toString();
                //得到字段名，如：id,sex,age,province,qq,wx,jcontent
                String fieldArray = metaDataMap.get(tablename);
                //json格式的dataStr,{"sex":"男","id":"1","age":"18"}
                JSONObject parseObject = JSON.parseObject(dataStr);

                //得到字段数组
                String[] field = fieldArray.split(",");
                //拼接bcp及jcontent字段数据
                for (int j = 0; j < field.length; j++) {
                    if(parseObject.containsKey(field[j].toUpperCase()) || parseObject.containsKey(field[j].toLowerCase())) {
                        dataBuilder.append(parseObject.get(field[j].toUpperCase())).append("\t");
                    } else {
                        if (! "jcontent".equalsIgnoreCase(field[j])) {  //忽略大小写
                            dataBuilder.append("\t");
                        }
                    }
                }
                if (Arrays.asList(field).contains("jcontent") || Arrays.asList(field).contains("JCONTENT")) {
                    dataBuilder.append(dataStr).append("\n");
                } else {
                    dataBuilder.deleteCharAt(dataBuilder.length()-1);//此时dataaBuilder长度远超过field.length
                    dataBuilder.append("\n");
                }

                //将dataBuilder存入valueMap
                valueMap.put(tablename, valueMap.get(tablename).append(dataBuilder.toString()));
            }

        }

//        System.out.println("valueMap.size()=" + valueMap.size());
        long successNum = 0L;

        long startTime = System.currentTimeMillis();
//        logger.info("start copy......");
        for (Object tablename : valueMap.keySet()) {
//            System.out.println("dataBuilder=" + String.valueOf(valueMap.get(tablename)));
            long resultNum = JdbcTemplate.getInstance().copyToPg(sqlMap.get(tablename), String.valueOf(valueMap.get(tablename)));
            successNum +=resultNum;
        }
        long endTime = System.currentTimeMillis();
        logger.info("入库成功{}条，总耗时：{}ms", successNum, endTime - startTime);

    }

}
