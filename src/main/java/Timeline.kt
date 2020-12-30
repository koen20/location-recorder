import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class Timeline(val configItem: ConfigItem, val mysql: Mysql) {
    fun getDataDate(dt: Date): String {
        return getData(mysql.getData(dt.time / 1000, (dt.time + 86400000) / 1000)).toString()
    }

    fun getData(locationItems: ArrayList<LocationItem>): JSONObject {
        val jsonArray = JSONArray()
        val jsonArrayAll = JSONArray()
        val jsonObjectRes = JSONObject()

        var lat = 0.0
        var lon = 0.0
        var time: Long = 0
        var multiple = true
        var added = true
        var count = 0
        var latTot = 0.0
        var lonTot = 0.0
        var firstTime: Timestamp? = null
        var endTime: Timestamp? = null
        locationItems.forEach { item ->
            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) >= configItem.radiusLocation) {
                time = item.date.time
                multiple = false
                if (!added) {
                    jsonArray.put(add(latTot, lonTot, count, firstTime!!, endTime!!))
                    added = true
                }
                count = 0
                latTot = 0.0
                lonTot = 0.0
                firstTime = item.date
                lat = item.lat
                lon = item.lon
            }
            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) < configItem.radiusLocation) {
                if (item.date.time - time > 420000 && !multiple) {
                    multiple = true
                    added = false
                }
                count += 1
                latTot += item.lat
                lonTot += item.lon
                endTime = item.date
            }
            jsonArrayAll.put(JSONObject().apply {
                put("date", item.date.time)
                put("lat", item.lat)
                put("lon", item.lon)
            })
        }
        if (!added) {
            jsonArray.put(add(latTot, lonTot, count, firstTime!!, endTime!!))
        }

        val jsonArrayRoutes = Routes().getRouteFromStop(jsonArray, jsonArrayAll)
        jsonObjectRes.put("routes", jsonArrayRoutes)
        jsonObjectRes.put("stops", jsonArray)

        //remove parts with possible inaccurate gps data
        try {
            var index = 0
            var lastIndex = jsonArrayRoutes.length() - 1

            //loop through all routes
            while (index <= lastIndex && lastIndex >= 0) {
                val item = jsonArrayRoutes.getJSONObject(index)
                if (item.getInt("pointCount") <= 4 && item.getDouble("distance") < 400 &&
                    item.getString("startLocation") == item.getString("stopLocation")
                ) {
                    for (k in 0 until jsonArray.length() - 1) {
                        val itemStop = jsonArray.getJSONObject(k)
                        if (itemStop.getLong("end") == item.getLong("start")) {//get the stop before the route that is being removed
                            jsonArrayRoutes.remove(index)
                            jsonArray.getJSONObject(k).put("end", jsonArray.getJSONObject(k + 1).getLong("end"))
                            jsonArray.remove(k + 1)
                            lastIndex--
                        }
                    }
                }
                index++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return jsonObjectRes
    }

    private fun add(latTot: Double, lonTot: Double, count: Int, firstTime: Timestamp, endTime: Timestamp): JSONObject {
        val stop = Address().getAddressName(
            round(latTot / count, 5),
            round(lonTot / count, 5),
            configItem,
            mysql
        )

        return JSONObject().apply {
            put("start", firstTime.time)
            put("end", endTime.time)
            put("location", stop.name)
            put("locationUserAdded", stop.isUserAdded)
            put("lat", round(latTot / count, 5))
            put("lon", round(lonTot / count, 5))
        }
    }

    companion object {
        fun distance(lat1: Double, lat2: Double, lon1: Double, lon2: Double, el1: Double, el2: Double): Double {
            val R = 6371 // Radius of the earth
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            var distance = R * c * 1000 // convert to meters
            val height = el1 - el2
            distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)
            return Math.sqrt(distance)
        }

        fun round(value: Double, places: Int): Double {
            require(places >= 0)
            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }
    }
}