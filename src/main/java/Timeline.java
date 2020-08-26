import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;

public class Timeline {
    public Timeline() {
        //getData(1597968000, 1598054400);
    }

    public String getDataDate(Request request, Response response) {
        String res = "";
        java.util.Date dt;

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd");

        try {
            dt = sdf.parse(request.queryParams("date"));
            System.out.println("ja1");
            res = getData(dt.getTime() / 1000, (dt.getTime() + 86400000) / 1000).toString();
            System.out.println("ja");
        } catch (ParseException e) {
            System.out.println("asodf");
            e.printStackTrace();
        }
        System.out.println(res);
        return res;
    }

    private JSONArray getData(long start, long end) {
        JSONArray jsonArray = new JSONArray();

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
                if (distance(lat, rs.getDouble("lat"), lon, rs.getDouble("lon"), 0, 0) > 100) {
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
                }
                if (distance(lat, rs.getDouble("lat"), lon, rs.getDouble("lon"), 0, 0) < 100) {
                    //System.out.println("dif: " + (rs.getTimestamp("date").getTime() - time));
                    if (rs.getTimestamp("date").getTime() - time > 420000 && !multiple) {

                        multiple = true;
                        added = false;
                        System.out.println(rs.getTimestamp("date"));
                    }
                    count = count + 1;
                    latTot = latTot + rs.getDouble("lat");
                    lonTot = lonTot + rs.getDouble("lon");
                    endTime = rs.getTimestamp("date");
                }
                lat = rs.getDouble("lat");
                lon = rs.getDouble("lon");
            }
            if (!added) {
                jsonArray.put(add(latTot, lonTot, count, firstTime, endTime));
                added = true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("datar");

        return jsonArray;
    }

    public JSONObject add(double latTot, double lonTot, int count, Timestamp firstTime, Timestamp endTime){
        JSONObject jsonObjectLoc = new JSONObject();
        JSONObject address = new JSONObject();
        try {
            address = getAddress(round(lonTot / count, 5), round(latTot / count, 5));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = "";
        if(address.has("street")){
            name = address.getString("street");
            if (address.has("housenumber")){
                name = name + " " + address.getString("housenumber");
            }
        } else if (address.has("name")){
            name = address.getString("name");
        }
        jsonObjectLoc.put("start", firstTime.toString());
        jsonObjectLoc.put("end", endTime.toString());
        jsonObjectLoc.put("location", name);
        jsonObjectLoc.put("lat", round(latTot / count, 5));
        jsonObjectLoc.put("lon", round(lonTot / count, 5));
        return jsonObjectLoc;
    }

    public JSONObject getAddress(double lon, double lat) throws IOException {
        //URL obj = new URL("http://photon.komoot.de/reverse?lon=" + lon + "&lat=" + lat);
        URL obj = new URL("https://vps3.koenhabets.nl/reverse?lon=" + lon + "&lat=" + lat);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "test");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject jsonObject = new JSONObject(response.toString()).getJSONArray("features").getJSONObject(0).getJSONObject("properties");
        System.out.println(new JSONObject(response.toString()).getJSONArray("features").getJSONObject(0).toString());
        return jsonObject;
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
