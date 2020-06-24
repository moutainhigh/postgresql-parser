package com.fiberhome.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * author: ws
 * time: 2020/4/29 23:13
 */
public class Test {
    public static void main(String[] args) {
        String sql = "insert into ws_test (id,sex,age,province,qq,wx) values(?, ?, ?, ?, ?, ?)";
        List<String> list1 = new ArrayList<String>();
        list1.add("1");
        list1.add("男");
        list1.add("16");
        list1.add("江苏省");
        list1.add("1623423");
        list1.add("asdasda");
        List<String> list2 = new ArrayList<String>();
        list2.add("2");
        list2.add("女");
        list2.add("15");
        list2.add("江苏省");
        list2.add("1623423");
        list2.add("asdasda");
        List<List<String>> data = new ArrayList<List<String>>();
        data.add(list1);
        data.add(list2);
//        JdbcTemplate.getInstance().insert(sql, data);

    }
}
