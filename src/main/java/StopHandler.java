import com.google.gson.Gson;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StopHandler {
    Mysql mysql;
    public StopHandler(Mysql mysql){
        this.mysql = mysql;
    }

    public String addStop(Request request, Response response){
        String res = "";
        Stop stop = new Stop(request.queryParams("name"), Double.parseDouble(request.queryParams("lat")),
                Double.parseDouble(request.queryParams("lon")),
                Integer.parseInt(request.queryParamOrDefault("radius", "100")), true);
        if (!mysql.AddStop(stop)){
            response.status(500);
        }
        mysql.updateStops();
        return res;
    }

    public String getStops(Request request, Response response){
        Gson gson = new Gson();
        return gson.toJson(mysql.getStops());
    }
}
