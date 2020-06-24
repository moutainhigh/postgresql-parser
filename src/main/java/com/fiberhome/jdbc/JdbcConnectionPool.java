package com.fiberhome.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.fiberhome.Conf.SysConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * description:
 * author: ws
 * time: 2020/4/29 22:38
 */
public class JdbcConnectionPool {
    public static Logger logger = LoggerFactory.getLogger(JdbcConnectionPool.class);

    private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();
    private static DruidDataSource druidDataSource = null;

    static {
        Properties properties = loadPropertiesFile(SysConstants.JDBC_CONF_FILE);

        try {
            druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
            //true:关闭druid连接失败无限重连
            druidDataSource.setBreakAfterAcquireFailure(true);
            //设置获取连接出错时的自动重连次数
            druidDataSource.setConnectionErrorRetryAttempts(0);
        } catch (Exception e) {
            logger.error("[JDBC Exception] -->"
                            + "Failed to configured the Druid DataSource, the exception message is:" + e.getMessage());
        }

    }

    public static Connection getConnection() {
        Connection connection = connectionThreadLocal.get();

        try {
            if (null == connection){
                connection = druidDataSource.getConnection();
                connectionThreadLocal.set(connection);
            }

        } catch (SQLException e) {
//            e.printStackTrace();
            logger.error("[JDBC Exception] -->"
                            + "Failed to create a connection, the exception message is:" + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection() {
        Connection connection = connectionThreadLocal.get();
        if (null != connection) {
            try {
                connection.close();
                connectionThreadLocal.remove();
            } catch (SQLException e) {
//                e.printStackTrace();
                logger.error("[JDBC Exception] -->"
                                + "Failed to close the DruidPooledConnection, the exception message is:" + e.getMessage());
            }
        }

    }

    public static void startTransaction() {
        Connection conn = connectionThreadLocal.get();

        try {
            if (conn == null) {
                conn=getConnection();
                connectionThreadLocal.set(conn);
            }
            conn.setAutoCommit(false);
        } catch (SQLException e) {
//            e.printStackTrace();
            logger.error("[JDBC Exception] -->"
                            + "Failed to start the transaction, the exception message is:" + e.getMessage());
        }

    }

    public static void commit() {
        Connection conn = connectionThreadLocal.get();
        try {
            conn.commit();
        } catch (SQLException e) {
            logger.error("[JDBC Exception] -->"
                            + "Failed to commit the transaction, the exception message is:" + e.getMessage());
        }

    }

    public static void rollback() {
        Connection conn = connectionThreadLocal.get();

        try {
            conn.rollback();
            connectionThreadLocal.remove();
        } catch (SQLException e) {
//            e.printStackTrace();
            logger.error("[JDBC Exception] -->"
                            + "Failed to rollback the transaction, the exception message is:" + e.getMessage());
        }

    }

    private static Properties loadPropertiesFile(String fullFile) {
        if (null == fullFile || fullFile.equals("")) {
            throw new IllegalArgumentException(
                    "Properties file path can not be null" + fullFile);
        }
        Properties prop = new Properties();
        try {
            prop.load(JdbcConnectionPool.class.getClassLoader().getResourceAsStream(fullFile));
        } catch (IOException e) {
            logger.error("[Properties Exception] --> "
                    + "Can not load jdbc properties, the exception message is:" + e.getMessage());
        }
        return prop;
    }

}
