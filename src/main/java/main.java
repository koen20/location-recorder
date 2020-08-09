import com.google.gson.Gson;
import spark.Spark;

import java.io.FileReader;

import static spark.Spark.get;

public class main {
    public static ConfigItem confItem;

    public static void main(String args[]) {
        confItem = getConfig();
        Data data = new Data();
        PhoneTrack phoneTrack = new PhoneTrack();
        new Mysql();
        new Mqtt();

        Spark.port(9936);

        get("/info", data::getData);
        get("/add", phoneTrack::addData);

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
