import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 描述
 * @author: ws
 * @time: 2020/5/18 14:38
 */
public class Test01 {
    public static void main(String[] args) {
//        String strSql;
//        StringBuilder sb = new StringBuilder();
        /*try {
            List<String> strings = FileUtils.readLines(new File("src/main/resources/config/table_conf"), "UTF-8");
            for (String string : strings) {
                System.out.println(string);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /*String str ="ws,xunxun,aaa,bbb";
        String[] split = StringUtils.split(str, ",");
        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]);
        }*/


        /*StringBuilder sb = new StringBuilder("ws");
        System.out.println(sb.toString());

        Map<String, StringBuilder> map = new HashMap<>();
        map.put("1",sb);
        map.put("1",sb.append("zzz"));
        for (StringBuilder stringBuilder : map.values()) {
            System.out.println(stringBuilder.toString());
        }*/

//        String str = "{\"data\":{\"age\":\"5\",\"id\":\"45\",\"province\":\"吉林省\",\"qq\":\"431105810\",\"sex\":\"女\",\"wx\":\"17794975986\"},\"tablename\":\"ws_test\"}\n" +
//                "{\"data\":{\"age\":\"75\",\"id\":\"34\",\"province\":\"广东省\",\"qq\":\"1854435\",\"sex\":\"男\",\"wx\":\"fz9z3V3h47\"},\"tablename\":\"ws_test\"}";


        String str = "{\"data\":{\"age\":\"2\",\"id\":\"1\",\"province\":\"河南省\",\"qq\":\"6291015445\",\"sex\":\"男\",\"wx\":\"17136564821\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"81\",\"id\":\"2\",\"province\":\"广东省\",\"qq\":\"974358101\",\"sex\":\"女\",\"wx\":\"0w000v19z3\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"88\",\"id\":\"3\",\"province\":\"安徽省\",\"qq\":\"78251953\",\"sex\":\"男\",\"wx\":\"6P8l242iw1\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"63\",\"id\":\"4\",\"province\":\"吉林省\",\"qq\":\"10105858\",\"sex\":\"女\",\"wx\":\"17765507860\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"48\",\"id\":\"5\",\"province\":\"河北省\",\"qq\":\"310910119\",\"sex\":\"男\",\"wx\":\"12z4BR8005\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"85\",\"id\":\"6\",\"province\":\"浙江省\",\"qq\":\"102971033910\",\"sex\":\"女\",\"wx\":\"17750792978\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"69\",\"id\":\"7\",\"province\":\"河南省\",\"qq\":\"93934751578\",\"sex\":\"男\",\"wx\":\"13803372165\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"94\",\"id\":\"8\",\"province\":\"浙江省\",\"qq\":\"105101519\",\"sex\":\"女\",\"wx\":\"91Z700624f\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"48\",\"id\":\"9\",\"province\":\"福建省\",\"qq\":\"110491525291\",\"sex\":\"男\",\"wx\":\"15109342601\"},\"tablename\":\"ws_test\"}\n" +
                "{\"data\":{\"age\":\"23\",\"id\":\"10\",\"province\":\"安徽省\",\"qq\":\"722947\",\"sex\":\"女\",\"wx\":\"13820645323\"},\"tablename\":\"ws_test\"}";
        ByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = null;

        bis = new ByteArrayInputStream(str.getBytes()); //写入

        bos = new ByteArrayOutputStream();    //内存流 不需要设置文件路径

        int len = 0;
        while((len=bis.read()) != -1) {
            char c = (char) len;
            bos.write(c);
        }

        String result = bos.toString();
        System.out.println("result=" + result);

        try {
            bis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
