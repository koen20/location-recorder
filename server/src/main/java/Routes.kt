import com.google.gson.JsonArray
import com.google.gson.JsonObject
import model.LocationItem
import model.LocationView

class Routes {
    fun getRouteFromStop(stops: ArrayList<LocationView>, all: ArrayList<LocationItem>): JsonArray {
        val jsonArray = JsonArray()
        stops.forEachIndexed { index, item ->  //loop through every stop
            try {
                val itemS = stops[index]
                var distanceTot = 0.0
                var pointCount = 0
                var lastLat = item.lat
                var lastLon = item.lon
                all.forEach { //loop through all locations
                    //get all locations between two stops
                    if (it.date.time >= item.end.time && it.date.time <= itemS.start.time) {
                        distanceTot += Timeline.distance(
                            it.lat, lastLat, it.lon, lastLon, 0.0, 0.0
                        )
                        lastLat = it.lat
                        lastLon = it.lon
                        pointCount += 1
                    }
                }
                val time = (itemS.start.time - item.end.time).toDouble()
                val timeHours = time / 1000 / 60 / 60
                var speed = distanceTot / 1000.0 / timeHours

                var movementType = "unknown"
                if (speed < 9) {
                    movementType = "walking"
                } else if (speed >= 9) {
                    movementType = "driving"
                }

                if (timeHours == 0.0){
                    speed = 0.0
                }

                jsonArray.add(JsonObject().apply {
                    addProperty("start", item.end.time)
                    addProperty("end", itemS.start.time)
                    addProperty("route", item.location + " - " + itemS.location)
                    addProperty("startLocation", item.location)
                    addProperty("stopLocation", itemS.location)
                    addProperty("distance", distanceTot)
                    addProperty("time", time)
                    addProperty("speed", speed)
                    addProperty("movementType", movementType)
                    addProperty("pointCount", pointCount)
                })
            } catch (exception: Exception){
                exception.printStackTrace()
            }
        }

        return jsonArray
    }
}