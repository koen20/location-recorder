import com.google.gson.Gson
import model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import kotlin.math.*


class Timeline(private val configItem: ConfigItem, private val mysql: Mysql) {
    fun getData(
        locationItems: ArrayList<LocationItem>,
        skipLast: Boolean = true,
        skipStops: Boolean = false
    ): ArrayList<LocationView> {
        val locationList = ArrayList<LocationView>()

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

            // increase the location radius if the time is longer than 14 minutes between two points
            if (distance(lat, item.lat, lon, item.lon, 0.0, 0.0) < 600 && item.date.time - lastTime > 820000) {
                if (locationList.size != 0) {
                    val lastAdded = locationList[locationList.size - 1]
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
                        locationItemsLoop.removeAt(0)
                    }

                    locationList.add(
                        add(
                            locationItemsLoop.sumOf { it.lat },
                            locationItemsLoop.sumOf { it.lon },
                            locationItemsLoop.size,
                            firstTime!!,
                            endTime!!,
                            skipStops
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
                    endTime!!,
                    skipStops,
                    skipLast
                )
            )
        }

        val arrayRoutes = Routes().getRouteFromStop(locationList, locationItems)

        //remove parts with possible inaccurate gps data
        try {
            //loop through all routes
            arrayRoutes.forEach { item ->
                if (item.pointCount!! <= 4 && item.distance < 700 &&
                    item.startLocation == item.stopLocation
                ) {
                    val removeList = ArrayList<LocationView>()
                    for (k in 0 until locationList.size - 1) {
                        val itemStop = locationList[k]
                        if (itemStop.end == item.startDate) {//get the stop before the route that is being removed
                            locationList[k].end = locationList[k + 1].end
                            removeList.add(locationList[k + 1])
                        }
                    }
                    removeList.forEach {
                        locationList.remove(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return locationList
    }

    fun addItemsToDb() {
        // get first location item, if nothing exists add everything,
        // if there already is an item resume adding at start date

        val locationsDbLast = mysql.locationDao.getLocations(true)
        val locationDataItems: ArrayList<LocationItem>
        val locations: ArrayList<LocationView>
        if (locationsDbLast.size == 0) {
            println("Adding all locations to db")
            locationDataItems = mysql.locationDataDao.getData(0)
            locations = getData(locationDataItems)
        } else {
            println("Adding new locations to db since ${Mqtt.getMysqlDateString(locationsDbLast[0].startDate.time / 1000)}")
            locationDataItems = mysql.locationDataDao.getData(locationsDbLast[0].startDate.time)
            locations = getData(locationDataItems)
            println(Gson().toJsonTree(locations))

            // update last added location item with new information
            val item = locations[0]
            locations[0].locationId = locationsDbLast[0].locationId
            println("Latest locationFetched: ${Gson().toJsonTree(locationsDbLast[0])}")
            mysql.locationDao.updateLocation(
                Location(
                    locationsDbLast[0].locationId,
                    Timestamp(item.start),
                    Timestamp(item.end),
                    item.stopId
                )
            )
        }
        locations.removeAt(locations.size - 1)

        // add all locations to the database and update the locationId in the locations array with the generated id
        locations.forEachIndexed { index, it ->
            if (locationsDbLast.size != 0) {
                if (index > 0) {
                    it.locationId = mysql.locationDao.addLocation(it)!!.locationId
                }
            } else {
                it.locationId = mysql.locationDao.addLocation(it)!!.locationId
            }
        }
        println("Added locations: ${Gson().toJsonTree(locations)}")

        //get all routes between locations. Then add the start and end location id to the route item
        val arrayRoutes = Routes().getRouteFromStop(locations, locationDataItems)
        arrayRoutes.forEach {
            locations.forEach { locationView ->
                if (locationView.end == it.startDate) {
                    it.startLocationId = locationView.locationId
                } else if (locationView.start == it.endDate) {
                    it.endLocationId = locationView.locationId
                }
            }
        }

        arrayRoutes.forEach {
            mysql.routeDao.addRoute(it)
        }
        println("Added routes: ${Gson().toJsonTree(arrayRoutes)}")
    }

    private fun add(
        latTot: Double,
        lonTot: Double,
        count: Int,
        firstTime: Timestamp,
        endTime: Timestamp,
        skipStops: Boolean,
        skipLast: Boolean = false
    ): LocationView {
        var stop = Stop(0, "", round(latTot / count, 5), round(lonTot / count, 5), "", "", null, null)
        if (!skipStops) {
            stop = Address().getAddressName(
                round(latTot / count, 5),
                round(lonTot / count, 5),
                configItem,
                mysql,
                skipLast
            )
        }

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
        // get the distance between two coordinates in meters
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