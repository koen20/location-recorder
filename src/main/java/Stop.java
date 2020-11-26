import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Stop {
    private String name;
    private double lat;
    private double lon;
    private int radius;


    public Stop(String name, double lat, double lon, int radius) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public static Stop getAddressName(double lat, double lon, ConfigItem configItem){
        ArrayList<Stop> stops = Mysql.stops;
        for(int i = 0; i < stops.size(); i++){
            System.out.println(Timeline.distance(lat, stops.get(i).getLat(), lon, stops.get(i).getLon(), 0, 0));
            if (Timeline.distance(lat, stops.get(i).getLat(), lon, stops.get(i).getLon(), 0, 0) < stops.get(i).getRadius()) {
                return stops.get(i);
            }
        }

        JSONObject address = new JSONObject();
        try {
            address = getAddress(lat, lon, configItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = "";
        if (address.has("street")) {
            name = address.getString("street");
            if (address.has("housenumber")) {
                name = name + " " + address.getString("housenumber");
            }
        } else if (address.has("name")) {
            name = address.getString("name");
        }
        Stop stop = new Stop(name, lat, lon, 0);
        return stop;
    }

    public static JSONObject getAddress(double lon, double lat, ConfigItem configItem) throws IOException {
        //URL obj = new URL("http://photon.komoot.de/reverse?lon=" + lon + "&lat=" + lat);
        URL obj = new URL(configItem.getReverseGeocodeAddress().replace("LON", lon + "").replace("LAT", lat + ""));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "owntracks-mysql-recorder");

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

    public int getRadius() {
        return radius;
    }
}
