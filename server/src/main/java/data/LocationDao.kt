package data

import com.google.gson.Gson
import com.google.gson.JsonObject
import model.Location
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp

interface LocationDao {
    fun addLocation(location: Location): Boolean
    fun addLocation(location: JsonObject): Boolean
    fun updateLocation(location: Location): Boolean
    fun getLocations(lastValue: Boolean): ArrayList<Location>
    //fun getLocations(startTime: Long, endTime: Long): ArrayList<LocationName>
    fun getLocations(stopName: String): ArrayList<Location>
}

class LocationDaoImpl(private val conn: Connection) : LocationDao {

    override fun addLocation(location: Location): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO location VALUES(NULL, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setTimestamp(1, location.startDate)
                ps.setTimestamp(2, location.endDate)
                ps.setInt(3, location.stopId)
                ps.execute()
            }
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
            val gson = Gson()
            println(gson.toJson(location))
        }
        return added
    }

    override fun addLocation(location: JsonObject): Boolean {
        return addLocation(
            Location(
                0,
                Timestamp(location.get("start").asLong),
                Timestamp(location.get("end").asLong),
                location.get("stopId").asInt,
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