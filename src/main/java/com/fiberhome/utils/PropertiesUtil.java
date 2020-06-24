package com.fiberhome.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @description: 配置文件工具类
 * @author: ws
 * @time: 2020/5/7 17:10
 */
public class PropertiesUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    public static Properties getProperties(String name) {
        InputStream resourceAsStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(name);
        Properties properties = new Properties();

        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (null!=resourceAsStream){
                    resourceAsStream.close();
                }
            } catch (IOException e) {
                logger.error("resourceAsStream error!", e.getMessage());
            }
        }
        return properties;
    }

}
