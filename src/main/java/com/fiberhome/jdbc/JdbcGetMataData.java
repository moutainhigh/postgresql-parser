package com.fiberhome.jdbc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kafka.utils.Json;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import scala.annotation.meta.field;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 描述
 * @author: ws
 * @time: 2020/5/28 9:03
 */
public class JdbcGetMataData {
    Connection conn = null;
    String tableName;


    public static void main(String[] args) {
        String originPath = System.getProperty("user.dir") + File.separator +  "data_50_json";

        String sql = "with T as (\n" +
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
                "select T.tabname,string_agg(T.attname,',') as cols from T where T.isseq='f' group by T.tabname";
        Map<String, String> metaDataMap = JdbcTemplate.getInstance().getMetaData(sql);

//        for (Map.Entry<String, String> entry : metaDataMap.entrySet()) {
//            System.out.println("entry.getKey()=" + entry.getKey() + "-------entry.getValue()=" + entry.getValue());
//        }

        //***********************************1.处理数据及sql拼接*********************************************
        //存储key:表名,value:sql
        Map<String, String> sqlMap = new ConcurrentHashMap<>();
        //存储key:表名,value数据
        Map<Object, StringBuilder> valueMap = new ConcurrentHashMap<>();
        List<String> list = new ArrayList<>();
        list.add("{\"tablename\":\"ws_test\",\"data\":{\"id\":\"1\",\"sex\":\"男\",\"age\":\"18\"}}");
        list.add("{\"data\":{\"age\":\"75\",\"id\":\"56\",\"province\":\"安徽省\",\"qq\":\"3741058842\",\"sex\":\"男\",\"wx\":\"13286425820\"},\"tablename\":\"ws_test\"}");

        /*try {
            List<String> lines = FileUtils.readLines(new File(originPath), "UTF-8");
            for (String line : lines) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        StringBuilder copySql = new StringBuilder(500);
        for (int i = 0; i < list.size(); i++) {
            StringBuilder dataBuilder = new StringBuilder(50000000);
            //拼接sql
            String data = (String) JSON.toJSON(list.get(i));
            JSONObject jsonObject = JSON.parseObject(data);

            Object tablename = jsonObject.get("tablename");

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


                //*********************2.获取dataBuilder并存入valueMap**********************************
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
                    if(parseObject.containsKey(field[j])) {
                        dataBuilder.append(parseObject.get(field[j])).append("\t");
                    } else {
                        if (! "jcontent".equals(field[j])) {
                            dataBuilder.append("\t");
                        }
                    }
                }
                if (Arrays.asList(field).contains("jcontent")) {
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
                    if(parseObject.containsKey(field[j])) {
                        dataBuilder.append(parseObject.get(field[j])).append("\t");
                    } else {
                        if (! "jcontent".equals(field[j])) {
                            dataBuilder.append("\t");
                        }
                    }
                }
                if (Arrays.asList(field).contains("jcontent")) {
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

        for (Object tablename : valueMap.keySet()) {
            JdbcTemplate2.getInstance().copyToPg3(sqlMap.get(tablename), String.valueOf(valueMap.get(tablename)));
        }

    }


}
