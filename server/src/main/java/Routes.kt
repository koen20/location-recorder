import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class Routes {
    fun getRouteFromStop(stops: JSONArray, all: JSONArray): JSONArray {
        val jsonArray = JSONArray()
        for (i in 0 until stops.length() - 1) { //loop through every stop
            try {
                val item = stops.getJSONObject(i)
                val itemS = stops.getJSONObject(i + 1)
                var distanceTot = 0.0
                var pointCount = 0
                var lastLat = item.getDouble("lat")
                var lastLon = item.getDouble("lon")
                for (k in 0 until all.length()) { //loop through all locations
                    val item2 = all.getJSONObject(k)
                    //get all locations between two stops
                    if (item2.getLong("date") >= item.getLong("end") && item2.getLong("date") <= itemS.getLong("start")) {
                        distanceTot += Timeline.distance(
                            item2.getDouble("lat"), lastLat, item2.getDouble("lon"), lastLon, 0.0, 0.0
                        )
                        lastLat = item2.getDouble("lat")
                        lastLon = item2.getDouble("lon")
                        pointCount += 1
                    }
                }
                val time = (itemS.getLong("start") - item.getLong("end")).toDouble()
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

                jsonArray.put(JSONObject().apply {
                    put("start", item.getLong("end"))
                    put("end", itemS.getLong("start"))
                    put("route", item.getString("location") + " - " + itemS.getString("location"))
                    put("startLocation", item.getString("location"))
                    put("stopLocation", itemS.getString("location"))
                    put("distance", distanceTot)
                    put("time", time)
                    put("speed", speed)
                    put("movementType", movementType)
                    put("pointCount", pointCount)
                })
            } catch (exception: Exception){
                exception.printStackTrace()
            }
        }

        return jsonArray
    }
}