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
                call.respondText(Gson().toJson(mysql.locationDataDao.getData(startTime.toLong() * 1000, endTime.toLong() * 1000)))
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
                    val locationDataItems = mysql.locationDataDao.getData(dt.time, dt.time + 86400000)
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
                    val locations = mysql.locationDao.getLocationsView(dt.time, dt.time + 86400000)
                    val lastLocation = mysql.locationDao.getLocationsView(0, 0, true)

                    //locations doesn't include the latest location. Fetch data since the latest location and add it.
                    if (lastLocation.size > 0) {
                        val locationDataItems =
                            mysql.locationDataDao.getData(
                                lastLocation[0].start,
                                (lastLocation[0].start + 86400000)
                            )

                        val locationsGenerated = Timeline(configItem, mysql).getData(locationDataItems)
                        locationsGenerated.removeAt(0)
                        locationsGenerated.forEach {
                            if (it.start < (dt.time + 86400000)) {
                                locations.add(it)
                            }
                        }
                    }
                    val gson = Gson()
                    val jsonObject = JsonObject()
                    jsonObject.add("stops", gson.toJsonTree(locations))
                    jsonObject.add(
                        "routes",
                        gson.toJsonTree(mysql.routeDao.getRoutes(dt.time, dt.time + 86400000))
                    )
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
