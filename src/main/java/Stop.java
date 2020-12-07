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

    public int getRadius() {
        return radius;
    }
}
