package com.fiberhome.jdbc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 增删改查
 * author: ws
 * time: 2020/4/29 22:58
 */
public class JdbcTemplate2 {
    public static Logger logger = LoggerFactory.getLogger(JdbcTemplate2.class);

    private static JdbcTemplate2 jdbcTemplate = null;

    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://172.16.108.7:5432/testdb_old111?stringtype=unspecified";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private JdbcTemplate2() {
    }

    public static JdbcTemplate2 getInstance() {
        if(jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate2();
        }
        return jdbcTemplate;
    }

    public boolean insert(String sql) {
        boolean f = false;
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = JdbcConnectionPool.getConnection();
            ps = conn.prepareStatement(sql);
            f = ps.execute();
        } catch (SQLException e) {
            logger.error("[JDBC Exception] -->"
                            + "Can not insert, the exceprion message is:" + e.getMessage());
        }  finally {
            try {
                if (null != ps) {
                    ps.close();
                }
            } catch (SQLException e) {
                logger.error("[JDBC Exception] -->"
                                + "Failed to close connection, the exceprion message is:" + e.getMessage());
            }

        }
        return f;

    }


    /**
     * 优化版5 : 判断是否需要加jcontent
     * 不加：老方法，解析字段
     * 加：字段从配置中读，占位符赋值中无值用null代替
     * @param data 获取的json数据
     * @param tableMap  获取的有值表配置信息
     * @param autoCommit
     */
    public void insertPg(List<String> data, Map<String, String> tableMap, String autoCommit) {
        //默认f为false，即手动提交事务
        boolean f = false;
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = JdbcConnectionPool.getConnection();
            //开始事务
            JdbcConnectionPool.startTransaction();

            //sql执行成功次数
            int successNum = 0;
            //sql执行失败次数
            int failNum = 0;
            long startTime = System.currentTimeMillis();
            //拼接sql，实例化StringBuilder引用对象
            StringBuilder sql = new StringBuilder(5000);
            String strSql;

            Map<String, String> valueMap = new HashMap<>(200);

            String[] fieldArray = new String[1000];

            //读取单行数据，拼接并执行sql
            int allCount = data.size();
            for (int dataNo = 0; dataNo < allCount; dataNo++) {
                //拼接sql
                sql.append("insert into ");
                String line = (String) JSON.toJSON(data.get(dataNo));
                //将json形式字符串转化为json
                JSONObject lineObject = JSON.parseObject(line);

                if (lineObject.get("tablename") != null){
                    //忽略kafka中tablename大小写
                    String tableName = ((String) lineObject.get("tablename")).toLowerCase();
                    //如果tablename是属于配置信息中的表，则字段名从配置信息中读取
                    sql.append(tableName + " (");
                    if (tableMap.containsKey(tableName)) {
                        //获取多个字段名
                        String sqlField = tableMap.get(tableName);
                        sql.append(sqlField + ",jcontent) values (");
                        //字段间用逗号做分隔符，split.length:统计配置中有多少个字段及相应占位符数量
//                        fieldArray = sqlField.split(",");
                        fieldArray = org.apache.commons.lang3.StringUtils.split(sqlField,",");
                        for (int j = 0; j < fieldArray.length; j++) {
                            sql.append("?,");
                        }
                        sql.append("?)"); //字符串拼接完成
                        //将完整的sql赋给String类型变量
                        strSql = String.valueOf(sql);
                        //每次sql执行结束，及时清空sql对象
                        sql.setLength(0);

                        //方法一：单条执行sql()
                        ps = conn.prepareStatement(strSql);    //不会出现null报错,但还是调用toString()方法,会new String()

                        //将kafka中每个数据字段key value存入map
                        for (Map.Entry<String, Object> entry : lineObject.entrySet()) {
                            //获取tablename,data及相应值
                            String key = entry.getKey();
                            Object dataValue = entry.getValue();
//                            String value = (String) entry.getValue();   //这里直接跳到finally
                            if (key.equals("data")) {
                                JSONObject dataObject = JSON.parseObject(dataValue.toString());
                                //获取data中每个字段名和字段值
                                for (Map.Entry<String, Object> dataEntry : dataObject.entrySet()) {
                                    String field = dataEntry.getKey();
                                    String fieldData = (String) dataEntry.getValue();
                                    //将data内每个key value存入map,key统一转为小写
                                    valueMap.put(field.toLowerCase(), fieldData);
                                }
                                //增加dataValue到valueMap
                                valueMap.put("jcontent", dataValue.toString());
                            }
                        }

                        //解析占位符
                        //这里有个问题，占位符 valueMap.get(j+1)的值可能跟我的sql中的字段名不一一对应
                        if (lineObject.get("data") != null) {
                            JSONObject dataResult = (JSONObject) lineObject.get("data");
                            for (int j = 0; j < fieldArray.length; j++) {
                                //fieldArray[j]中对应的字段值不在kafka data中，则valueMap.get()得到的就是null
                                ps.setObject(j+1, valueMap.get(fieldArray[j]));
                            }
                            ps.setObject(fieldArray.length+1, valueMap.get("jcontent"));
                            valueMap.clear();

                        } else {
                            logger.error("data not found!");
                        }

                        try {
                            int single = ps.executeUpdate();
                            successNum += single;
                        } catch (SQLException e) {
                            logger.error("[JDBC Exception] -->"
                                    + "Can not insert, the exception message is:" + e.getMessage() + " -->对应的错误sql为：{}", strSql);
                            failNum += 1;
                            //pg库,同一事务中如果某次数据库操作中出错,那这个事务以后的sql都会出错,报current transaction is aborted,因此需要使用commit解决
                            f = Boolean.parseBoolean(autoCommit.trim());

                            if (true == f) {
                                if( failNum == 1) {
                                    JdbcConnectionPool.commit();
                                    conn.setAutoCommit(f);
                                }
                            } else {
                                JdbcConnectionPool.commit();
                            }
                        }
                        if (null != ps) {
                            ps.close();
                        }

                    } else {        //情况二:无jcontent字段，需解析字段
                        int num = 1;
                        //获取每个字段
                        for (Map.Entry<String, Object> entry : lineObject.entrySet()) {
                            //获取tablename,data及相应值
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if(key.equals("data")) {
                                JSONObject dataObject = JSON.parseObject(value.toString());
                                //获取data中每个字段名和字段值
                                for (Map.Entry<String, Object> dataEntry : dataObject.entrySet()) {
                                    String field = dataEntry.getKey();
                                    String fieldData = (String) dataEntry.getValue();
                                    valueMap.put(String.valueOf(num), fieldData);
                                    num++;
                                    sql.append(field + ",");
                                }
                                sql.deleteCharAt(sql.length()-1);
                                //拼接占位符
                                sql.append(") values(");
                                for (int j = 0; j < valueMap.size(); j++) {
                                    sql.append("?,");
                                }
                                sql.deleteCharAt(sql.length()-1);
                                sql.append(")");
                            }
                        }
                        //将完整的sql赋给String类型变量
                        strSql = String.valueOf(sql);
                        //每次sql执行结束，及时清空sql对象
                        sql.setLength(0);

                        //方法一：单条执行sql
                        ps = conn.prepareStatement(strSql);

                        //解析占位符
                        if (lineObject.get("data") != null) {
                            JSONObject dataResult = (JSONObject) lineObject.get("data");
                            for (int j = 0; j < dataResult.size(); j++) {
                                try {
                                    ps.setObject(j+1, valueMap.get(String.valueOf(j+1)));
                                } catch (SQLException e) {
                                    logger.error("占位符赋值错误：" + e.getMessage());
                                }
                            }
                            valueMap.clear();
                        } else {
                            logger.error("data not found!");
                        }

                        try {
                            int single = ps.executeUpdate();
                            successNum += single;
                        } catch (SQLException e) {
                            logger.error("[JDBC Exception] -->"
                                    + "Can not insert, the exception message is:" + e.getMessage() + " -->对应的错误sql为：{}", strSql);
                            failNum += 1;
                            //pg库,同一事务中如果某次数据库操作中出错,那这个事务以后的sql都会出错,报current transaction is aborted,因此需要使用commit解决
                            f = Boolean.parseBoolean(autoCommit.trim());

                            if (true == f) {
                                if( failNum == 1) {
                                    JdbcConnectionPool.commit();
                                    conn.setAutoCommit(f);
                                }
                            } else {
                                JdbcConnectionPool.commit();
                            }
                        }
                        if (null != ps) {
                            ps.close();
                        }
                    }

                } else {
                    logger.error("tablename not found!");
                }

            }
            //清理对象
            sql = null;
            valueMap = null;

            if (false == f) {
                JdbcConnectionPool.commit();
            }

            long endTime = System.currentTimeMillis();
            if (successNum != 0) {
                logger.info("入库成功{}条，入库失败{}条，总条数：{}条，总耗时：{}ms", successNum, failNum, successNum + failNum, endTime - startTime);
            }
        } catch (SQLException e) {
            logger.error("[JDBC Exception] -->"
                    + "Failed to prepareStatement, the exception message is:" + e.getMessage());
        } finally {
            try {
                if (null != ps){
                    ps.close();
                }
                JdbcConnectionPool.closeConnection();
            } catch (SQLException e) {
                logger.error("[JDBC Exception] -->"
                        + "Failed to close connection, the exception message is:" + e.getMessage());
            }
            if (null != conn) {
                JdbcConnectionPool.closeConnection();
            }
        }

    }



    /**
     * 优化版6 : 判断是否需要加jcontent ---copy方式
     * 不加：老方法，解析字段
     * 加：字段从配置中读，占位符赋值中无值用null代替
     * @param data 获取的json数据
     * @param tableMap  获取的有值表配置信息
     * @param autoCommit
     */
    public void insertPg2(List<String> data, Map<String, String> tableMap, String autoCommit) {
        //默认f为false，即手动提交事务
        boolean f = false;
        PreparedStatement ps = null;
        Connection conn = null;
        CopyManager copyManager = null;

        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            StringBuilder copySql = new StringBuilder(500);

            //sql执行成功次数
            int successNum = 0;
            //sql执行失败次数
            int failNum = 0;
            long startTime = System.currentTimeMillis();

            //读取单行数据，拼接并执行sql
            int allCount = data.size();
            for (int dataNo = 0; dataNo < allCount; dataNo++) {
                copyManager = new CopyManager((BaseConnection) conn);
//                copySql.append("copy ws_test(id,sex,age,province,qq,wx) from stdin delimiter as '\t' Null as 'null'");
                copySql.append("copy ws_test(id,sex,age,province,qq,wx) from stdin with (DELIMITER E'\t', NULL '',ENCODING utf8)");
                StringReader stringReader = new StringReader(data.get(dataNo));
                long resultStatus = copyManager.copyIn(copySql.toString(), stringReader);   //成功是1
                logger.info("resultStatus=" + resultStatus);
                successNum ++;
                copySql.setLength(0);
            }

            long endTime = System.currentTimeMillis();
            if (successNum != 0) {
                logger.info("入库成功{}条，入库失败{}条，总条数：{}条，总耗时：{}ms", successNum, failNum, successNum + failNum, endTime - startTime);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if(null!=conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }



    /**
     * 优化版6 : 判断是否需要加jcontent ---copy方式
     * 不加：老方法，解析字段
     * 加：字段从配置中读，占位符赋值中无值用null代替
     * @param data 获取的json数据
     */
    public void copyToPg(List<String> data) {
        //默认f为false，即手动提交事务
//        boolean f = false;
        PreparedStatement ps = null;
        Connection conn = null;
        CopyManager copyManager = null;

        try {
//            conn = JdbcConnectionPool.getConnection();
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            StringBuilder copySql = new StringBuilder(500);

            //sql执行成功次数
            int successNum = 0;
            //sql执行失败次数
            int failNum = 0;
            long startTime = System.currentTimeMillis();

            //读取单行数据，拼接并执行sql
            int allCount = data.size();
            for (int dataNo = 0; dataNo < allCount; dataNo++) {
                copyManager = new CopyManager((BaseConnection) conn);
//                copySql.append("copy ws_test(id,sex,age,province,qq,wx) from stdin delimiter as '\t' Null as 'null'");
                copySql.append("copy ws_test(id,sex,age,province,qq,wx) from stdin with (DELIMITER E'\t', NULL '',ENCODING utf8)");
                StringReader stringReader = new StringReader(data.get(dataNo));
                long resultStatus = copyManager.copyIn(copySql.toString(), stringReader);   //成功是1
                logger.info("resultStatus=" + resultStatus);
                successNum ++;
                copySql.setLength(0);
            }

            long endTime = System.currentTimeMillis();
            if (successNum != 0) {
                logger.info("入库成功{}条，入库失败{}条，总条数：{}条，总耗时：{}ms", successNum, failNum, successNum + failNum, endTime - startTime);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if(null!=conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    public void copyToPg2(String copySql, String data) {
        //默认f为false，即手动提交事务
//        boolean f = false;
        PreparedStatement ps = null;
        Connection conn = null;
        CopyManager copyManager = null;

        try {
            conn = JdbcConnectionPool.getConnection();

            long startTime = System.currentTimeMillis();
            //读取单行数据，拼接并执行sql
                copyManager = new CopyManager((BaseConnection) conn.getMetaData().getConnection());
                StringReader stringReader = new StringReader(data);
                long successNum = copyManager.copyIn(copySql, stringReader);   //输出：成功条数
                logger.info("successNum=" + successNum);
            long endTime = System.currentTimeMillis();
            if (successNum != 0) {
                logger.info("入库成功{}条，总耗时：{}ms", successNum, endTime - startTime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            /*try {
                if(null!=conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }*/

        }

    }


    public void copyToPg3(String copySql, String data) {
        PreparedStatement ps = null;
        Connection conn = null;
        CopyManager copyManager = null;

//        ByteArrayInputStream bis = null;
//        ByteArrayOutputStream bos = null;

        try {
            conn = JdbcConnectionPool.getConnection();
//            Class.forName(DRIVER);
//            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            long startTime = System.currentTimeMillis();
            //读取单行数据，拼接并执行sql
            copyManager = new CopyManager((BaseConnection) conn.getMetaData().getConnection());

            /*String str = "1\t男\t2\t河南省\t6291015445\t17136564821\t{\"qq\":\"6291015445\",\"wx\":\"17136564821\",\"province\":\"河南省\",\"sex\":\"男\",\"id\":\"1\",\"age\":\"2\"}\n" +
                    "2\t女\t81\t广东省\t974358101\t0w000v19z3\t{\"qq\":\"974358101\",\"wx\":\"0w000v19z3\",\"province\":\"广东省\",\"sex\":\"女\",\"id\":\"2\",\"age\":\"81\"}\n" +
                    "3\t男\t88\t安徽省\t78251953\t6P8l242iw1\t{\"qq\":\"78251953\",\"wx\":\"6P8l242iw1\",\"province\":\"安徽省\",\"sex\":\"男\",\"id\":\"3\",\"age\":\"88\"}\n" +
                    "4\t女\t63\t吉林省\t10105858\t17765507860\t{\"qq\":\"10105858\",\"wx\":\"17765507860\",\"province\":\"吉林省\",\"sex\":\"女\",\"id\":\"4\",\"age\":\"63\"}\n" +
                    "5\t男\t48\t河北省\t310910119\t12z4BR8005\t{\"qq\":\"310910119\",\"wx\":\"12z4BR8005\",\"province\":\"河北省\",\"sex\":\"男\",\"id\":\"5\",\"age\":\"48\"}\n" +
                    "6\t女\t85\t浙江省\t102971033910\t17750792978\t{\"qq\":\"102971033910\",\"wx\":\"17750792978\",\"province\":\"浙江省\",\"sex\":\"女\",\"id\":\"6\",\"age\":\"85\"}";

            CharMatcher cm = CharMatcher.is('\0');
            String newStr = cm.removeFrom(str);
            System.out.println(newStr);

            System.out.println("----------------------------");

            bis = new ByteArrayInputStream(newStr.getBytes()); //写入*/

            /*bos = new ByteArrayOutputStream(); //内存流 不需要设置文件路径

            int len = 0;
            while((len=bis.read()) != -1) {
                char c = (char) len;
                bos.write(c);
            }

            String result = bos.toString();
            System.out.println("result=" + result);*/

//            long successNum = copyManager.copyIn(copySql, bis);   //输出：成功条数
//            CopyIn copyIn = copyManager.copyIn(copySql);
//
//
//            bis.close();

            StringReader stringReader = new StringReader(data);
            long successNum = copyManager.copyIn(copySql, stringReader);   //输出：成功条数
            logger.info("successNum=" + successNum);
            long endTime = System.currentTimeMillis();
            if (successNum != 0) {
                logger.info("入库成功{}条，总耗时：{}ms", successNum, endTime - startTime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if(null!=conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }




}
