import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import model.ConfigItem
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
                    val dt = df.parse(date)
                    val locationDataItems = mysql.locationDataDao.getData(dt.time / 1000, (dt.time + 86400000) / 1000)
                    val locationItems = timeline.getData(locationDataItems)
                    val arrayRoutes = Routes().getRouteFromStop(locationItems, locationDataItems)

                    val jsonObjectRes = JsonObject()
                    val gson = Gson()
                    jsonObjectRes.add("routes", gson.toJsonTree(arrayRoutes))
                    jsonObjectRes.add("stops", gson.toJsonTree(locationItems))
                    call.respondText(jsonObjectRes.toString())
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

                    //locations doesn't include the latest location. Fetch data since the latest location and add it.
                    val locationDataItems = mysql.locationDataDao.getData(locations[locations.size - 1].start / 1000)
                    val locationsGenerated = Timeline(configItem, mysql).getData(locationDataItems)
                    locationsGenerated.removeAt(0)
                    locations.addAll(locationsGenerated)
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
