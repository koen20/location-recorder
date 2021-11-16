import com.google.gson.Gson
import com.google.gson.JsonObject
import model.Location
import model.LocationItem
import model.LocationView
import model.Route
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
        var lastTime: Long = 0
        var multiple = true
        var added = true
        var firstTime: Timestamp? = null
        var endTime: Timestamp? = null
        val locationItemsLoop = ArrayList<LocationItem>()

        locationItems.forEachIndexed { index, item ->
            locationItems[index].date = Timestamp(item.date.time)
            var radiusLocation = configItem.radiusLocation
            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) < 600 && item.date.time - lastTime > 820000) {
                if (locationList.size != 0) {
                    val lastAdded = locationList.get(locationList.size - 1)
                    if (distance(lastAdded.lat, item.lat, lastAdded.lon, item.lon, 0.0, 0.0) > 300) {
                        radiusLocation = 600
                    }
                }
            }

            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) >= radiusLocation) {
                time = item.date.time
                multiple = false
                if (!added) {
                    var locationCountLim = 4
                    if (locationItemsLoop.size > 15) {
                        locationCountLim = 10
                    }
                    while (locationItemsLoop.size > locationCountLim) {
                        locationItemsLoop.removeAt(locationItemsLoop.size - 1)
                        locationItemsLoop.removeAt(0);
                    }

                    locationList.add(
                            add(
                                locationItemsLoop.sumOf { it.lat },
                                locationItemsLoop.sumOf { it.lon },
                                locationItemsLoop.size,
                                firstTime!!,
                                endTime!!
                            )
                        )
                    added = true
                }
                locationItemsLoop.clear()
                firstTime = item.date
                lat = item.lat
                lon = item.lon
            }
            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) < radiusLocation) {
                if (item.date.time - time > 420000 && !multiple) {
                    multiple = true
                    added = false
                }
                locationItemsLoop.add(item)
                endTime = item.date
                lastTime = item.date.time
            }
        }
        if (!added) {
            locationList.add(
                add(
                    locationItemsLoop.sumOf { it.lat },
                    locationItemsLoop.sumOf { it.lon },
                    locationItemsLoop.size,
                    firstTime!!,
                    endTime!!
                )
            )
        }

        val arrayRoutes = Routes().getRouteFromStop(locationList, locationItems)

        //remove parts with possible inaccurate gps data
        try {
            //loop through all routes
            val removeListR = ArrayList<Route>()
            arrayRoutes.forEachIndexed { index, item ->
                if (item.pointCount!! <= 4 && item.distance < 700 &&
                    item.startLocation == item.stopLocation
                ) {
                    val removeList = ArrayList<LocationView>()
                    for (k in 0 until locationList.size - 1) {
                        val itemStop = locationList[k]
                        if (itemStop.end == item.startDate) {//get the stop before the route that is being removed
                            removeListR.add(item)
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
                arrayRoutes.remove(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val gson = Gson()

        jsonObjectRes.add("routes", gson.toJsonTree(arrayRoutes))
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