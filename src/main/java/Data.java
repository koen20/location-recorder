import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Data {
    public Data () {

    }

    public String getData(Request request, Response response){
        String res = "";
        long startTime = Long.parseLong(request.queryParams("startTime"));
        long endTime = Long.parseLong(request.queryParams("endTime"));
        res = getDataTime(startTime, endTime).toString();
        return res;
    }

    public static JSONArray getDataTime(long startTime, long endTime) {
        JSONArray jsonArray = new JSONArray();
        try {
            Statement stmt = Mysql.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM data WHERE date BETWEEN '" + Mqtt.getMysqlDateString(startTime) + "' AND '"
                    + Mqtt.getMysqlDateString(endTime) + "'");
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("date", rs.getTimestamp("date"));
                jsonObject.put("lat", rs.getDouble("lat"));
                jsonObject.put("lon", rs.getDouble("lon"));
                jsonArray.put(jsonObject);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }
}
