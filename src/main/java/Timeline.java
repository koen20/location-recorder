import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;

public class Timeline {
    ConfigItem configItem;
    public Timeline(ConfigItem configItem) {
        this.configItem = configItem;
        //getData(1597968000, 1598054400);
    }

    public String getDataDate(Request request, Response response) {
        String res = "";
        java.util.Date dt;

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd");

        try {
            dt = sdf.parse(request.queryParams("date"));
            res = getData(dt.getTime() / 1000, (dt.getTime() + 86400000) / 1000).toString();
        } catch (ParseException e) {
            System.out.println("asodf");
            e.printStackTrace();
        }
        System.out.println(res);
        return res;
    }

    private JSONObject getData(long start, long end) {
        JSONArray jsonArray = new JSONArray();
        JSONArray jsonArrayAll = new JSONArray();
        JSONArray jsonArrayRoutes = new JSONArray();
        JSONObject jsonObjectRes = new JSONObject();

        try {
            Statement stmt = Mysql.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM data WHERE date BETWEEN '" + Mqtt.getMysqlDateString(start) + "' AND '"
                    + Mqtt.getMysqlDateString(end) + "'");
            double lat = 0;
            double lon = 0;
            long time = 0;
            boolean multiple = true;
            boolean added = true;
            int count = 0;
            double latTot = 0;
            double lonTot = 0;
            Timestamp firstTime = null;
            Timestamp endTime = null;
            while (rs.next()) {
                if (distance(lat, rs.getDouble("lat"), lon, rs.getDouble("lon"), 0, 0) >= configItem.getRadiusLocation()) {
                    time = rs.getTimestamp("date").getTime();
                    multiple = false;
                    if (!added) {
                        jsonArray.put(add(latTot, lonTot, count, firstTime, endTime));
                        added = true;
                    }
                    count = 0;
                    latTot = 0;
                    lonTot = 0;
                    firstTime = rs.getTimestamp("date");
                    lat = rs.getDouble("lat");
                    lon = rs.getDouble("lon");
                }
                if (distance(lat, rs.getDouble("lat"), lon, rs.getDouble("lon"), 0, 0) < configItem.getRadiusLocation()) {
                    if (rs.getTimestamp("date").getTime() - time > 420000 && !multiple) {

                        multiple = true;
                        added = false;
                    }
                    count += 1;
                    latTot = latTot + rs.getDouble("lat");
                    lonTot = lonTot + rs.getDouble("lon");
                    endTime = rs.getTimestamp("date");
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("date", rs.getTimestamp("date").getTime());
                jsonObject.put("lat", rs.getDouble("lat"));
                jsonObject.put("lon", rs.getDouble("lon"));
                jsonArrayAll.put(jsonObject);
            }
            if (!added) {
                jsonArray.put(add(latTot, lonTot, count, firstTime, endTime));
            }
            rs.close();
            stmt.close();
            jsonArrayRoutes = Routes.getRouteFromStop(jsonArray, jsonArrayAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        jsonObjectRes.put("routes", jsonArrayRoutes);
        jsonObjectRes.put("stops", jsonArray);

        return jsonObjectRes;
    }

    public JSONObject add(double latTot, double lonTot, int count, Timestamp firstTime, Timestamp endTime) {
        JSONObject jsonObjectLoc = new JSONObject();
        Stop stop = StopHandler.getAddressName(round(latTot / count, 5), round(lonTot / count, 5), configItem);
        jsonObjectLoc.put("start", firstTime.getTime());
        jsonObjectLoc.put("end", endTime.getTime());
        jsonObjectLoc.put("location", stop.getName());
        jsonObjectLoc.put("locationUserAdded", stop.isUserAdded());
        jsonObjectLoc.put("lat", round(latTot / count, 5));
        jsonObjectLoc.put("lon", round(lonTot / count, 5));
        return jsonObjectLoc;
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
