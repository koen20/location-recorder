import com.google.gson.Gson
import spark.*
import java.io.FileReader


fun main() {
    val configItem = getConfig()
    val mysql = Mysql(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
    val data = Data()
    val phoneTrack = PhoneTrack()
    val stopHandler = StopHandler()
    val timeline = Timeline(configItem)
    Spark.port(9936)

    Spark.get("/info") { request: Request?, response: Response? -> data.getData(request!!, response!!) }
    Spark.get("/add") { request: Request?, response: Response? -> phoneTrack.addData(request, response) }
    Spark.get("/timeline") { request: Request?, response: Response? -> timeline.getDataDate(request, response) }
    Spark.path("/stop") {
        Spark.post("/add") { request: Request?, response: Response? -> stopHandler.addStop(request, response) }
        Spark.get("/get") { request: Request?, response: Response? -> stopHandler.getStops(request, response) }
    }
}

fun getConfig(): ConfigItem {
    val gson = Gson()
    return gson.fromJson(FileReader("config.json"), ConfigItem::class.java)
}
