import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import model.Stop
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

fun Route.data(mysql: Mysql, configItem: ConfigItem) {
    route("/info") {
        get {
            val startTime = call.parameters["startTime"] ?: return@get call.respondText(
                "Missing startTime",
                status = HttpStatusCode.BadRequest
            )
            val endTime = call.parameters["endTime"] ?: return@get call.respondText(
                "Missing endTime",
                status = HttpStatusCode.BadRequest
            )
            call.respondText(Gson().toJson(mysql.getData(startTime.toLong(), endTime.toLong())))
        }
    }

    route("/timeline") {
        get {
            val df = SimpleDateFormat("yyyy-MM-dd")
            val date = call.parameters["date"] ?: return@get call.respondText(
                "Missing date",
                status = HttpStatusCode.BadRequest
            )
            try {
                val timeline = Timeline(configItem, mysql)
                call.respondText(timeline.getDataDate(df.parse(date)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    route("/timelineDb") {
        get {
            val df = SimpleDateFormat("yyyy-MM-dd")
            val date = call.parameters["date"] ?: return@get call.respondText(
                "Missing date",
                status = HttpStatusCode.BadRequest
            )
            try {
                val timeline = Timeline(configItem, mysql)
                val dt = df.parse(date)
                val jsonArray = JSONArray();
                mysql.getLocations(dt.time / 1000, (dt.time + 86400000) / 1000).forEach {
                    jsonArray.put(timeline.add(it.lat, it.lon, 1, it.startDate, it.endDate))
                }
                val jsonObject = JSONObject()
                jsonObject.put("stops", jsonArray)
                jsonObject.put("routes", JSONArray())
                call.respondText(jsonObject.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    route("/stop") {
        get {
            if (call.parameters["name"] != null) {
                call.respondText(Gson().toJson(mysql.getLocations(call.parameters["name"]!!)))
            }
            call.respondText(Gson().toJson(mysql.stops))
        }
        post {
            var radius = 100
            if (call.parameters["date"] != null) {
                radius = call.parameters["date"]!!.toInt()
            }
            val stop = Stop(
                0, call.parameters["name"]!!, call.parameters["lat"]!!.toDouble(),
                call.parameters["lon"]!!.toDouble(), radius, true
            )

            if (!mysql.addStop(stop)) {
                call.respondText("Insert failed", status = HttpStatusCode.InternalServerError)
            } else {
                call.respondText("Added")
            }
        }
    }

    addPhoneTrackLocation(mysql)
}
