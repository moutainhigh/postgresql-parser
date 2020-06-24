import com.fiberhome.jdbc.JdbcConnectionPool;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.copy.CopyManager;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @description: 描述
 * @author: ws
 * @time: 2020/6/2 11:08
 */
public class CopyInAPI {

    public static void main(String[] args) {
        PreparedStatement ps = null;
        Connection conn = null;
        CopyManager copyManager = null;

        String[] data = {"1\t2\t3\t4\t5\t6\n",
            "one\ttwo\tthree\tfour\tfive\tsix\n"};

        String join = StringUtils.join(data);
        System.out.println("join=" + join);
        String join1 = "1\t2\t3\t4\t5\t6\n" +
                "one\ttwo\tthree\tfour\tfive\tsix\n";

        String sql = "copy ws_test(id,sex,age,province,qq,wx) from stdin with (DELIMITER E'\\t',NULL '',ENCODING utf8)";

        StringReader stringReader = new StringReader(join1);

        conn = JdbcConnectionPool.getConnection();

        try {
            long successNum = copyManager.copyIn(sql, stringReader);
            System.out.println("successNum=" + successNum);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
