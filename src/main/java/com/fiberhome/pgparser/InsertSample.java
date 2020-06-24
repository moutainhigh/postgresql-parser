package com.fiberhome.pgparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 描述
 * @author: ws
 * @time: 2020/4/29 10:27
 */
public class InsertSample {
    private static final Logger logger = LoggerFactory.getLogger(InsertSample.class);

    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://172.16.108.7:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static void main(String[] args) {

        String originPath = System.getProperty("user.dir") + File.separator + "table_conf";

        /*try {
            String jsonStr = FileUtils.readFileToString(new File(originPath), "UTF-8");

            List<String> lines = FileUtils.readLines(new File(originPath), "UTF-8");
            long startTime = System.currentTimeMillis();
            for (String line : lines) {
                JSONObject jsonObject = JSON.parseObject(line);
                Object nameResult = jsonObject.get("name");
                Object sexResult = jsonObject.get("sex");
                Object ageResult = jsonObject.get("age");

                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs =null ;

                Class.forName(DRIVER);
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                String insertSql = "insert into ws_test (name,sex,age) values(?, ?, ?)";

                //创建sql命令对象
                ps = conn.prepareStatement(insertSql);

                ps.setObject(1, nameResult);
                ps.setObject(2, sexResult);
                ps.setObject(3, Integer.parseInt(String.valueOf(ageResult)));
                ps.addBatch();

                ps.executeBatch();
//            conn.commit();

            }
            long endTime = System.currentTimeMillis();
            System.out.println("sql入库耗时为：" + (endTime - startTime) + "ms");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }*/

        List<String> lines = null;
        List<String> tableList = new ArrayList<>();
        List<String> fieldList = new ArrayList<>();
        Map<String, String> tableMap = new ConcurrentHashMap<>();
        try {
            lines = FileUtils.readLines(new File(originPath), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();
        for (String line : lines) {
            JSONObject jsonObject = JSON.parseObject(line);
            System.out.println("==============");
            System.out.println(jsonObject.get("haas1a"));
            if (jsonObject.get(null) != null){
                System.out.println("ok");
            } else {
                System.out.println("为空");
            }

            /*if(StringUtils.isNotEmpty((CharSequence) jsonObject.get("ws_test111"))) {
                System.out.println("不是空");

            }else {
                System.out.println("kong");
            }
*/

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                String value = (String) entry.getValue();
                System.out.println("key=" + key);
                System.out.println("value=" + value);
//                tableList.add(key);
//                fieldList.add(value);
                tableMap.put(key, value);
            }

        }




    }

}
