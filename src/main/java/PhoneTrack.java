import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.sql.Statement;
public class PhoneTrack {
    public PhoneTrack() {

    }

    public String addData(Request request, Response response) {
        try {
            String tst = Mqtt.getMysqlDateString(Long.parseLong(request.queryParams("timestamp")));
            Statement stmt = Mysql.conn.createStatement();
            String alt = request.queryParams("alt");
            if (alt.equals("")){
                stmt.executeUpdate("INSERT INTO data VALUES (NULL, '" + tst + "', '"
                        + request.queryParams("lat") + "', '" + request.queryParams("lon") + "'" +
                        ", NULL, '" + request.queryParams("acc")
                        + "',  '" + request.queryParams("batt") + "', '" + request.queryParams("tid") + "')");
            } else {
                stmt.executeUpdate("INSERT INTO data VALUES (NULL, '" + tst + "', '"
                        + request.queryParams("lat") + "', '" + request.queryParams("lon") + "'" +
                        ", '" + alt + "', '" + request.queryParams("acc")
                        + "',  '" + request.queryParams("batt") + "', '" + request.queryParams("tid") + "')");
            }

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
