import model.LocationItem
import model.LocationView
import model.Route

class Routes {
    fun getRouteFromStop(stops: ArrayList<LocationView>, all: ArrayList<LocationItem>): ArrayList<Route> {
        val routeList = ArrayList<Route>()
        for (i in 0 until stops.size - 1) {  //loop through every stop
            try {
                val item = stops[i]
                val itemS = stops[i + 1]
                var distanceTot = 0.0
                var pointCount = 0
                var lastLat = item.lat
                var lastLon = item.lon
                all.forEach { //loop through all locations
                    //get all locations between two stops
                    if (it.date.time >= item.end && it.date.time <= itemS.start) {
                        distanceTot += Timeline.distance(
                            it.lat, lastLat, it.lon, lastLon, 0.0, 0.0
                        )
                        lastLat = it.lat
                        lastLon = it.lon
                        pointCount += 1
                    }
                }
                val time = (itemS.start - item.end).toDouble()
                val timeHours = time / 1000 / 60 / 60
                var speed = distanceTot / 1000.0 / timeHours

                var movementType = "unknown"
                if (speed < 9) {
                    movementType = "walking"
                } else if (speed >= 9) {
                    movementType = "driving"
                }

                if (timeHours == 0.0) {
                    speed = 0.0
                }

                routeList.add(
                    Route(
                        null,
                        item.end,
                        itemS.start,
                        item.locationId,
                        itemS.locationId,
                        distanceTot,
                        time,
                        pointCount,
                        movementType,
                        speed
                    )
                )

            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        return routeList
    }
}