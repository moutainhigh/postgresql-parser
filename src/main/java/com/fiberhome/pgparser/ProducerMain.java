package com.fiberhome.pgparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 描述
 * @author: ws
 * @time: 2020/4/30 9:16
 */
public class ProducerMain {
    private static final Logger logger = LoggerFactory.getLogger(ProducerMain.class);

    public static void main(String[] args) {
        String topic = "topic_ws10w_all";
        ProducerAPI p = new ProducerAPI();
        p.sendMessage(topic);

    }

}
