package data

import Mqtt
import model.Location
import model.LocationView
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp


interface LocationDao {
    fun addLocations(location: ArrayList<Location>): Location?
    fun addLocationViews(location: ArrayList<LocationView>): Location?
    fun updateLocation(location: Location): Boolean
    fun getLocations(lastValue: Boolean): ArrayList<Location>
    fun getLocationsView(startTime: Long, endTime: Long): ArrayList<LocationView>

    //fun getLocations(startTime: Long, endTime: Long): ArrayList<LocationName>
    fun getLocations(stopName: String): ArrayList<Location>
}

class LocationDaoImpl(private val conn: Connection) : LocationDao {

    //add location to db, returns added location with the generated id
    override fun addLocations(locations: ArrayList<Location>): Location? {
        var locationAdded: Location? = null
        try {
            val insert = "INSERT INTO location VALUES(NULL, ?, ?, ?)"
            val pst = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)
            var count = 0
            var executeResult: IntArray = IntArray(0)
            locations.forEachIndexed { index, it ->
                pst.use { ps ->
                    ps.setTimestamp(1, it.startDate)
                    ps.setTimestamp(2, it.endDate)
                    ps.setInt(3, it.stopId)
                    ps.addBatch()
                    count++
                    if (count > 500 || index + 1 == locations.size) {
                        executeResult = ps.executeBatch()
                    }
                }
            }

            val rs = pst.generatedKeys
                for (i in 0 until executeResult.size) {
                    rs.next()
                    if (executeResult[i] === 1) {
                        println(
                            "Execute Result: " + i + ", Update Count: " + executeResult[i] + ", id: "
                                    + rs.getLong(1)
                        )
                    }
                }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return locationAdded
    }

    override fun addLocationViews(locationViews: ArrayList<LocationView>): Location? {
        val locations = ArrayList<Location>()
        locationViews.forEach {
            locations.add(
                Location(
                    0,
                    Timestamp(it.start),
                    Timestamp(it.end),
                    it.stopId,
                )
            )
        }

        return addLocations(locations)
    }

    override fun updateLocation(location: Location): Boolean {
        var added = false
        try {
            conn.prepareStatement("UPDATE location SET startDate = ?, endDate = ?, stopId = ? WHERE locationId = ?")
                .use { ps ->
                    ps.setTimestamp(1, location.startDate)
                    ps.setTimestamp(2, location.endDate)
                    ps.setInt(3, location.stopId)
                    ps.setInt(4, location.locationId)
                    ps.execute()
                }
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    override fun getLocations(lastValue: Boolean): ArrayList<Location> {
        val items: ArrayList<Location> = ArrayList()
        try {
            var query = "SELECT * FROM location"
            if (lastValue) {
                query = "SELECT * FROM location ORDER BY startDate DESC LIMIT 1"
            }
            conn.createStatement().use { stmt ->
                stmt.executeQuery(query).use { rs ->
                    while (rs.next()) {
                        val locationItem = Location(
                            rs.getInt("locationId"),
                            rs.getTimestamp("startDate"),
                            rs.getTimestamp("endDate"),
                            rs.getInt("stopId"),
                        )
                        items.add(locationItem)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return items
    }

    override fun getLocationsView(startTime: Long, endTime: Long): ArrayList<LocationView> {
        val items: ArrayList<LocationView> = ArrayList()
        try {
            val query = "SELECT * FROM location, stop WHERE location.stopId = stop.stopId AND (startDate BETWEEN '${
                Mqtt.getMysqlDateString(startTime)
            }' AND '${
                Mqtt.getMysqlDateString(
                    endTime
                )
            }' or endDate BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${
                Mqtt.getMysqlDateString(
                    endTime
                )
            }') order by location.locationId"

            conn.createStatement().use { stmt ->
                stmt.executeQuery(query).use { rs ->
                    while (rs.next()) {
                        val locationViewItem = LocationView(
                            rs.getInt("locationId"),
                            rs.getTimestamp("startDate").time,
                            rs.getTimestamp("endDate").time,
                            rs.getDouble("lat"),
                            rs.getDouble("lon"),
                            rs.getInt("stopId"),
                            rs.getString("customName") ?: rs.getString("name")
                        )
                        items.add(locationViewItem)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return items
    }

    /*override fun getLocations(startTime: Long, endTime: Long): ArrayList<LocationName> {
        val data = ArrayList<LocationName>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery(
                "SELECT * FROM location, stops WHERE (startDate BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${
                    Mqtt.getMysqlDateString(
                        endTime
                    )
                }' or endDate BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${
                    Mqtt.getMysqlDateString(
                        endTime
                    )
                }') AND location.savedLocationId = stops.id"
            ).use { rs ->
                while (rs.next()) {
                    data.add(
                        LocationName(
                            0, rs.getTimestamp("startDate"), rs.getTimestamp("endDate"),
                            rs.getInt("osDataId"), rs.getInt("savedLocationId"), rs.getString("name"), rs.getDouble("lat"), rs.getDouble("lon")
                        )
                    )
                }
            }
        }

        conn.createStatement().use { stmt ->
            stmt.executeQuery(
                "SELECT * FROM location, osData WHERE startDate BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${
                    Mqtt.getMysqlDateString(
                        endTime
                    )
                }' AND location.osDataId = osData.id"
            ).use { rs ->
                while (rs.next()) {
                    data.add(
                        LocationName(
                            0, rs.getTimestamp("startDate"), rs.getTimestamp("endDate"),
                            rs.getInt("osDataId"), rs.getInt("savedLocationId"), rs.getString("name"), rs.getDouble("lat"), rs.getDouble("lon")
                        )
                    )
                }
            }
        }

        return data
    }*/

    override fun getLocations(stopName: String): ArrayList<Location> {
        val items: ArrayList<Location> = ArrayList()
        try {
            conn.prepareStatement(
                "SELECT * FROM location, stop WHERE location.stopId = stop.stopId AND (stop.name = ? OR stop.customName = ?)"
            )
                .use { stmt ->
                    stmt.setString(1, stopName)
                    stmt.setString(2, stopName)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            val addressItem = Location(
                                rs.getInt("locationId"),
                                rs.getTimestamp("startDate"),
                                rs.getTimestamp("endDate"),
                                rs.getInt("stopId"),
                            )
                            items.add(addressItem)
                        }
                    }
                }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return items
    }
}