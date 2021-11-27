package data

import model.Location
import model.LocationView
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp


interface LocationDao {
    fun addLocation(location: Location): Location?
    fun addLocation(location: LocationView): Location?
    fun updateLocation(location: Location): Boolean
    fun getLocations(lastValue: Boolean): ArrayList<Location>
    fun getLocationsView(startTime: Long, endTime: Long, lastValue: Boolean = false): ArrayList<LocationView>
    fun getLocations(stopName: String): ArrayList<Location>
}

class LocationDaoImpl(private val conn: Connection) : LocationDao {

    //add location to db, returns added location with the generated id
    override fun addLocation(location: Location): Location? {
        var locationAdded: Location? = null
        try {
            val insert = "INSERT INTO location VALUES(NULL, ?, ?, ?)"
            val pst = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)
            pst.use { ps ->
                ps.setTimestamp(1, location.startDate)
                ps.setTimestamp(2, location.endDate)
                ps.setInt(3, location.stopId)
                ps.execute()
            }
            locationAdded = location

            val rs = pst.generatedKeys
            if (rs.next()) {
                locationAdded.locationId = rs.getInt(1)
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return locationAdded
    }

    override fun addLocation(location: LocationView): Location? {
        return addLocation(
            Location(
                0,
                Timestamp(location.start),
                Timestamp(location.end),
                location.stopId,
            )
        )
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

    override fun getLocationsView(startTime: Long, endTime: Long, lastValue: Boolean): ArrayList<LocationView> {
        val items: ArrayList<LocationView> = ArrayList()
        try {
            val query: String = if (lastValue) {
                "SELECT * FROM location, stop WHERE location.stopId = stop.stopId " +
                        "ORDER BY startDate DESC LIMIT 1"
            } else {
                "SELECT * FROM location, stop WHERE location.stopId = stop.stopId " +
                        "AND ((startDate BETWEEN ? AND ? or endDate BETWEEN ? AND ?) " +
                        "or (? between startDate AND endDate or ? between startDate AND endDate)) " +
                        "order by location.locationId"
            }
            conn.prepareStatement(query).use { stmt ->
                if (!lastValue) {
                    stmt.setTimestamp(1, Timestamp(startTime))
                    stmt.setTimestamp(2, Timestamp(endTime))
                    stmt.setTimestamp(3, Timestamp(startTime))
                    stmt.setTimestamp(4, Timestamp(endTime))
                    stmt.setTimestamp(5, Timestamp(startTime))
                    stmt.setTimestamp(6, Timestamp(endTime))
                }
                stmt.executeQuery().use { rs ->
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