import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import model.Location
import model.LocationItem
import model.LocationView
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.util.*
import kotlin.math.*


class Timeline(val configItem: ConfigItem, val mysql: Mysql) {
    fun getDataDate(dt: Date): String {
        return getData(mysql.locationDataDao.getData(dt.time / 1000, (dt.time + 86400000) / 1000)).toString()
    }

    fun getData(locationItems: ArrayList<LocationItem>): JsonObject {
        val locationList = ArrayList<LocationView>()
        val jsonObjectRes = JsonObject()

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

        locationItems.forEachIndexed { index, item ->
            locationItems[index].date = Timestamp(item.date.time)
            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) >= configItem.radiusLocation) {
                time = item.date.time
                multiple = false
                if (!added) {
                    locationList.add(add(latTot, lonTot, count, firstTime!!, endTime!!))
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
        }
        if (!added) {
            locationList.add(add(latTot, lonTot, count, firstTime!!, endTime!!))
        }

        val jsonArrayRoutes = Routes().getRouteFromStop(locationList, locationItems)

        //remove parts with possible inaccurate gps data
        try {
            //loop through all routes
            val removeListR = ArrayList<JsonElement>()
            jsonArrayRoutes.forEachIndexed { index, jsonElement ->
                val item = jsonElement.asJsonObject
                if (item.get("pointCount").asInt <= 4 && item.get("distance").asDouble < 600 &&
                    item.get("startLocation").asString == item.get("stopLocation").asString
                ) {
                    val removeList = ArrayList<LocationView>()
                    for (k in 0 until locationList.size - 1) {
                        val itemStop = locationList[k]
                        if (itemStop.end == item.get("start").asLong) {//get the stop before the route that is being removed
                            removeListR.add(jsonElement)
                            locationList[k].end = locationList[k + 1].end
                            removeList.add(locationList[k + 1])
                        }
                    }
                    removeList.forEach {
                        locationList.remove(it)
                    }
                }
            }
            removeListR.forEach {
                jsonArrayRoutes.remove(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val gson = Gson()
        val element = gson.toJsonTree(locationList, object : TypeToken<List<LocationView?>?>() {}.type)
        if (!element.isJsonArray()) {
            throw Exception();
        }

        jsonObjectRes.add("routes", jsonArrayRoutes)
        jsonObjectRes.add("stops", gson.toJsonTree(locationList))

        return jsonObjectRes
    }

    fun addItemsToDb() {
        val locationsDb = mysql.locationDao.getLocations(true)
        if (locationsDb.size == 0) {
            // add everything to db
            println("Adding all locations to db")
            val res = getData(mysql.locationDataDao.getData(0))
            val jsonArray = res.get("stops").asJsonArray
            for (g in 0 until jsonArray.size()) {
                val itemL = jsonArray.get(g).asJsonObject
                mysql.locationDao.addLocation(itemL)
            }
        } else {
            //todo fix this
            println("Adding new locations to db")
            val res = getData(mysql.locationDataDao.getData(locationsDb[0].startDate.time / 1000))
            println(res)
            val jsonArray = res.get("stops").asJsonArray
            val item = jsonArray.get(0).asJsonObject
            mysql.locationDao.updateLocation(
                Location(
                    locationsDb[0].locationId,
                    Timestamp(item.get("start").asLong),
                    Timestamp(item.get("end").asLong),
                    item.get("stopId").asInt
                )
            )

            jsonArray.remove(0)
            for (g in 0 until jsonArray.size()) {
                val itemL = jsonArray.get(g).asJsonObject
                mysql.locationDao.addLocation(itemL)
            }
        }
    }

    fun add(latTot: Double, lonTot: Double, count: Int, firstTime: Timestamp, endTime: Timestamp): LocationView {
        val stop = Address().getAddressName(
            round(latTot / count, 5),
            round(lonTot / count, 5),
            configItem,
            mysql
        )

        return LocationView(
            0,
            firstTime.time,
            endTime.time,
            round(latTot / count, 5),
            round(lonTot / count, 5),
            stop.stopId,
            stop.customName ?: stop.name
        )
    }

    companion object {
        fun distance(lat1: Double, lat2: Double, lon1: Double, lon2: Double, el1: Double, el2: Double): Double {
            val r = 6371 // Radius of the earth
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = (sin(latDistance / 2) * sin(latDistance / 2)
                    + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                    * sin(lonDistance / 2) * sin(lonDistance / 2)))
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            var distance = r * c * 1000 // convert to meters
            val height = el1 - el2
            distance = distance.pow(2.0) + height.pow(2.0)
            return sqrt(distance)
        }

        fun round(value: Double, places: Int): Double {
            require(places >= 0)
            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }
    }
}