import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.text.SimpleDateFormat

fun Route.data(mysql: Mysql, configItem: ConfigItem) {
    route("/api") {
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
                call.respondText(Gson().toJson(mysql.locationDataDao.getData(startTime.toLong(), endTime.toLong())))
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
                    val dt = df.parse(date)
                    val locations = mysql.locationDao.getLocationsView(dt.time / 1000, (dt.time + 86400000) / 1000)
                    val gson = Gson()
                    val jsonObject = JsonObject()
                    jsonObject.add("stops", gson.toJsonTree(locations))
                    jsonObject.add("routes", gson.toJsonTree("[]"))
                    call.respondText(jsonObject.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        route("/stop") {
            get {
                if (call.parameters["name"] != null) {
                    call.respondText(Gson().toJson(mysql.locationDao.getLocations(call.parameters["name"]!!)))
                }
                call.respondText(Gson().toJson(mysql.stopDao.getStops()))
            }
            post {
                if (!mysql.stopDao.updateCustomName(call.parameters["stopId"]!!.toInt(), call.parameters["name"]!!)) {
                    call.respondText("Insert failed", status = HttpStatusCode.InternalServerError)
                } else {
                    call.respondText("Added")
                }
            }
        }

        addPhoneTrackLocation(mysql)
    }
}
