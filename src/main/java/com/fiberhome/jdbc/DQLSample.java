package com.fiberhome.jdbc;

import java.sql.*;

/**
 * @description: postgresql-jdbc
 * @author: ws
 * @time: 2020/4/28 18:25
 */
public class DQLSample {
    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://172.16.108.7:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs =null ;

        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
//            String sql = "select * from all_result limit 10";
            String sql = "with T as (\n" +
                    "select M.tabname,M.attname,M.attnum,case when attrdef is null or position('nextval' in attrdef)=0 then 'f' else 't' end isseq\n" +
                    "from ( \n" +
                    "SELECT a.attrelid::regclass as tabname,a.attname,pg_catalog.format_type(a.atttypid, a.atttypmod),a.attnum,\n" +
                    "( SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid, true) for 128) \n" +
                    "   FROM pg_catalog.pg_attrdef d\n" +
                    "   WHERE d.adrelid = a.attrelid AND d.adnum = a.attnum AND a.atthasdef ) as attrdef\n" +
                    "  FROM pg_catalog.pg_attribute a \n" +
                    " WHERE a.attrelid in (select oid from pg_class where relname in ('ws_test')) AND a.attnum > 0 AND NOT a.attisdropped \n" +
                    " ) M \n" +
                    " order by M.attnum \n" +
                    ")\n" +
                    "select T.tabname,string_agg(T.attname,',') as cols from T where T.isseq='f' group by T.tabname";
            //创建sql命令对象
            ps = conn.prepareStatement(sql);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            while (rs.next()) {
                for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                    System.out.print(rs.getObject(i));
                    System.out.print("\t");
                }
                System.out.println();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("sql查询耗时为：" + (endTime - startTime) + "ms");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(null!=rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(null!=ps) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
