import model.Location
import model.OsAddressItem
import model.Stop
import org.json.JSONObject
import java.sql.*
import java.util.*
import kotlin.collections.ArrayList

interface Storage {
    fun addStop(stop: Stop): Boolean
    fun addLocation(item: JSONObject): Boolean
    fun addLocation(location: Location): Boolean
    fun updateLocation(location: Location): Boolean
    fun getLocations(lastValue: Boolean = false): ArrayList<Location>
    fun addOsAddress(item: OsAddressItem): Boolean
    fun getData(startTime: Long, endTime: Long = 7289648397): ArrayList<LocationItem>
    fun updateStops()
    fun updateOsAdresses()
    fun getLocations(stopName: String): ArrayList<Location>
}

class Mysql(configItem: ConfigItem) : Storage {
    var stops: ArrayList<Stop> = ArrayList()
    var osAddressItems: ArrayList<OsAddressItem> = ArrayList()
    lateinit var conn: Connection

    init {
        try {
            conn =
                DriverManager.getConnection(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
            stops = getStopsDb()
            osAddressItems = getOsAddressDb()
        } catch (e: Exception) {
            println("Failed to connect to database $e")
        }

        Timer().scheduleAtFixedRate(object : TimerTask() {
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
        }, 10000, 60000)

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                stops = getStopsDb()
                osAddressItems = getOsAddressDb()
            }
        }, 120000, 21600000)
    }

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

    fun disconnect() {
        try {
            conn.close()
        } catch (e: Exception) {

        }
    }

    override fun addStop(stop: Stop): Boolean {
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

    override fun addLocation(item: JSONObject): Boolean {
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

    override fun addLocation(location: Location): Boolean {
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

    override fun updateLocation(location: Location): Boolean {
        var added = false
        try {
            conn.prepareStatement("UPDATE location SET startDate = ?, endDate = ?, osDataId = ?, savedLocationId = ? WHERE id = ?")
                .use { ps ->
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

    override fun addOsAddress(item: OsAddressItem): Boolean {
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

    override fun getData(startTime: Long, endTime: Long): ArrayList<LocationItem> {
        val data = ArrayList<LocationItem>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery(
                "SELECT * FROM data WHERE date BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${
                    Mqtt.getMysqlDateString(
                        endTime
                    )
                }'"
            ).use { rs ->
                while (rs.next()) {
                    data.add(LocationItem(rs.getTimestamp("date"), rs.getDouble("lat"), rs.getDouble("lon")))
                }
            }
        }

        return data
    }

    override fun updateStops() {
        stops = getStopsDb()
    }

    override fun updateOsAdresses() {
        osAddressItems = getOsAddressDb()
    }

    override fun getLocations(stopName: String): ArrayList<Location> {
        val items: ArrayList<Location> = ArrayList()
        try {
                conn.prepareStatement(
                    "SELECT * FROM location, stops WHERE location.savedLocationId = stops.id AND stops.name = ?"
                )
                    .use { stmt ->
                        stmt.setString(1, stopName)
                        stmt.executeQuery().use { rs ->
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
}