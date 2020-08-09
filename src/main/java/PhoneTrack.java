import org.json.JSONException;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class PhoneTrack {
    public PhoneTrack() {

    }

    public String addData(Request request, Response response) {
        try {
            String tst = Mqtt.getMysqlDateString(Long.parseLong(request.queryParams("timestamp")));
            Statement stmt = Mysql.conn.createStatement();
            stmt.executeUpdate("INSERT INTO data VALUES (NULL, '" + tst + "', '"
                    + request.queryParams("lat") + "', '" + request.queryParams("lon") + "'" +
                    ", '" + request.queryParams("alt") + "', '" + request.queryParams("acc")
                    + "',  '" + request.queryParams("batt") + "', '" + request.queryParams("tid") + "')");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                System.out.println(Mysql.conn.isValid(3000));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            response.status(500);
        }
        return "";
    }
}
