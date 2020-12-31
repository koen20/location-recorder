import model.Location
import model.OsAddressItem
import model.Stop
import org.json.JSONObject
import java.sql.*
import java.util.*
import kotlin.collections.ArrayList

class Mysql(configItem: ConfigItem) {
    companion object {
        lateinit var conn: Connection
        var stops: ArrayList<Stop> = ArrayList()
        var osAddressItems: ArrayList<OsAddressItem> = ArrayList()
        lateinit var configItem: ConfigItem

        fun getStopsDb(): ArrayList<Stop> {
            val items: ArrayList<Stop> = ArrayList()
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT * FROM stops").use { rs ->
                        while (rs.next()) {
                            val stop = Stop(
                                    rs.getInt("id"),
                                    rs.getString("name"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getInt("radius"),
                                    true
                            )
                            items.add(stop)
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return items
        }

        fun getOsAddressDb(): ArrayList<OsAddressItem> {
            val items: ArrayList<OsAddressItem> = ArrayList()
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT * FROM osData").use { rs ->
                        while (rs.next()) {
                            val addressItem = OsAddressItem(
                                    rs.getInt("id"),
                                    rs.getString("name"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("dateFetched"),
                                    rs.getString("city"),
                                    rs.getString("country")
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

    init {
        try {
            conn =
                    DriverManager.getConnection(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
            stops = getStopsDb()
            osAddressItems = getOsAddressDb()
        } catch (e: Exception) {
            println("Failed to connect to database $e")
        }
        val updateTimer = Timer()
        updateTimer.scheduleAtFixedRate(checkMysqlConnection(), 10000, 60000)
        val updateTimerStops = Timer()
        updateTimerStops.scheduleAtFixedRate(updateTimerStops(), 120000, 21600000)
    }

    fun disconnect() {
        try {
            conn.close()
        } catch (e: Exception) {

        }
    }

    fun addStop(stop: Stop): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO stops VALUES(NULL, ?, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setString(1, stop.name)
                ps.setDouble(2, stop.lat)
                ps.setDouble(3, stop.lon)
                ps.setInt(4, stop.radius)
                ps.execute()
            }
            added = true
            updateStops()
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun addLocation(item: JSONObject): Boolean{
        return addLocation(
                Location(
                        0,
                        Timestamp(item.getLong("start")),
                        Timestamp(item.getLong("end")),
                        item.getInt("osDataId"),
                        item.getInt("savedLocationId")
                )
        )
    }

    fun addLocation(location: Location): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO location VALUES(NULL, ?, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setTimestamp(1, location.startDate)
                ps.setTimestamp(2, location.endDate)
                if (location.osDataId == 0) {
                    ps.setNull(3, Types.INTEGER)
                } else {
                    ps.setInt(3, location.osDataId)
                }
                if (location.savedLocationId == 0) {
                    ps.setNull(4, Types.INTEGER)
                } else {
                    ps.setInt(4, location.savedLocationId)

                }
                ps.execute()
            }
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun updateLocation(location: Location): Boolean {
        var added = false
        try {
            val insert = "UPDATE location SET startDate = ?, endDate = ?, osDataId = ?, savedLocationId = ? WHERE id = ?"
            conn.prepareStatement(insert).use { ps ->
                ps.setTimestamp(1, location.startDate)
                ps.setTimestamp(2, location.endDate)
                if (location.osDataId == 0) {
                    ps.setNull(3, Types.INTEGER)
                } else {
                    ps.setInt(3, location.osDataId)
                }
                if (location.savedLocationId == 0) {
                    ps.setNull(4, Types.INTEGER)
                } else {
                    ps.setInt(4, location.savedLocationId)

                }
                ps.setInt(5, location.id)
                ps.execute()
            }
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun getLocations(lastValue: Boolean = false): ArrayList<Location> {
        val items: ArrayList<Location> = ArrayList()
        try {
            var query = "SELECT * FROM location"
            if (lastValue) {
                query = "SELECT * FROM location ORDER BY startDate DESC LIMIT 1"
            }
            conn.createStatement().use { stmt ->
                stmt.executeQuery(query).use { rs ->
                    while (rs.next()) {
                        val addressItem = Location(
                                rs.getInt("id"),
                                rs.getTimestamp("startDate"),
                                rs.getTimestamp("endDate"),
                                rs.getInt("osDataId"),
                                rs.getInt("savedLocationId")
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

    fun addOsAddress(item: OsAddressItem): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO osData VALUES(NULL, ?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setString(1, item.name)
                ps.setDouble(2, item.lat)
                ps.setDouble(3, item.lon)
                ps.setTimestamp(4, item.dateFetched)
                ps.setString(5, item.city)
                ps.setString(6, item.country)
                ps.execute()
            }
            osAddressItems.add(item)
            added = true
            updateOsAdresses()
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun getData(startTime: Long, endTime: Long = 7289648397): ArrayList<LocationItem> {
        val data = ArrayList<LocationItem>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM data WHERE date BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${Mqtt.getMysqlDateString(endTime)}'").use { rs ->
                while (rs.next()) {
                    data.add(LocationItem(rs.getTimestamp("date"), rs.getDouble("lat"), rs.getDouble("lon")))
                }
            }
        }

        return data
    }

    fun updateStops() {
        stops = getStopsDb()
    }

    fun updateOsAdresses() {
        osAddressItems = getOsAddressDb()
    }

    fun getStops(): ArrayList<Stop> {
        return stops
    }

    fun getOsAddressItems(): ArrayList<OsAddressItem> {
        return osAddressItems
    }

    private class checkMysqlConnection : TimerTask() {
        override fun run() {
            try {
                if (!conn.isValid(3000)) {
                    conn.close()
                    conn = DriverManager.getConnection(
                            configItem.mysqlServer,
                            configItem.mysqlUsername, configItem.mysqlPassword
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    private class updateTimerStops : TimerTask() {
        override fun run() {
            stops = getStopsDb()
            osAddressItems = getOsAddressDb()
        }
    }
}