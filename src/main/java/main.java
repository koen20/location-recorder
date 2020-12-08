import com.google.gson.Gson;
import spark.Spark;

import java.io.FileReader;

import static spark.Spark.*;

public class main {
    public static ConfigItem confItem;

    public static void main(String args[]) {
        confItem = getConfig();
        Data data = new Data();
        PhoneTrack phoneTrack = new PhoneTrack();
        StopHandler stopHandler = new StopHandler();
        new Mysql(confItem.getMysqlServer(), confItem.getMysqlUsername(), confItem.getMysqlPassword());
        new Mqtt(confItem);
        Timeline timeline = new Timeline(confItem);
        Spark.port(9936);

        get("/info", data::getData);
        get("/add", phoneTrack::addData);
        get("/timeline", timeline::getDataDate);
        path("/stop", () -> {
            post("/add", stopHandler::addStop);
            get("/get", stopHandler::getStops);
        });
    }

    public static ConfigItem getConfig(){
        ConfigItem configItem = null;
        try {
            Gson gson = new Gson();
            configItem = gson.fromJson(new FileReader("config.json"), ConfigItem.class);
        } catch (Exception e) {
            System.out.println("Unable to read config file");
            e.printStackTrace();
        }
        return configItem;
    }
}
