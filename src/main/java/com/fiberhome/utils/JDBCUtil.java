package com.fiberhome.utils;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @description: 描述
 * @author: ws
 * @time: 2020/4/29 16:59
 */
public class JDBCUtil {
    private static Properties properties;
    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static{
        try {
            properties = new Properties();
            InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("config/db.properties");
            properties.load(is);
            url = properties.getProperty("url");
            user = properties.getProperty("username");
            password = properties.getProperty("password");
            driver = properties.getProperty("driver");
            Class.forName(driver);
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public static Connection getConn() throws Exception{

        Connection connection = DriverManager.getConnection(url, user, password);

        return connection;
    }

    public static void close(ResultSet resultSet, PreparedStatement preparedStatement,
                             Connection connection){

        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if(preparedStatement != null ){
                preparedStatement.close();
            }
            if(connection != null ){
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public static PreparedStatement insertExecute(String sql){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PreparedStatement ps = null;
        //创建sql命令对象
        try {
            if (!sql.isEmpty()) {

                ps = conn.prepareStatement(sql);
            } else {
                System.out.println("sql为空");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ps;

    }


}
